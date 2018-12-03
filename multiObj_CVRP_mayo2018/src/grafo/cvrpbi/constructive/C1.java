package grafo.cvrpbi.constructive;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class C1 implements Constructive<WCPInstance, WCPSolution> {
	private class InsertCost {
		int[] pos;
		double[] cost;
		double[] dist;
		double[] time;

		public InsertCost(int r) {
			this.pos = new int[r];
			this.cost = new double[r];
			this.dist = new double[r];
			this.time = new double[r];
		}
	}

	private class Regret {
		int r;
		double regret;

		public Regret(int r, double regret) {
			this.r = r;
			this.regret = regret;
		}
	}

	private CRandom cRand;
	private double alpha;

	public C1(double alpha) {
		this.alpha = alpha;
		cRand = new CRandom();
	}

	@Override
	public WCPSolution constructSolution(WCPInstance instance) {
		Random rnd = RandomManager.getRandom();
		int nRoutes = WCPInstance.currentVehicles;
		WCPSolution sol = new WCPSolution(instance);
		// Candidate nodes
		int n = instance.getNodes();
		List<Integer> cl = new ArrayList<>(n);
		for (int i = 1; i < n; i++) {
			cl.add(i);
		}
		// Seed nodes
		for (int i = 0; i < nRoutes; i++) {
			int v = cl.remove(rnd.nextInt(cl.size()));
			sol.addNode(v, i);
		}
		// Complete routes
		InsertCost[] insertCosts = createInsertCost(sol, cl, n, nRoutes);
		int insertedByRegret = nRoutes + 1;
		int nodesPerRegret = (int) Math.ceil(n * ((alpha < 0) ? rnd.nextDouble() : alpha));
		while (!cl.isEmpty()) {
			if (insertedByRegret < nodesPerRegret) {
				int selected = -1;
				Regret maxRegret = null;
				int selectedPos = -1;
				int posCL = 0;
				for (int v : cl) {
					Regret reg = regret(sol, insertCosts, v);
					if ((reg.r >= 0) && ((maxRegret == null || maxRegret.regret < reg.regret))) {
						maxRegret = reg;
						selected = v;
						selectedPos = posCL;
					}
					posCL++;
				}
				if (selected < 0) {
					insertedByRegret = nodesPerRegret;
					continue;
				}
				cl.remove(selectedPos);
				sol.addNode(selected, maxRegret.r, insertCosts[selected].pos[maxRegret.r],
						insertCosts[selected].dist[maxRegret.r],insertCosts[selected].time[maxRegret.r]);
				insertedByRegret = insertedByRegret + 1;
				updateInsertCost(sol, cl, insertCosts, maxRegret.r);
			} else {
				double minCost = Double.MAX_VALUE;
				int minRoute = -1;
				int selected = -1;
				int selectedPos = -1;
				int posCL = 0;
				for (int v : cl) {
					for (int r = 0; r < nRoutes; r++) {
						if ((insertCosts[v].pos[r] > 0) && (insertCosts[v].cost[r] < minCost)) {
							minCost = insertCosts[v].cost[r];
							minRoute = r;
							selected = v;
							selectedPos = posCL;
						}
					}
					posCL++;
				}
				if (selected < 0) {
					return cRand.constructSolution(instance);
				}
				cl.remove(selectedPos);
				sol.addNode(selected, minRoute, insertCosts[selected].pos[minRoute],
						insertCosts[selected].dist[minRoute],insertCosts[selected].time[minRoute]);
				updateInsertCost(sol, cl, insertCosts, minRoute);
			}

		}
		sol.findLongestRoute();
		sol.findTimes();
		return sol;
	}

	private InsertCost[] createInsertCost(WCPSolution sol, List<Integer> cl, int n, int nRoutes) {
		InsertCost[] insertCost = new InsertCost[n];
		for (int v : cl) {
			insertCost[v] = new InsertCost(nRoutes);
			for (int r = 0; r < nRoutes; r++) {
				if (!sol.isFeasibleAdd(v, r)) {
					insertCost[v].pos[r] = -1;
					continue;
				}
				int size = sol.getRoute(r).size();
				insertCost[v].cost[r] = Double.MAX_VALUE;
				for (int p = 1; p < size; p++) {
					double extraTime = sol.evalTimeAddNode(v, r, p);
					if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
						double eval = sol.evalAddNode(v, r, p);
						if (eval < insertCost[v].cost[r]) {
							insertCost[v].cost[r] = eval;
							insertCost[v].pos[r] = p;
							insertCost[v].dist[r] = eval;
							insertCost[v].time[r] = extraTime;
						}
					}
				}
			}
		}
		return insertCost;
	}

	private void updateInsertCost(WCPSolution sol, List<Integer> cl, InsertCost[] insertCost, int r) {
		for (int v : cl) {
			if (!sol.isFeasibleAdd(v, r)) {
				insertCost[v].pos[r] = -1;
				continue;
			}
			int size = sol.getRoute(r).size();
			insertCost[v].cost[r] = Double.MAX_VALUE;
			for (int p = 1; p < size; p++) {
				double extraTime = sol.evalTimeAddNode(v, r, p);
				if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
					double eval = sol.evalAddNode(v, r, p);
					if (eval < insertCost[v].cost[r]) {
						insertCost[v].cost[r] = eval;
						insertCost[v].pos[r] = p;
						insertCost[v].dist[r] = eval;
						insertCost[v].time[r] = extraTime;
					}
				}
			}
		}
	}

	private Regret regret(WCPSolution sol, InsertCost[] ic, int v) {
		InsertCost icv = ic[v];
		double min1 = Double.MAX_VALUE;
		double min2 = Double.MAX_VALUE;
		Regret reg = new Regret(-1, -1);
		for (int i = 0; i < icv.cost.length; i++) {
			if (icv.pos[i] < 0) {
				continue;
			}
			if (!sol.isFeasibleAdd(v, i)) {
				icv.pos[i] = -1;
				continue;
			}
			if (icv.cost[i] < min1) {
				min2 = min1;
				min1 = icv.cost[i];
				reg.r = i;
			} else if (icv.cost[i] < min2) {
				min2 = icv.cost[i];
			}
		}
		reg.regret = min2 - min1;
		return reg;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + alpha + ")";
	}
}
