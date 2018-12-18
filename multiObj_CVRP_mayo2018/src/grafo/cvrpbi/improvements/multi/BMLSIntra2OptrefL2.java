package grafo.cvrpbi.improvements.multi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

/**
 * Created by jesussanchezoro on 06/10/2017.
 */
public class BMLSIntra2OptrefL2 implements Improvement<WCPSolution> {

	private int maxIter = 10;
	
	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		int nonImproved = 0;
		int nRoutes = solution.getNumRoutes();
		List<Integer> routeIdx = new ArrayList<>(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			routeIdx.add(r);
		}
		double bestF = solution.distanceL2To(MultiLS_ref.getBestPoint());
		while (nonImproved<maxIter) {
			Collections.shuffle(routeIdx, rnd);
			for (int r : routeIdx) {
				int size = solution.getRoute(r).size();
				double bestTime = solution.getRoute(r).getTime();
				double bestDist = solution.getRoute(r).getDistance();
				double prevDistance = solution.getRoute(r).getDistance();
				int bestStart = -1;
				int bestEnd = -1;
				for (int start = 1; start < size - 2; start++) {
					for (int end = start + 2; end < size - 1; end++) {
						double extraTime = solution.evalTimeMove2Opt(r, start, end);
						if (extraTime <= WCPSolution.workingTime) {
							double distance = solution.evalMove2Opt(r, start, end);
							double f1 = solution.getTotalDist() - prevDistance + distance;
							int LR = solution.longestRouteIf(r, distance, solution.getRoute(r).getDistance());
							double f2 = (LR == r) ? distance : solution.getRoute(LR).getDistance();
							double f3 = solution.difTimeIf(r, extraTime);

							if (Pareto.checkDominance(f1, f2, f3, solution.getNumRoutes())) {
								WCPSolution newPareto = new WCPSolution(solution);
								newPareto.move2Opt(r, start, end, distance, extraTime);
								Pareto.add(newPareto);
							}
							double f = MultiLS_ref.distanceL2ToBest(f1, f2, f3, solution.getNumRoutes());
							if (f < bestF) {
									bestDist = distance;
									bestTime = extraTime;
									bestStart = start;
									bestEnd = end;
									bestF =f; 
								}
						}
					}
				}
				if (bestStart > 0) {
					solution.move2Opt(r, bestStart, bestEnd, bestDist, bestTime);
					//System.out.println("movimiento 2OPT: " + solution);
					Pareto.add(solution);
					nonImproved = 0;
				}
				else{
					nonImproved++;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "BMLSIntra2OptrefL2";
	}
}
