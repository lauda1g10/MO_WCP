package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.CVRPInstance;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public class FLS_1to1 implements Improvement<WCPSolution> {

	private int maxIter = 5;
	@Override
	public String toString() {
		return "FLS_1to1";
	}
	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		int nonImproved = 0;
		boolean entra = true;
		double bestF = solution.getOF();
		while (entra && nonImproved < maxIter) {
			int nRoutes = solution.getNumRoutes();
			int r1 = rnd.nextInt(nRoutes);
			int r2 = rnd.nextInt(nRoutes);
			int size1 = solution.getRoute(r1).size()-1;
			int size2 = solution.getRoute(r2).size()-1;
			if (size1 - 2 <= 0) {
				nonImproved++;
				// System.out.println("eliminamos una Ruta");
				solution.removeRoute(r1);
			} else if (size2 - 2 <= 0) {
				nonImproved++;
				// System.out.println("eliminamos una Ruta");
				solution.removeRoute(r2);
			} else {
				int start = rnd.nextInt(size1 - 1) + 1;
				int dst = rnd.nextInt(size2 - 1) + 1;
				if (r1 == r2 && start == dst) {
					continue;
				}
				double[] time = solution.evalTimeInterchange(r1, r2, start, start, dst, dst);
				if (Math.max(time[0], time[1]) > WCPSolution.workingTime) {
					continue;
				}
				double[] distance = solution.evalInterchange(r1, r2, start, start, dst, dst);
				double f1 = solution.getTotalDist() - solution.getRoute(r1).getDistance() + distance[0]
						- solution.getRoute(r2).getDistance() + distance[1];
				int LR = solution.longestRouteIf(r1, distance[0], solution.getRoute(r1).getDistance());
				double f2 = (LR == r1) ? distance[0] : solution.getRoute(LR).getDistance();
				LR = solution.longestRouteIf(r2, distance[1], solution.getRoute(LR).getDistance());
				f2 = (LR == r2) ? distance[1] : solution.getRoute(LR).getDistance();
				double f3 = solution.difTimeIf(r1, time[0], r2, time[1]);
				if (Pareto.checkDominance(f1, f2, f3, solution.getNumRoutes())) {
					WCPSolution newPareto = new WCPSolution(solution);
					newPareto.moveInterchange(r1, r2, start, start, dst, dst, distance, time);
					if (!Pareto.add(newPareto)) {
						Pareto.addApprox(newPareto);
					}
				}
				if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
					if ((f1 - bestF) < -CVRPInstance.EPSILON) {
						solution.moveInterchange(r1, r2, start, start, dst, dst, distance, time);
						entra = false;
						break;
						}
					else{
						nonImproved++;
					}
				} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {

					if (f2 < bestF) {
						solution.moveInterchange(r1, r2, start, start, dst, dst, distance, time);
						entra = false;
						break;
						}
					else{
						nonImproved++;
					}
				} else if (WCPSolution.currentOF == ObjFunct.TIME) {
					if (f3 < bestF) {
						solution.moveInterchange(r1, r2, start, start, dst, dst, distance, time);
						entra = false;
						break;
						}
					else{
						nonImproved++;
					}
				} else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
					double f = C1_WCK.evalWierzbicki(f1, f2, f3, solution.getNumRoutes());
					if (f < bestF) {
						solution.moveInterchange(r1, r2, start, start, dst, dst, distance, time);
						entra = false;
						break;
						}
					else{
						nonImproved++;
					}
				}
			}
		}
	}
}
