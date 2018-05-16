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
public class C1_CombiRandom implements Constructive<WCPInstance, WCPSolution> {
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

	private static final int TOTAL_DIST = 0;
	private static final int LONGEST_ROUTE = 1;
	private static final int TIME = 2;

	private CRandom cRand;
	private double alpha;
	private int objIndex = TOTAL_DIST;

	public C1_CombiRandom(double alpha) {
		this.alpha = alpha;
		cRand = new CRandom();
	}

	@Override
	public WCPSolution constructSolution(WCPInstance instance) {
		Random rnd = RandomManager.getRandom();
		objIndex = rnd.nextInt(3);
		int nRoutes = instance.getVehicles();
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
		InsertCost[] insertCostsLR = createInsertCostLR(sol, cl, n, nRoutes);
		InsertCost[] insertCostsTimes = createInsertCostTimes(sol, cl, n, nRoutes);

		int insertedByRegret = nRoutes + 1;
		int nodesPerRegret = (int) Math.ceil(n * ((alpha < 0) ? rnd.nextDouble() : alpha));
		objIndex = TOTAL_DIST;

		while (!cl.isEmpty()) {
			if (insertedByRegret < nodesPerRegret) {
				int selected = -1;
				Regret maxRegret = null;
				int selectedPos = -1;
				int posCL = 0;
				for (int v : cl) {
					// Switch
					Regret reg = new Regret(v,0);
					switch (objIndex){
					case 0: reg = regret(sol,insertCosts,v);
					case 1: reg = regretLR(sol,insertCosts,v);
					case 2: reg = regretTimes(sol,insertCosts,v);
					}
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
				InsertCost[] selectedInsertCosts = new InsertCost[cl.size()];; 
				switch (objIndex){
				case 0:selectedInsertCosts = insertCosts;
				case 1:selectedInsertCosts = insertCostsLR;
				case 2:selectedInsertCosts = insertCostsTimes;
				}
				sol.addNode(selected, maxRegret.r, selectedInsertCosts[selected].pos[maxRegret.r],
						selectedInsertCosts[selected].dist[maxRegret.r]);
				insertedByRegret = insertedByRegret + 1;
				updateInsertCost(sol, cl, insertCosts, maxRegret.r);
				updateInsertCostLR(sol, cl, insertCostsLR, maxRegret.r);
			} else {
				double minCost = Double.MAX_VALUE;
				int minRoute = -1;
				int selected = -1;
				int selectedPos = -1;
				int posCL = 0;
				InsertCost[] selectedInsertCosts = new InsertCost[cl.size()]; 
				switch (objIndex){
				case 0:selectedInsertCosts = insertCosts;
				case 1:selectedInsertCosts = insertCostsLR;
				case 2:selectedInsertCosts = insertCostsTimes;
				}
				for (int v : cl) {
					for (int r = 0; r < nRoutes; r++) {
						if ((selectedInsertCosts[v].pos[r] > 0) && (selectedInsertCosts[v].cost[r] < minCost)) {
							minCost = selectedInsertCosts[v].cost[r];
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
				sol.addNode(selected, minRoute, selectedInsertCosts[selected].pos[minRoute],
						selectedInsertCosts[selected].dist[minRoute]);
				updateInsertCost(sol, cl, insertCosts, minRoute);
				updateInsertCostLR(sol, cl, insertCostsLR, minRoute);
				updateInsertCostTimes(sol,cl,insertCostsTimes,minRoute);
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
					double eval = sol.evalAddNode(v, r, p);
					if (eval < insertCost[v].cost[r]) {
						insertCost[v].cost[r] = eval;
						insertCost[v].dist[r] = eval;
						insertCost[v].pos[r] = p;
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
				double eval = sol.evalAddNode(v, r, p);
				if (eval < insertCost[v].cost[r]) {
					insertCost[v].cost[r] = eval;
					insertCost[v].dist[r] = eval;
					insertCost[v].pos[r] = p;
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

	private InsertCost[] createInsertCostLR(WCPSolution sol, List<Integer> cl, int n, int nRoutes) {
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
				insertCost[v].pos[r] = -1;
				for (int p = 1; p < size; p++) {
					double eval = sol.evalAddNode(v, r, p);
					int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
							sol.getRoute(r).getDistance());
					double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
					double ic = f2 - sol.getDistanceLongestRoute();
					if (ic < insertCost[v].cost[r]) {
						insertCost[v].cost[r] = ic;
						insertCost[v].pos[r] = p;
						insertCost[v].dist[r] = eval;
					}
				}
			}
		}
		return insertCost;
	}

	private void updateInsertCostLR(WCPSolution sol, List<Integer> cl, InsertCost[] insertCost, int r) {
		for (int v : cl) {
			if (!sol.isFeasibleAdd(v, r)) {
				insertCost[v].pos[r] = -1;
				continue;
			}
			int size = sol.getRoute(r).size();
			insertCost[v].cost[r] = Double.MAX_VALUE;
			insertCost[v].pos[r] = -1;
			for (int p = 1; p < size; p++) {
				double eval = sol.evalAddNode(v, r, p);
				int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
						sol.getRoute(r).getDistance());
				double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
				double ic = Math.max(0, f2 - sol.getDistanceLongestRoute());
				if (ic < insertCost[v].cost[r]) {
					insertCost[v].cost[r] = ic;
					insertCost[v].pos[r] = p;
					insertCost[v].dist[r] = eval;
				}
			}
		}
	}

	private Regret regretLR(WCPSolution sol, InsertCost[] ic, int v) {
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

	private InsertCost[] createInsertCostTimes(WCPSolution sol, List<Integer> cl, int n, int nRoutes) {
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
				insertCost[v].pos[r] = -1;
				for (int p = 1; p < size; p++) {
					double eval = sol.evalAddNode(v, r, p);
					int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
							sol.getRoute(r).getDistance());
					double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
					double ic = f2 - sol.getDistanceLongestRoute();
					if (ic < insertCost[v].cost[r]) {
						insertCost[v].cost[r] = ic;
						insertCost[v].pos[r] = p;
						insertCost[v].dist[r] = eval;
					}
				}
			}
		}
		return insertCost;
	}

	private void updateInsertCostTimes(WCPSolution sol, List<Integer> cl, InsertCost[] insertCost, int r) {
		for (int v : cl) {
			if (!sol.isFeasibleAdd(v, r)) {
				insertCost[v].pos[r] = -1;
				continue;
			}
			int size = sol.getRoute(r).size();
			insertCost[v].cost[r] = Double.MAX_VALUE;
			insertCost[v].pos[r] = -1;
			for (int p = 1; p < size; p++) {
				double extraTime = sol.evalTimeAddNode(v, r, p);
				if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
					double eval = sol.evalAddNode(v, r, p);
					double difT = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
					double ic = difT - sol.getDifTime();
					if (ic < insertCost[v].cost[r]) {
						insertCost[v].cost[r] = ic;
						insertCost[v].pos[r] = p;
						insertCost[v].dist[r] = eval;
						insertCost[v].time[r] = extraTime;
					}
				}
			}
		}
	}

	private Regret regretTimes(WCPSolution sol, InsertCost[] ic, int v) {
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
