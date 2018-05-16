package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.improvements.multi.VND_WCK;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public class VNS_WCK implements Improvement<WCPSolution> {

	private C1_WCK c;// lo necesito para el valor de lambda!
	private VND_WCK ls;
	private WCPSolution best;
	private double kStep;
	private static final double kMax = 0.2;

	public VNS_WCK(C1_WCK c, VND_WCK ls, double kStep) {
		this.c = c;
		this.ls = ls;
		this.kStep = kStep;
	}

	@Override
	public void improve(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		int nRoutes = sol.getNumRoutes();
		double WValue = C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),sol.getDifTime(),nRoutes);
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = shake(sol, pertSize);
			ls.improve(s);
			if (best == null) {
				best = s;
				WValue = C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),nRoutes);
			} else {
				if (C1_WCK.evalWierzbicki(s.getTotalDist(), s.getDistanceLongestRoute(),s.getDifTime(),nRoutes) < WValue) {
					best = s;
					sol.copy(s);
					WValue = C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),nRoutes);
				}
			}
			k = k + kStep;
		}
	}

	private WCPSolution shake(WCPSolution s, int k) {
		WCPSolution sol = new WCPSolution(s);
		Random rnd = RandomManager.getRandom();
		int i = 0;
		int nRoutes = s.getNumRoutes();
		while (i < k) {
			int r1 = rnd.nextInt(nRoutes);
			int size1 = sol.getRoute(r1).size();
			int r2 = rnd.nextInt(nRoutes);
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

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + c + "," + ls + ")";
	}
}
