package grafo.cvrpbi.improvements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

/**
 * Created by lauradelgado on 08/10/2017.
 */
public class LSIntraMoveK implements Improvement<WCPSolution> {
	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		WCPInstance instance = solution.getInstance();
		int nRoutes = instance.getVehicles();
		List<Integer> routeIdx = new ArrayList<>(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			routeIdx.add(r);
		}
		double bestF = solution.getOF();
		boolean improve = true;
		while (improve) {
			improve = false;
			Collections.shuffle(routeIdx, rnd);
			for (int r : routeIdx) {
				int size = solution.getRoute(r).size();
				double bestTime = solution.getRoute(r).getTime();
				double bestDist = solution.getRoute(r).getDistance();
				double prevDistance = solution.getRoute(r).getDistance();
				int bestStart = -1;
				int bestEnd = -1;
				int bestDst = -1;
				for (int start = 1; start < size - 1; start++) {
					for (int end = start; end < size - 1; end++) {
						for (int dst = 1; dst < size; dst++) {
							if (dst >= start - 1 && dst <= end + 1) {
								continue;
							}
							double extraTime = solution.evalTimeMoveSubRoute(r, start, end, dst);
							if (extraTime <= WCPSolution.workingTime) {
								double distance = solution.evalMoveSubRoute(r, start, end, dst);
								double f1 = solution.getTotalDist() - prevDistance + distance;
								int LR = solution.longestRouteIf(r, distance, solution.getRoute(r).getDistance());
								double f2 = (LR == r) ? distance : solution.getRoute(LR).getDistance();
								double f3 = solution.difTimeIf(r, extraTime);

								if (Pareto.checkDominance(f1, f2, f3, nRoutes)) {
									WCPSolution newPareto = new WCPSolution(solution);
									newPareto.move2Opt(r, start, end, distance, extraTime);
									if (!Pareto.add(newPareto)) {
										Pareto.addApprox(newPareto);
									}
								}
								if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
									if (f1 < bestF) {
										bestF = f1;
										bestDist = distance;
										bestTime = extraTime;
										bestStart = start;
										bestEnd = end;
										bestDst = dst;
									}
								} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
									if (f2 < bestF) {
										bestF = f2;
										bestDist = distance;
										bestTime = extraTime;
										bestStart = start;
										bestEnd = end;
										bestDst = dst;
									}
								} else if (WCPSolution.currentOF == ObjFunct.TIME) {
									if (f3 < bestF) {
										bestDist = distance;
										bestTime = extraTime;
										bestStart = start;
										bestEnd = end;
										bestDst = dst;
									}
								}
							}
						}
					}
				}

				if (bestStart > 0) {
					solution.moveSubRoute(r, bestStart, bestEnd, bestDst, bestDist, bestTime);
					Pareto.add(solution);
					improve = true;
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
