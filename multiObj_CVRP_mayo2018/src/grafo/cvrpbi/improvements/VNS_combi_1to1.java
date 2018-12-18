package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public abstract class VNS_combi_1to1 extends VNS_combi {

	public VNS_combi_1to1(Improvement<WCPSolution>[] ls, double kStep) {
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
			int r2 = rnd.nextInt(nr);
			int size2 = sol.getRoute(r2).size();
	
			if (size1 < 3 || size2 < 3) {
				continue;
			}
			int p1 = 1 + rnd.nextInt(size1 - 2);
			int v1 = sol.getRoute(r1).getNodeAt(p1);
			int p2 = 1 + rnd.nextInt(size2 - 2);
			int v2 = sol.getRoute(r2).getNodeAt(p2);
			if (r1 == r2 && v1 == v2) {
				continue;
			} else if (sol.isFeasibleInterchange(r1, v1, r2, v2)) {
				double evalT [] = sol.evalTimeInterchange(r1, r2, p1, p1, p2, p2);
				if (Math.max(evalT[0], evalT[1])<=WCPSolution.workingTime){
				double eval[] = sol.evalInterchange(r1, r2, p1, p1, p2, p2);
				sol.moveInterchange(r1, r2, p1, p1, p2, p2, eval,evalT);
			}
			i++;
		}
		}
		return sol;
	}
}
