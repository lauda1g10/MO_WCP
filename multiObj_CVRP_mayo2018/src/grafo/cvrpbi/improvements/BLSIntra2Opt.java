package grafo.cvrpbi.improvements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

/**
 * Created by jesussanchezoro on 06/10/2017.
 */
public class BLSIntra2Opt implements Improvement<WCPSolution> {

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
		double bestF = solution.getOF();
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
								if (!Pareto.add(newPareto)) {
									Pareto.addApprox(newPareto);
								}
							}
							if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
								if (f1<bestF) {
									bestF = f1;
									bestDist = distance;
									bestTime = extraTime;
									bestStart = start;
									bestEnd = end;
								}
							} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
								if (f2 < bestF) {
									bestF = f2;
									bestDist = distance;
									bestTime = extraTime;
									bestStart = start;
									bestEnd = end;
								}
							} else if (WCPSolution.currentOF == ObjFunct.TIME) {
								if (f3 < bestF) {
									bestF = f3;
									bestDist = distance;
									bestTime = extraTime;
									bestStart = start;
									bestEnd = end;
								}
							}else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
								double f = C1_WCK.evalWierzbicki(f1, f2, f3,solution.getNumRoutes());
								if ( f < bestF) {
									bestDist = distance;
									bestTime = extraTime;
									bestStart = start;
									bestEnd = end;
									bestF =f; 
								}
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
		return "BLS_2opt";
	}
}
