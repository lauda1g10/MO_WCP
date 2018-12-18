package grafo.cvrpbi.improvements.multi;

import grafo.cvrpbi.structure.CVRPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by jesussanchezoro on 06/10/2017.
 */
public class VND_refL2 implements Improvement<WCPSolution> {

    protected Improvement<WCPSolution>[] ls;

    public VND_refL2() {
        this.ls = MultiLS_ref.getLS();
    }

    @Override
    public void improve(WCPSolution solution) {
        int k = 0;
        double before = solution.distanceL2To(MultiLS_ref.getBestPoint());
        while (k < ls.length) {
            ls[k].improve(solution);
            if (solution.distanceL2To(MultiLS_ref.getBestPoint()) - before < -CVRPInstance.EPSILON) {
                k = 0;
               before = solution.distanceL2To(MultiLS_ref.getBestPoint());
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
