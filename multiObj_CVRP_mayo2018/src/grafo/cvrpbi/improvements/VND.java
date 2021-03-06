package grafo.cvrpbi.improvements;

import grafo.cvrpbi.structure.CVRPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by jesussanchezoro on 06/10/2017.
 */
public class VND implements Improvement<WCPSolution> {

    protected Improvement<WCPSolution>[] ls;

    public VND(Improvement<WCPSolution>[] ls) {
        this.ls = ls;
    }

    @Override
    public void improve(WCPSolution solution) {
        int k = 0;
        while (k < ls.length) {
            double before = solution.getOF();
            ls[k].improve(solution);
            if (solution.getOF() - before < -CVRPInstance.EPSILON) {
                k = 0;
            } else {
                k++;
            }
        }    
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("VND").append("(");
        for (Improvement<WCPSolution> search : ls) {
            stb.append(search.getClass().getSimpleName()).append(",");
        }
        stb.append(")");
        return stb.toString();
    }
}
