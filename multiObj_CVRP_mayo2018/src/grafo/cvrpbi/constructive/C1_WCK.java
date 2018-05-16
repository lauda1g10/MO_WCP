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
public class C1_WCK implements Constructive<WCPInstance, WCPSolution> {

	private static double rho = 0.0001;

	private class InsertCost {
		int[] pos;
		double[] dist;
		double[] time;
		double[] value;

		public InsertCost(int r) {
			this.pos = new int[r];
			this.dist = new double[r];
			this.time = new double[r];
			this.value = new double[r];
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
	public static double lambda[];
	public static double ideal[];
	public static double nadir[];
	
	public C1_WCK(double alpha) {
		lambda = new double[4];
		ideal = new double[4];
		nadir = new double[4];
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
					if ((reg.r >= 0)) {
						if (maxRegret == null || maxRegret.regret < reg.regret) {
							maxRegret = reg;
							selected = v;
							selectedPos = posCL;
						} else if (maxRegret.regret == reg.regret) {
							if (selected < 0
									|| instance.getNode(v).getDemand() > instance.getNode(selected).getDemand()) {
								maxRegret = reg;
								selected = v;
								selectedPos = posCL;
							}
						}
					}
					posCL++;
				}
				if (selected < 0) {
					insertedByRegret = nodesPerRegret;
					continue;
				}
				cl.remove(selectedPos);
				sol.addNode(selected, maxRegret.r, insertCosts[selected].pos[maxRegret.r],
						insertCosts[selected].dist[maxRegret.r], insertCosts[selected].time[maxRegret.r]);
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
						if ((insertCosts[v].pos[r] >= 0) && (insertCosts[v].value[r] < minCost)) {
							minCost = insertCosts[v].value[r];
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
						insertCosts[selected].dist[minRoute], insertCosts[selected].time[minRoute]);
				updateInsertCost(sol, cl, insertCosts, minRoute);
			}
		}

		return sol;
	}

	public static double evalWierzbicki(double f1, double f2, double f3, int f4) {
		return Math.max(Math.max(lambda[0] * (f1-ideal[0])/(nadir[0]-ideal[0]), lambda[1] * (f2-ideal[1])/(nadir[1]-ideal[1])),(1-(lambda[0]+lambda[1]))*((f3-ideal[2])/(nadir[2]-ideal[2])))
				+ rho * ((f1-ideal[0])/(nadir[0]-ideal[0]) + (f2-ideal[1])/(nadir[1]-ideal[1]) + (f3-ideal[2])/(nadir[2]-ideal[2])+(f4-ideal[3])/(nadir[3]-ideal[3]));
	}

	public static double getRho() {
		return rho;
	}

	public static void setRho(double r) {
		rho = r;
	}
public static void setIdeal(double[] i){
	ideal= i;
}
public static void setNadir(double[] i){
	nadir = i;
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
				insertCost[v].value[r] = Double.MAX_VALUE;
				insertCost[v].pos[r] = -1;
				for (int p = 1; p < size; p++) {
					double extraTime = sol.evalTimeAddNode(v, r, p);
					if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
						double eval = sol.evalAddNode(v, r, p);
						double f1 = sol.getTotalDist() + eval;
						int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
								sol.getRoute(r).getDistance());
						double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
						double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
						double ic = evalWierzbicki(f1, f2, f3, sol.getNumRoutes())
								- evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(), sol.getDifTime(),sol.getNumRoutes());
						if (ic < insertCost[v].value[r]) {
							insertCost[v].value[r] = ic;
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
			insertCost[v].value[r] = Double.MAX_VALUE;
			insertCost[v].pos[r] = -1;
			for (int p = 1; p < size; p++) {
				double extraTime = sol.evalTimeAddNode(v, r, p);
				if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
					double eval = sol.evalAddNode(v, r, p);
					double f1 = sol.getTotalDist() + eval;
					int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
							sol.getRoute(r).getDistance());
					double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
					double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
					double ic = evalWierzbicki(f1, f2, f3,sol.getNumRoutes())
							- evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(), sol.getDifTime(),sol.getNumRoutes());
					if (ic < insertCost[v].value[r]) {
						insertCost[v].value[r] = ic;
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
		for (int i = 0; i < icv.value.length; i++) {
			if (icv.pos[i] < 0) {
				continue;
			}
			if (!sol.isFeasibleAdd(v, i)) {
				icv.pos[i] = -1;
				continue;
			}
			if (icv.value[i] < min1) {
				min2 = min1;
				min1 = icv.value[i];
				reg.r = i;
			} else if (icv.value[i] < min2) {
				min2 = icv.value[i];
			}
		}
		reg.regret = min2 - min1;
		return reg;
	}

	public void setLambda(double l1, double l2) {
		lambda[0] = l1;
		lambda[1] = l2;
		lambda[1] = (1-lambda[0])*(lambda[1]-1)+(1-lambda[0]);//normalizamos lambda2
		lambda[2] = 1-(lambda[0]+lambda[1]);
	}

	public void setIdeal1(double i1) {
		ideal[0] = i1;
	}

	public void setIdeal2(double i2) {
		ideal[1] = i2;
	}
	public void setIdeal3(double i3) {
		ideal[2] = i3;
	}

	public void setLambda(){
	Random rnd = RandomManager.getRandom();
	double suma = 0;
	for(int i = 0; i<3; i++){
	lambda[i] = rnd.nextDouble();
	suma+=lambda[i];}
	for(int i = 0; i<3; i++){
		lambda[i] = lambda[i]/suma;}
}
	public double getLambda1() {
		return lambda[0];
	}
	public double getLambda2() {
		return lambda[1];
	}

	public double getLambda3() {
		return 1-lambda[0]-lambda[1];
	}
	public double getIdeal1() {
		return ideal[0];
	}

	public double getIdeal2() {
		return ideal[1];
	}
	public double getIdeal3() {
		return ideal[2];
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + alpha + ")";
	}
}
