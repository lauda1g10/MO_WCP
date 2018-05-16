package grafo.cvrpbi.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import grafo.cvrpbi.structure.WCPInstance ;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Node;
import grafo.cvrpbi.structure.Pareto;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.tools.RandomManager;

public class PathRelinking_length extends AlgPathRelinking {

	public PathRelinking_length(Algorithm<WCPInstance > a) {
		super(a);
	}

	@Override
	protected void PathRelinking() {

		int runs = 0;
		while (runs < super.maxTries) {
			int rG = RandomManager.getRandom().nextInt(WCPInstance .currentVehicles);
			boolean finish = false;
			int fails = 0;
			while (!finish) {

				finish = true;
				int rI = super.findRouteCommonWith(rG);
				int eqPos = 0;
				while (initial.getRoute(rI).size() < guide.getRoute(rG).size()) {
					// inserto en rI
					boolean entra = false;
					for (int p = 1; p < guide.getRoute(rG).size() - 1; p++) {
						if (!initial.getRoute(rI).contains(guide.getRoute(rG).getNodeAt(p))) {
							int v = guide.getRoute(rG).getNodeAt(p);
							if (initial.isFeasibleAdd(v, rI)) {
								if (tryInsert(v, rI)) {
									entra = true;
									break;
								}
							}
						}
					}
					if (!entra) {// si la inserción no es factible
						fails++;
						int i = 0;
						while (i < super.maxTries) {
							int p = RandomManager.getRandom().nextInt(initial.getRoute(rI).size() - 1);
							if (!initial.getRoute(rI).contains(guide.getRoute(rG).getNodeAt(p))) {
								int v = guide.getRoute(rG).getNodeAt(p);
								int rT = this.nodeRouteI[v];
								if (rT != rI) {
									if (tryInterchangeP(v, rI, p)) {
										i = 0;
										break;
									} else {
										i++;
									}
								} else {
									i++;
								}
							} else {
								i++;
							}
						}
						if (fails > super.maxTries) {
							finish = true;
							break;
						}
					}
				}
				while (initial.getRoute(rI).size() > guide.getRoute(rG).size()) {
					// elimino de rI, empezando con aquellos nodos que no están
					// en
					// rG y tienen mayor carga:
					int v = -1;
					List<Integer> nodes = new ArrayList<>(
							initial.getRoute(rI).getSubRoute(1, initial.getRoute(rI).size() - 2));
					List<Node> nodesBYdemand = new ArrayList<>();
					for (int n : nodes) {
						nodesBYdemand.add(instance.getNode(n));
					}
					nodesBYdemand.sort(Comparator.comparingDouble(Node::getDemand)); // orden
																						// creciente.
					Collections.reverse(nodesBYdemand);// la pongo en orden
														// decreciente.
					boolean entra = false;
					for (int n = 0; n < nodesBYdemand.size(); n++) {
						if (!guide.getRoute(rG).contains(nodesBYdemand.get(n).getId())) {
							v = nodesBYdemand.get(n).getId();
							if (this.tryRemove(v, rI)) {
								entra = true;
								break;
							}
						}
					}
					if (!entra) {// si el movimiento no es factible
						// quiero intercambiar el nodo v de rI con otro nodo(vT)
						// de
						// OTRA ruta(rT)!
						// intento intercambiarlo con un nodo de la ruta con
						// menos
						// carga
						int rT = initial.findMinDemandRoute();
						for (int n = 0; n < nodesBYdemand.size(); n++) {
							if (!guide.getRoute(rG).contains(nodesBYdemand.get(n).getId())) {
								v = nodesBYdemand.get(n).getId();
								if (rT == rI) {
									for (int i = 0; i < super.maxTries; i++) {
										rT = RandomManager.getRandom().nextInt(WCPInstance .currentVehicles);
										for (int p = 1; p < initial.getRoute(rT).size() - 1; p++) {
											int vT = initial.getRoute(rT).getNodeAt(p); // ¿cómo
																						// lo
																						// escojo?
											if (rT != rI && initial.isFeasibleInterchange(rT, vT, rI, v)) {
												if (tryInterchange(v, rI, vT, rT)) {
													entra = true;
													break;
												}
											}
										}
										if (entra) {
											break;
										}
									}
									if (entra) {
										break;
									}
								} else {
									for (int p = 1; p < initial.getRoute(rT).size() - 1; p++) {
										int vT = initial.getRoute(rT).getNodeAt(p); // ¿cómo
																					// lo
																					// escojo?
										if (initial.isFeasibleInterchange(rI, v, rT, vT)) {
											if (tryInterchange(v, rI, vT, rT)) {
												entra = true;
												break;
											}
										}
									}
									if (entra) {
										break;
									}
								}
							}
						}
						if (entra) {
							break;
						} else {
							fails++;
							if (fails > super.maxTries - 1) {
								finish = true;
								break;
							}
						}
					}
				}
				if (initial.getRoute(rI).size() == guide.getRoute(rG).size()) {
					// hacemos movimientos para la igualdad de posiciones:
					boolean entra = false;
					for (int p = 1; p < guide.getRoute(rG).size() - 1; p++) {
						if (guide.getRoute(rG).getNodeAt(p) != initial.getRoute(rI).getNodeAt(p)) {
							int vQ = guide.getRoute(rG).getNodeAt(p); // Este
																		// es
																		// el
																		// nodo
																		// que
																		// QUIERO
																		// en
																		// la
																		// posición
																		// p
							int vT = initial.getRoute(rI).getNodeAt(p); // este
																		// es
																		// el
																		// nodo
																		// que
																		// TENGO
																		// en
																		// esta
																		// posición.
							if (initial.isFeasibleInterchange(this.nodeRouteI[vQ], vQ, rI, vT)) { // esta
																									// Functión
																									// evalúa
																									// si
																									// vQ
																									// cabe
																									// en
																									// rI
																									// y
																									// vT
																									// en
																									// la
																									// otra
																									// ruta.
								if (tryInterchangeP(vQ, rI, p)) {
									entra = true;// Esta
								}
								// Functión
								// mueve
								// (si
								// se
								// puede)
								// el
								// nodo
								// vQ
								// en
								// la
								// ruta
								// rI,
								// en
								// la
								// misma
								// posición.
							}

						} else {
							eqPos++;
						}
					}
					if (eqPos == guide.getRoute(rG).size() - 2) {
						finish = true;
					}
					if (!entra) {
						fails++;
						if (fails > super.maxTries - 1) {
							finish = true;
						}
					}

				}
			}
			runs++;
		}
	}

