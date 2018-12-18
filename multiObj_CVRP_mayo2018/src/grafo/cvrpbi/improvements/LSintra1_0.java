package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.CVRPInstance;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public class LSintra1_0 implements Improvement<WCPSolution> {

	private int maxIter = 10;

	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		int nonImproved = 0;
		double bestF = solution.getOF();
		while (nonImproved < maxIter) {
			int nRoutes = solution.getNumRoutes();
			int r = rnd.nextInt(nRoutes);
			int size = solution.getRoute(r).size();
			double bestDist = solution.getRoute(r).getDistance();
			double bestTime = solution.getRoute(r).getTime();
			if (size - 2 <= 0) {
				nonImproved++;
				System.out.println("eliminamos una Ruta");
				solution.removeRoute(r);
			} else {
				int bestP1 = -1;
				int bestP2 = -1;
				for (int start = 1; start < size - 1; start++) {
					for (int dst = 1; dst < size - 1; dst++) {
						if (dst == start) {
							continue;
						}
						double time = solution.evalTimeMoveSubRoute(r, start, start, dst);
						if (time > WCPSolution.workingTime) {
							continue;
						}

						double distance = solution.evalMoveSubRoute(r, start, start, dst);
						double f1 = solution.getTotalDist() - solution.getRoute(r).getDistance() + distance;
						int LR = solution.longestRouteIf(r, distance, solution.getRoute(r).getDistance());
						double f2 = (LR == r) ? distance : solution.getRoute(LR).getDistance();
						double f3 = solution.difTimeIf(r, time);
						if (Pareto.checkDominance(f1, f2, f3, solution.getNumRoutes())) {
							WCPSolution newPareto = new WCPSolution(solution);
							newPareto.moveSubRoute(r, start, start, dst, distance, time);
							if (!Pareto.add(newPareto)) {
								Pareto.addApprox(newPareto);
							}
						}
						if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
							if ((distance - bestDist) < -CVRPInstance.EPSILON) {
								bestDist = distance;
								bestTime = time;
								bestP1 = start;
								bestP2 = dst;
							}
						} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {

							if (f2 < bestF) {
								bestDist = distance;
								bestF = f2;
								bestTime = time;
								bestP1 = start;
								bestP2 = dst;
							}
						} else if (WCPSolution.currentOF == ObjFunct.TIME) {
							if (f3 < bestF) {
								bestDist = distance;
								bestF = f3;
								bestTime = time;
								bestP1 = start;
								bestP2 = dst;

							}
						} else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
							double f = C1_WCK.evalWierzbicki(f1, f2, f3, solution.getNumRoutes());
							if (f < bestF) {
								bestDist = distance;
								bestTime = time;
								bestP1 = start;
								bestP2 = dst;
								bestF = f;
							}
						}
					}
				}
				if (bestP1 > 0) {
					solution.moveSubRoute(r, bestP1, bestP1, bestP2, bestDist, bestTime);
					nonImproved = 0;
					break;
				} else
					nonImproved++;
			}
		}
	}
}
