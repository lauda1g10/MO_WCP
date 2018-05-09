package grafo.cvrpbi.improvements;

import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by jesussanchezoro on 06/10/2017.
 */
public class VND implements Improvement<WCPSolution> {

    private Improvement<WCPSolution>[] ls;

    public VND(Improvement<WCPSolution>[] ls) {
        this.ls = ls;
    }

    @Override
    public void improve(WCPSolution solution) {
        int k = 0;
        while (k < ls.length) {
            double before = solution.getOF();
            ls[k].improve(solution);
            if (solution.getOF() - before < -WCPInstance_RealInstance.EPSILON) {
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
