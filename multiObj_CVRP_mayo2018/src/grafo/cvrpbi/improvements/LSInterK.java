package grafo.cvrpbi.improvements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.cvrpbi.structure.Pareto;
import grafo.optilib.metaheuristics.Improvement;

/**
 * Created by jesussanchezoro on 06/10/2017. MODIFICADO: LauraDelgadoAntequera
 * on 28/10/17 el input del constructor define la longitud máxima de la cadena a
 * mover, pero el estudio de intercambios se realiza partiendo desde el menor
 * tamaño (k=1)
 */
public class LSInterK implements Improvement<WCPSolution> {

	private double kMaxPercent;

	public LSInterK(double kPercent) {
		this.kMaxPercent = kPercent;
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
		boolean improve = true;
		double bestF = solution.getOF();
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
					double[] bestTime = { solution.getRoute(r1).getTime(), solution.getRoute(r2).getTime() };
					int bestP1 = -1;
					int bestP2 = -1;
					int bestK = -1;
					int max = (int) Math.ceil(kMaxPercent * r1Size);
					for (int k = 1; k < max; k++) {
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
									if (Pareto.checkDominance(f1, f2, f3, solution.getNumRoutes())) {
										WCPSolution newPareto = new WCPSolution(solution);
										newPareto.moveK(r1, r2, p1, p1 + k, p2, eval, evalTime);
										if (!Pareto.add(newPareto)) {
											Pareto.addApprox(newPareto);
										}
									}
									if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
										if ((eval[0] + eval[1]) - (bestEval[0] + bestEval[1]) < -WCPInstance.EPSILON) {
											bestEval = eval;
											bestTime = evalTime;
											bestP1 = p1;
											bestP2 = p2;
											bestK = k;
										}
									} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
										if (f2 < bestF) {
											bestF = f2;
											bestEval = eval;
											bestTime = evalTime;
											bestP1 = p1;
											bestP2 = p2;
											bestK = k;
										}
									} else if (WCPSolution.currentOF == ObjFunct.TIME) {
										if (f3 < bestF) {
											bestF = f3;
											bestEval = eval;
											bestTime = evalTime;
											bestP1 = p1;
											bestP2 = p2;
											bestK = k;
										}
									}
								}
							}
						}
					}
					if (bestP1 > 0) {
						solution.moveK(r1, r2, bestP1, bestP1 + bestK, bestP2, bestEval,bestTime);
						improve = true;
						bestF = solution.getOF();
						break;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + kMaxPercent + ")";
	}
}
