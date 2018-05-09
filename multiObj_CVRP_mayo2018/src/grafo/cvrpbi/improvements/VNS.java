package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public class VNS implements Improvement<WCPSolution> {

	private VND ls;
	private WCPSolution best;
	private double kStep;
	private static final double kMax = 0.2;

	public VNS(Improvement[] improvements, double kStep) {
		this.ls = new VND(improvements);
		this.kStep = kStep;
	}

	@Override
	public void improve(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		double Value = sol.getOF();
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = shake(sol, pertSize);
			ls.improve(s);
			if (best == null || s.getOF() < Value) {
				best = s;
				Value = s.getOF();
				sol.copy(s);
			}
			k = k + kStep;
		}
	}

	private WCPSolution shake(WCPSolution s, int k) {
		WCPSolution sol = new WCPSolution(s);
		int nr = sol.getInstance().getVehicles();
		Random rnd = RandomManager.getRandom();
		int i = 0;
		while (i < k) {
			int r1 = rnd.nextInt(nr);
			int size1 = sol.getRoute(r1).size();
			// double dif1 =
			// sol.getRoute(r1).getDistance()-sol.getRoute(r1).distanceBetween(0,
			// size1-1);
			int r2 = rnd.nextInt(nr);
			int size2 = sol.getRoute(r2).size();
			// double dif2 =
			// sol.getRoute(r2).getDistance()-sol.getRoute(r2).distanceBetween(0,
			// size2-1);
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
				double evalT[] = sol.evalTimeInterchange(r1, r2, p1, p1, p2, p2);
				if (Math.max(evalT[0], evalT[1]) <= WCPSolution.workingTime) {
					double eval[] = sol.evalInterchange(r1, r2, p1, p1, p2, p2);
					sol.moveInterchange(r1, r2, p1, p1, p2, p2, eval, evalT);
				}
				i++;
			}
		}
		return sol;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + ls + ")";
	}
}
