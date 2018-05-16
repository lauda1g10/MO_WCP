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

/**
 * Created by lauradelgado on 08/10/2017.
 */
public class LSInterK_ref implements Improvement<WCPSolution> {

	private double kPercent;

	public LSInterK_ref(double kPercent) {
		this.kPercent = kPercent;
	}

	@Override
	public void improve(WCPSolution solution) {
		Random rnd = new Random(1234);
		WCPInstance instance = solution.getInstance();
		int nRoutes = instance.getVehicles();
		List<Integer> routesIdx1 = new ArrayList<>(nRoutes);
		List<Integer> routesIdx2 = new ArrayList<>(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			routesIdx1.add(r);
			routesIdx2.add(r);
		}
		double bestdisVal = Utilities.distanceToRefL2(solution);
		boolean improve = true;
		while (improve) {
			improve = false;
			Collections.shuffle(routesIdx1, rnd);
			Collections.shuffle(routesIdx2, rnd);
			for (int r1 : routesIdx1) {
				int r1Size = solution.getRoute(r1).size();
				for (int r2 : routesIdx2) {
					if (r1 == r2)
						continue;
					int r2Size = solution.getRoute(r2).size();
					double[] bestEval = { solution.getRoute(r1).getDistance(), solution.getRoute(r2).getDistance() };
					double[] bestTimes = { solution.getRoute(r1).getTime(), solution.getRoute(r2).getTime() };
					int bestP1 = -1;
					int bestP2 = -1;
					int bestK = -1;
					for (int k = 1; k < (int) Math.ceil(kPercent * r1Size); k++) {
						for (int p1 = 1; p1 < r1Size - k - 1; p1++) {
							List<Integer> subList = solution.getRoute(r1).getSubRoute(p1, p1 + k);
							if (!solution.isFeasibleAdd(subList, r2)) {
								continue;
							}
							for (int p2 = 1; p2 < r2Size; p2++) {
								double[] evalTime = solution.evalTimeMoveK(r1, r2, p1, p1 + k, p2);
								if (evalTime[0] <= WCPSolution.workingTime && evalTime[1] <= WCPSolution.workingTime) {
									double[] eval = solution.evalMoveK(r1, r2, p1, p1 + k, p2);
									double f1 = solution.getTotalDist() - solution.getRoute(r1).getDistance()
											- solution.getRoute(r2).getDistance() + eval[0] + eval[1];
									double f2;
									if (eval[0] > eval[1]) {// entonces r1
															// resulta ser más
															// larga y
															// evaluamos...
										int LR = solution.longestRouteIf(r1, eval[0],
												solution.getRoute(r1).getDistance());
										f2 = (LR == r1) ? eval[0] : solution.getRoute(LR).getDistance();
									} else {
										int LR = solution.longestRouteIf(r2, eval[1],
												solution.getRoute(r2).getDistance());
										f2 = (LR == r2) ? eval[1] : solution.getRoute(LR).getDistance();
									}
									double f3 = solution.difTimeIf(r1, evalTime[0], r2, evalTime[1]);
									if (Pareto.checkDominance(f1, f2, f3, nRoutes)) {
										WCPSolution newPareto = new WCPSolution(solution);
										newPareto.moveK(r1, r2, p1, p1 + k, p2, eval, evalTime);
										if (!Pareto.add(newPareto)) {
											Pareto.addApprox(newPareto);
										}
									}
									double disVal = Utilities.distanceToRefL2(f1, f2, f3, nRoutes);
									if (disVal - bestdisVal < -WCPInstance.EPSILON) {
										bestdisVal = disVal;
										bestP1 = p1;
										bestP2 = p2;
										bestEval = eval;
										bestTimes = evalTime;
										bestK = k;
									}
								}
							}
						}
						if (bestP1 > 0) {
							solution.moveK(r1, r2, bestP1, bestP1 + bestK, bestP2, bestEval, bestTimes);
							Pareto.add(solution);
							improve = true;
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + kPercent + ")";
	}
}
