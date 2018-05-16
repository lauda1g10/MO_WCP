package grafo.cvrpbi.improvements.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.Utilities;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

/**
 * Created by lauradelgado on 08/10/2017.
 */
public class LSIntra2Opt2_ref implements Improvement<WCPSolution> {

	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		WCPInstance instance = solution.getInstance();
		int nRoutes = instance.getVehicles();
		List<Integer> routeIdx = new ArrayList<>(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			routeIdx.add(r);
		}
		double bestdist = Utilities.distanceToRefL2(solution);
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
				for (int start = 1; start < size - 2; start++) {
					for (int end = start + 1; end < size - 1; end++) {
						double extraTime = solution.evalTimeMove2Opt(r, start, end);
						if (extraTime <= WCPSolution.workingTime) {
							double distance = solution.evalMove2Opt(r, start, end);
							double f1 = solution.getTotalDist() - prevDistance + distance;
							int LR = solution.longestRouteIf(r, distance, solution.getRoute(r).getDistance());
							double f2 = (LR == r) ? distance : solution.getRoute(LR).getDistance();
							double f3 = solution.difTimeIf(r, extraTime);
							if (Pareto.checkDominance(f1, f2, f3, instance.currentVehicles)) {
								WCPSolution newPareto = new WCPSolution(solution);
								newPareto.move2Opt(r, start, end, distance, extraTime);
								if (!Pareto.add(newPareto)) {
									Pareto.addApprox(newPareto);
								}
							}
							double distEval = Utilities.distanceToRefL2(f1, f2, f3, instance.currentVehicles);
							if ((distEval - bestdist) < -WCPInstance.EPSILON) {
								bestDist = distance;
								bestStart = start;
								bestEnd = end;
								bestdist = distEval;
								bestTime = extraTime;
							}
						}
					}
					if (bestStart > 0) {
						solution.move2Opt(r, bestStart, bestEnd, bestDist, bestTime);
						Pareto.add(solution);
						improve = true;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