	private boolean tryInsert(int v, int r) {
		boolean modified = false;
		double[] bestEval = new double[2];
		double[] bestTime = new double[2];
		int bestP = -1;// indica la posición donde voy a incluir el nodo que
						// quito de rFROM
		// si rI NO contiene el nodo correspondiente, LO BUSCO en InitialSol
		int rFROM = super.nodeRouteI[v];
		int pFROM = super.nodePosI[v];
		double F = Double.MAX_VALUE;
		double f1;
		double f2;
		// y evalúo su inserción en todas las posiciones posibles.
		for (int p = 1; p < initial.getRoute(r).size() - 1; p++) {
			if (p != pFROM) {
				double[] evalTime = initial.evalTimeMoveK(rFROM, r, pFROM, pFROM, p);
				if (evalTime[0] <= WCPSolution.workingTime && evalTime[1] <= WCPSolution.workingTime) {
					double[] eval = initial.evalMoveK(rFROM, r, pFROM, pFROM, p);
					f1 = initial.getTotalDist() - initial.getRoute(r).getDistance()
							- initial.getRoute(rFROM).getDistance() + eval[0] + eval[1];
					f2 = initial.getDistanceLongestRoute();
					if (initial.getDistanceLongestRoute() < Math.max(eval[0], eval[1])) {
						f2 = Math.max(eval[0], eval[1]);
					}
					double f3 = initial.difTimeIf(rFROM, evalTime[0], r, evalTime[1]);
					if (Pareto.checkDominance(f1, f2, f3, initial.getNumRoutes())) {
						// comprobamos si lo podemos incluir en el conjunto de
						// no
						// dominadas.
						WCPSolution s = new WCPSolution(initial);
						s.moveK(rFROM, r, pFROM, pFROM, p, eval, evalTime);
						Pareto.add(s);
					}
					// según la Functión que esté optimizando, me quedo con una
					// u
					// otra:
					if (WCPSolution.currentOF == WCPSolution.ObjFunct.TOTAL_DIST) {
						if (f1 < F) {
							F = f1;
							bestEval = eval;
							bestTime = evalTime;
							bestP = p;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.LONGEST_ROUTE) {
						if (f2 < F) {
							F = f2;
							bestEval = eval;
							bestTime = evalTime;
							bestP = p;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.TIME) {
						if (f3 < F) {
							F = f3;
							bestEval = eval;
							bestTime = evalTime;
							bestP = p;
						}
					}
				}
			}
		}
		// cuando ya he analizado todas las posibilidades, devuelvo la
		// solución:
		if (bestP > 0) {
			initial.moveK(rFROM, r, pFROM, pFROM, bestP, bestEval, bestTime);
			this.updateNodeInRouteI(rFROM);
			modified = true;
			this.updateNodeInRouteI(r);
			this.updateNodeInRouteI(rFROM);
			this.updateSymDistanceM(r);
			this.updateSymDistanceM(rFROM);
		}
		return modified;
	}

	private boolean tryRemove(int v, int r) {
		// lo elimino de la solución actual (sé que v está en rI y lo introduzco
		// en la ruta con menor demanda total.
		int rTO = initial.findMinDemandRoute();
		// si rTO == r, lo meto en una ruta aleatoria en la que el movimiento
		// sea FACTIBLE.
		if (rTO == r) {
			for (int i = 0; i < super.maxTries; i++) {
				rTO = RandomManager.getRandom().nextInt(WCPInstance .currentVehicles);
				if (rTO != r && initial.isFeasibleAdd(v, rTO)) {
					return tryInsert(v, rTO);
				}
			}
		} else if (initial.isFeasibleAdd(v, rTO)) {
			return tryInsert(v, rTO);
		}
		return false;

	}

	private boolean tryInterchange(int v1, int r1, int v2, int r2) {
		// intercambiamos los nodos de ruta, pero NO se indica en qué posición.
		boolean modified = false;
		int bestP1 = -1;// dónde voy a insertar v2 en r1.
		int bestP2 = -1;// dónde voy a insertar v1 en r2.
		double[] bestEval = new double[2];
		double[] bestTime = new double[2];
		double F = Double.MAX_VALUE;
		double f1;
		double f2;
		for (int p1 = 1; p1 < initial.getRoute(r1).size() - 1; p1++) {
			for (int p2 = 1; p2 < initial.getRoute(r2).size() - 1; p2++) {
				double[] evalTime = initial.evalTimeInterchangeP(r1, r2, v1, p1, v2, p2);
				if (evalTime[0] <= WCPSolution.workingTime && evalTime[1] <= WCPSolution.workingTime) {
					double[] eval = initial.evalInterchangeP(r1, r2, v1, p1, v2, p2);
					f1 = initial.getTotalDist() - initial.getRoute(r1).getDistance()
							- initial.getRoute(r2).getDistance() + eval[0] + eval[1];
					f2 = initial.getDistanceLongestRoute();
					if (initial.getDistanceLongestRoute() < Math.max(eval[0], eval[1])) {
						f2 = Math.max(eval[0], eval[1]);
					}
					double f3 = initial.difTimeIf(r1, evalTime[0], r2, evalTime[1]);
					if (Pareto.checkDominance(f1, f2, f3, initial.getNumRoutes())) {
						// comprobamos si lo podemos incluir en el conjunto de
						// no
						// dominadas.
						WCPSolution s = new WCPSolution(initial);
						s.moveInterchangeP(r1, r2, v1, p1, v2, p2, eval, evalTime);
						Pareto.add(s);
					}
					// según la Functión que esté optimizando, me quedo con una
					// u
					// otra:
					if (WCPSolution.currentOF == WCPSolution.ObjFunct.TOTAL_DIST) {
						if (f1 < F) {
							F = f1;
							bestEval = eval;
							bestTime = evalTime;
							bestP1 = p1;
							bestP2 = p2;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.LONGEST_ROUTE) {
						if (f2 < F) {
							F = f2;
							bestEval = eval;
							bestTime = evalTime;
							bestP1 = p1;
							bestP2 = p2;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.TIME) {
						if (f3 < F) {
							F = f3;
							bestEval = eval;
							bestTime = evalTime;
							bestP1 = p1;
							bestP2 = p2;
						}
					}
				}
			}
		}
		if (bestP1 > 0) {
			initial.moveInterchangeP(r1, r2, v1, bestP1, v2, bestP2, bestEval, bestTime);
			modified = true;
			this.updateNodeInRouteI(r1);
			this.updateNodeInRouteI(r2);
			this.updateSymDistanceM(r1);
			this.updateSymDistanceM(r2);
		}
		return modified;
	}

	private boolean tryInterchangeP(int vQ, int rQ, int p) {
		int nRoutes = initial.getNumRoutes();
		// movemos el nodo vQ a la ruta rQ en la posición p.
		boolean modified = false;
		// dónde está vQ en la solución inicial?
		double[] bestEval = new double[2];
		double[] bestTime = new double[2];
		int rT = this.nodeRouteI[vQ];
		double f1;
		double f2;
		// quien está en ese sitio en la solución inicial?
		int vT = initial.getRoute(rQ).getNodeAt(p);

		if (rT == rQ) {
			int pT = this.nodePosI[vQ];
			// quiero intercambiar vQ con posición pT en rT con vT con posición
			// p en rQ.
			double[] evalTime = initial.evalTimeInterchange(rQ, rT, p, p, pT, pT);
			if (evalTime[0] <= WCPSolution.workingTime && evalTime[1] <= WCPSolution.workingTime) {
				double[] eval = initial.evalInterchange(rQ, rT, p, p, pT, pT);
				f1 = initial.getTotalDist() - initial.getRoute(rQ).getDistance() + eval[0];
				f2 = initial.getDistanceLongestRoute();
				if (initial.getDistanceLongestRoute() < eval[0]) {
					f2 = eval[0];
				}
				double f3 = initial.difTimeIf(rQ, evalTime[0], rT, evalTime[1]);
				// comprobamos si lo podemos incluir en el conjunto de no
				// dominadas.
				if (Pareto.checkDominance(f1, f2, f3, nRoutes)) {
					WCPSolution s = new WCPSolution(initial);
					s.moveInterchange(rQ, rT, p, p, pT, pT, eval, evalTime);
					Pareto.add(s);
				}
				initial.moveInterchange(rQ, rT, p, p, pT, pT, eval, evalTime);
				modified = true;
				this.updateNodeInRouteI(rQ);
				this.updateNodeInRouteI(rT);
				this.updateSymDistanceM(rQ);
				this.updateSymDistanceM(rT);
			}
		} else {
			// si las rutas son distintas, evalúo el intercambio de vQ a la
			// posición p de rQ, la otra posición me da igual...la de menor
			// coste de inserción
			int bestP = -1;
			double F = Double.MAX_VALUE;
			for (int pT = 1; pT < initial.getRoute(rT).size() - 1; pT++) {
				double[] evalTime = initial.evalTimeInterchangeP(rQ, rT, vT, p, vQ, pT);
				if (evalTime[0] <= WCPSolution.workingTime && evalTime[1] <= WCPSolution.workingTime) {
					double[] eval = initial.evalInterchangeP(rQ, rT, vT, p, vQ, pT);
					f1 = initial.getTotalDist() - initial.getRoute(rQ).getDistance()
							- initial.getRoute(rT).getDistance() + eval[0] + eval[1];
					f2 = initial.getDistanceLongestRoute();
					if (initial.getDistanceLongestRoute() < Math.max(eval[0], eval[1])) {
						f2 = Math.max(eval[0], eval[1]);
					}
					double f3 = initial.difTimeIf(rQ, evalTime[0], rT, evalTime[1]);
					// comprobamos si lo podemos incluir en el conjunto de no
					// dominadas.
					if (Pareto.checkDominance(f1, f2, f3, nRoutes)) {
						WCPSolution s = new WCPSolution(initial);
						s.moveInterchangeP(rQ, rT, vT, p, vQ, pT, eval, evalTime);
						Pareto.add(s);
					}
					// según la Functión que esté optimizando, me quedo con una
					// u
					// otra:
					if (WCPSolution.currentOF == WCPSolution.ObjFunct.TOTAL_DIST) {
						if (f1 < F) {
							F = f1;
							bestEval = eval;
							bestTime = evalTime;
							bestP = pT;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.LONGEST_ROUTE) {
						if (f2 < F) {
							F = f2;
							bestEval = eval;
							bestTime = evalTime;
							bestP = pT;
						}
					} else if (WCPSolution.currentOF == WCPSolution.ObjFunct.TIME) {
						if (f3 < F) {
							F = f3;
							bestEval = eval;
							bestTime = evalTime;
							bestP = pT;
						}
					}
				}
			}
			if (bestP > 0) {
				initial.moveInterchangeP(rQ, rT, vT, p, vQ, bestP, bestEval, bestTime);
				modified = true;
				this.updateNodeInRouteI(rQ);
				this.updateNodeInRouteI(rT);
				this.updateSymDistanceM(rQ);
				this.updateSymDistanceM(rT);
			}
		}
		return modified;
	}
}
