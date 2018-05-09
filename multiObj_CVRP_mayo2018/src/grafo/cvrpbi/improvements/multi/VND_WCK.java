package grafo.cvrpbi.improvements.multi;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.Utilities;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by lauradelgadoAntequera on 18/10/2017.
 */
public class VND_WCK implements Improvement<WCPSolution> {

    private Improvement<WCPSolution>[] ls;//han de ser BL para optimizar la distancia L2 al pto de referencia

    public VND_WCK(Improvement<WCPSolution>[] ls) {
        this.ls = ls;
    }

    @Override
    public void improve(WCPSolution solution) {
        int k = 0;
        while (k < ls.length) {
            double before = C1_WCK.evalWierzbicki(solution.getTotalDist(), solution.getDistanceLongestRoute(), solution.getDifTime(), solution.getNumRoutes());
            ls[k].improve(solution);
            Pareto.add(solution);
            if (C1_WCK.evalWierzbicki(solution.getTotalDist(), solution.getDistanceLongestRoute(), solution.getDifTime(), solution.getNumRoutes()) - before < 0) {
                k = 0;
            } else {
                k++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append(this.getClass().getSimpleName()).append("(");
        for (Improvement<WCPSolution> search : ls) {
            stb.append(search).append(",");
        }
        stb.append(")");
        return stb.toString();
    }
}
