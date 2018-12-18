package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public abstract class VNS_combi_1to0 extends VNS_combi {

	public VNS_combi_1to0(Improvement<WCPSolution>[] ls, double kStep) {
		super(ls,kStep);
	}
	protected WCPSolution shake(WCPSolution s, int k) {
    	WCPSolution sol = new WCPSolution(s);
        int nr = sol.getNumRoutes();
        Random rnd = RandomManager.getRandom();
        int i = 0;
        while (i < k) {
            int r1 = rnd.nextInt(nr);
            int size1 = sol.getRoute(r1).size();
            if (size1<3){
            	continue;
            }
            int p1 = 1+rnd.nextInt(size1-2);
            int p2 = 1+rnd.nextInt(size1-2);
            int min = Math.min(p1, p2);
            int max = Math.max(p1, p2);
            if (p1 == p2)
            {
            	continue;
            }
            else 
            {
            	double time = sol.evalTimeMove2Opt(r1, min, max);
				if (time > WCPSolution.workingTime) {
					continue;
				}
            double eval = sol.evalMove2Opt(r1, min, max);
            sol.move2Opt(r1, min, max, eval, time);
            }
            i++;
        }
        return sol;
    }

}
