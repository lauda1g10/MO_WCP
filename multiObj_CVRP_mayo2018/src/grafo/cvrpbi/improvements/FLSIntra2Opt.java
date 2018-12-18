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
public class FLSIntra2Opt implements Improvement<WCPSolution> {
	private int maxIter = 5;
	@Override
	public void improve(WCPSolution solution) {
		Random rnd = RandomManager.getRandom();
		int nRoutes = solution.getNumRoutes();
		List<Integer> routeIdx = new ArrayList<>(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			routeIdx.add(r);
		}
		boolean entra = true;
		double bestF = solution.getOF();
		int nonImproved = 0;
		while (entra && nonImproved<maxIter) {
			Collections.shuffle(routeIdx, rnd);
			for (int r : routeIdx) {
				int size = solution.getRoute(r).size()-1;
				if (size<3){
					entra = false;
					break;
				}
				double prevDistance = solution.getRoute(r).getDistance();
				int start = rnd.nextInt(size - 1)+1;
				int end = rnd.nextInt(size - start)+start-1;
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
							solution.move2Opt(r, start, end, distance, extraTime);
							entra = false;
							break;
							}
						else{
							nonImproved++;
						}
						} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
								if (f2 < bestF) {
									solution.move2Opt(r, start, end, distance, extraTime);
									entra = false;
									break;
									}
								else{
									nonImproved++;
								}
							} else if (WCPSolution.currentOF == ObjFunct.TIME) {
								if (f3 < bestF) {
									solution.move2Opt(r, start, end, distance, extraTime);
									entra = false;
									break;
									}
								else{
									nonImproved++;
								}
							}else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
								double f = C1_WCK.evalWierzbicki(f1, f2, f3,solution.getNumRoutes());
								if ( f < bestF) {
									solution.move2Opt(r, start, end, distance, extraTime);
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

	@Override
	public String toString() {
		return "FLS_2opt";
	}
}
