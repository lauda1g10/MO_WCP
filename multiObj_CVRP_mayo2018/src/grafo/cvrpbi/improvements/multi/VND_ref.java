package grafo.cvrpbi.improvements.multi;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.Utilities;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by lauradelgadoAntequera on 18/10/2017.
 */
public class VND_ref implements Improvement<WCPSolution> {

    private Improvement<WCPSolution>[] ls;//han de ser BL para optimizar la distancia L2 al pto de referencia

    public VND_ref(Improvement<WCPSolution>[] ls) {
        this.ls = ls;
    }

    @Override
    public void improve(WCPSolution solution) {
        int k = 0;
        while (k < ls.length) {
            double before = Utilities.distanceToRefL2(solution);
            ls[k].improve(solution);
            Pareto.add(solution);
            if (Utilities.distanceToRefL2(solution) - before < 0) {
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
