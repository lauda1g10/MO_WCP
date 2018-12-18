package grafo.cvrpbi.improvements.multi;

import java.util.Random;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

public class BMLS_1to0refL2 implements Improvement<WCPSolution> {

	private int maxIter = 10;
@Override
public String toString() {
	return "BMLS_1to0refL2";
}
	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		int nonImproved = 0;
		double bestF = solution.distanceL2To(MultiLS_ref.getBestPoint());
		while (nonImproved < maxIter) {
			int nRoutes = solution.getNumRoutes();
			int r1 = rnd.nextInt(nRoutes);
			int r2 = rnd.nextInt(nRoutes);
			int size1 = solution.getRoute(r1).size();
			int size2 = solution.getRoute(r2).size();
			if (r1 == r2) {
				// intra-route move
				int r = r1;
				double bestDist = solution.getRoute(r1).getDistance();
				double bestTime = solution.getRoute(r1).getTime();
				if (size1 - 2 <= 0) {
					nonImproved++;
					//// System.out.println("eliminamos una Ruta");
					solution.removeRoute(r1);
				} else {
					int bestP1 = -1;
					int bestP2 = -1;
					for (int start = 1; start < size1 - 1; start++) {
						for (int dst = 1; dst < size1 - 1; dst++) {
							if (dst == start) {
								continue;
							}
							double time = solution.evalTimeMoveSubRoute(r1, start, start, dst);
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
								Pareto.add(newPareto);
							}
							double f = MultiLS_ref.distanceL2ToBest(f1, f2, f3, solution.getNumRoutes());
							if (f < bestF) {
								bestDist = distance;
								bestTime = time;
								bestP1 = start;
								bestP2 = dst;
								bestF = f;
							}
						}
						if (bestP1 > 0) {
							solution.moveSubRoute(r, bestP1, bestP1, bestP2, bestDist, bestTime);
							//// System.out.println("movimiento 1-0: " +
							//// solution);
							nonImproved = 0;
							break;
						} else
							nonImproved++;
					}
				}
			} else {
				// movimiento ENTRE rutas distintas:
				double[] bestDist = new double[] { solution.getRoute(r1).getDistance(),
						solution.getRoute(r2).getDistance() };
				double[] bestTime = new double[] { solution.getRoute(r1).getTime(), solution.getRoute(r2).getTime() };
				if (size1 - 2 <= 0) {
					nonImproved++;
					// System.out.println("eliminamos una Ruta");
					solution.removeRoute(r1);
				} else if (size2 - 2 <= 0) {
					nonImproved++;
					// System.out.println("eliminamos una Ruta");
					solution.removeRoute(r2);
				} else if (size2 > 3 && size1 > 3) {
					int bestS = -1;
					int bestD = -1;
					for (int start = 1; start < size1 - 1; start++) {
						for (int dst = 1; dst < size2 - 1; dst++) {
							double[] time = solution.evalTimeMoveK(r1, r2, start, start, dst);
							if (Math.max(time[0], time[1]) > WCPSolution.workingTime) {
								continue;
							}

							double[] distance = solution.evalMoveK(r1, r2, start, start, dst);
							double f1 = solution.getTotalDist() - solution.getRoute(r1).getDistance() + distance[0]
									- solution.getRoute(r2).getDistance() + distance[1];
							int LR = solution.longestRouteIf(r1, distance[0], solution.getRoute(r1).getDistance());
							double f2 = (LR == r1) ? distance[0] : solution.getRoute(LR).getDistance();
							LR = solution.longestRouteIf(r2, distance[1], solution.getRoute(LR).getDistance());
							f2 = (LR == r2) ? distance[1] : solution.getRoute(LR).getDistance();
							double f3 = solution.difTimeIf(r1, time[0], r2, time[1]);
							if (Pareto.checkDominance(f1, f2, f3, solution.getNumRoutes())) {
								WCPSolution newPareto = new WCPSolution(solution);
								newPareto.moveK(r1, r2, start, start, dst, distance, time);
								Pareto.add(newPareto);
							}
							double f = MultiLS_ref.distanceL2ToBest(f1, f2, f3, solution.getNumRoutes());
								if (f < bestF) {
									bestDist = distance;
									bestTime = time;
									bestS = start;
									bestD = dst;
									bestF = f;
								}
							}
					}
					if (bestS > 0) {
						solution.moveK(r1, r2, bestS, bestS, bestD, bestDist, bestTime);
						// System.out.println("tras movimiento nueva solución =
						// "+solution);
						nonImproved = 0;
						break;
					} else
						nonImproved++;
				}
			}
		}
	}
}
