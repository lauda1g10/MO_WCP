package grafo.cvrpbi.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.improvements.LSInterK;
import grafo.cvrpbi.improvements.LSIntra2Opt;
import grafo.cvrpbi.improvements.VNS;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.Timer;

/**
 * Created by LauraDelgado on 27/10/2017.
 */
public class AlgConstWCK_Multi_LS implements Algorithm<WCPInstance_RealInstance> {

	private C1_WCK c;
	private Constructive<WCPInstance_RealInstance, WCPSolution> c1;
	private Constructive<WCPInstance_RealInstance, WCPSolution> c2;
	private Constructive<WCPInstance_RealInstance, WCPSolution> c3;
	private Improvement<WCPSolution> ls;
	private int iters;
	private WCPSolution best;
	private final double ratio = 1.15;
	private int lambdaInterval;

	public AlgConstWCK_Multi_LS(C1_WCK c, Constructive<WCPInstance_RealInstance, WCPSolution> c1,
			Constructive<WCPInstance_RealInstance, WCPSolution> c2, Constructive<WCPInstance_RealInstance, WCPSolution> c3,
			Improvement<WCPSolution> ls, int iters, int lambdaInterval) {
		this.c = c;
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.ls = ls; //ha de ser WCK
		this.iters = iters;
		this.lambdaInterval = lambdaInterval;
	}

	@Override
	public Result execute(WCPInstance_RealInstance instance) {
		best = null;
		Result r = new Result(instance.getName());
		Pareto.reset();
		System.out.print(instance.getName() + "\t");
		Timer.initTimer();
		// Initial values for ideal1&2&3
		VNS vns = new VNS(
				new Improvement[] { new LSInterK(0.01), new LSIntra2Opt(), new LSInterK(0.01)}, 0.05);
		
		double[] min = new double[4];
		double[] max = new double[4];
		for (int k = instance.numVeh(); k < instance.getMaxVeh(); k++) {
		// Initial values for ideal1&2&3
			WCPSolution[] sols = new WCPSolution[4];
			Set<WCPSolution> bests = new HashSet<WCPSolution>();
		WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
		sols[0] = null;
		for (int i = 0; i < iters; i++) {
			WCPSolution sol = (WCPSolution) c1.constructSolution(instance);
			if (sol != null) {
				if (sols[0] == null) {
					sols[0] = sol;
				} else {
					if (sols[0].getOF() > sol.getOF()) {
						sols[0] = sol;
						bests.removeIf(u -> u.getOF() < ratio * sols[0].getOF());
					} else if (sols[0].getOF() * ratio > sol.getOF()) {
						bests.add(sol);
					}
					if (!Pareto.add(sol)) {
						Pareto.addApprox(sol);
					}
				}
			}
		}
		for (WCPSolution s : bests) {
			vns.improve(s);
			if (s.getOF() < sols[0].getOF()) {
				sols[0] = s;
			}
		}
		if (sols[0] != null) {
			System.out.println("Solución 1 = " + sols[0]);
			bests.clear();
			WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
			sols[1] = null;
			for (int i = 0; i < iters; i++) {
				WCPSolution sol = (WCPSolution) c2.constructSolution(instance);
				if (sol != null) {
					if (sols[1] == null) {
						sols[1] = sol;
					} else {
						if (sols[1].getOF() > sol.getOF()) {
							sols[1] = sol;
							bests.removeIf(u -> u.getOF() < ratio * sols[1].getOF());
						} else if (sols[1].getOF() * ratio > sol.getOF()) {
							bests.add(sol);
						}
						if (!Pareto.add(sol)) {
							Pareto.addApprox(sol);
						}
					}
				}
			}
			for (WCPSolution s : bests) {
				vns.improve(s);
				if (s.getOF() < sols[1].getOF()) {
					sols[1] = s;
				}
			}
			System.out.println("Solución 2 = " + sols[1]);
			if (sols[1] != null) {
				bests.clear();
				WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
				sols[2] = null;
				for (int i = 0; i < iters; i++) {
					WCPSolution sol = (WCPSolution) c3.constructSolution(instance);
					if (sol != null) {
						if (sols[2] == null) {
							sols[2] = sol;
						} else {
							if (sols[2].getOF() > sol.getOF()) {
								sols[2] = sol;
								bests.removeIf(u -> u.getOF() < ratio * sols[2].getOF());
							} else if (sols[2].getOF() * ratio > sol.getOF()) {
								bests.add(sol);
							}
							if (!Pareto.add(sol)) {
								Pareto.addApprox(sol);
							}
						}
					}
				}
				for (WCPSolution s : bests) {
					vns.improve(s);
					if (s.getOF() < sols[2].getOF()) {
						sols[2] = s;
					}
				}
				System.out.println("Solución 3 = " + sols[2]);
				if (sols[2] != null) {
					bests.clear();
				}
			}
		}
		if (Pareto.size() > 0) {
			if (sols[0] == null) {
				sols[0] = Pareto.getFrontAt(0);
			}
			if (sols[1] == null) {
				sols[1] = Pareto.getFrontAt(0);
			}
			if (sols[2] == null) {
				sols[2] = Pareto.getFrontAt(0);
			}
			for (int p = 1; p < Pareto.size(); p++) {
				if (Pareto.getFrontAt(p).getTotalDist() < sols[0].getTotalDist()) {
					sols[0] = Pareto.getFrontAt(p);
				}
				if (Pareto.getFrontAt(p).getDistanceLongestRoute() < sols[1].getDistanceLongestRoute()) {
					sols[1] = Pareto.getFrontAt(p);
				}
				if (Pareto.getFrontAt(p).getDifTime() < sols[2].getDifTime()) {
					sols[2] = Pareto.getFrontAt(p);
				}
			}
			min[0] = Math.min(Math.min(sols[0].getTotalDist(), sols[1].getTotalDist()), sols[2].getTotalDist());
			max[0] = Math.max(Math.max(sols[0].getTotalDist(), sols[1].getTotalDist()), sols[2].getTotalDist());
			min[1] = Math.min(Math.min(sols[0].getDistanceLongestRoute(), sols[1].getDistanceLongestRoute()),
					sols[2].getDistanceLongestRoute());
			max[1] = Math.max(Math.max(sols[0].getDistanceLongestRoute(), sols[1].getDistanceLongestRoute()),
					sols[2].getDistanceLongestRoute());
			min[2] = Math.min(Math.min(sols[0].getDifTime(), sols[1].getDifTime()), sols[2].getDifTime());
			max[2] = Math.max(Math.max(sols[0].getDifTime(), sols[1].getDifTime()), sols[2].getDifTime());
			min[3] = instance.numVeh();
			max[3] = instance.getMaxVeh();
			C1_WCK.setIdeal(min);
			// System.out.print(min);
			C1_WCK.setNadir(max);
			// System.out.print(max);
			// ahora podemos aplicar las BL

			for (int i = 0; i < lambdaInterval; i++) {
				c.setLambda();
				constructN(instance, iters);
			}
		}
		WCPInstance.currentVehicles = WCPInstance.currentVehicles+1;
		}
		double secs = Timer.getTime() / 1000.0;
		System.out.println("Soluciones de Pareto = " + Pareto.size());
		System.out.println(C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(), best.getDifTime(),best.getNumRoutes())
				+ "\t" + secs);
		r.add("TotalDist",
				C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(), best.getDifTime(),best.getNumRoutes()));
		r.add("Time (s)", secs);
		File folder = new File("./pareto/"+ instance.getName());
		if (!folder.exists()) {
			folder.mkdirs();
			}
		String path = "./pareto/" + instance.getName() +"/"+this.getClass().getSimpleName()+".txt";
		Pareto.saveToFile(path);
		return r;
	}

	private WCPSolution constructN(WCPInstance_RealInstance instance, int n) {
		best = null;
		List<WCPSolution> bestSols = new ArrayList<WCPSolution>();
		for (int i = 0; i < iters; i++) {
			WCPSolution sol = c.constructSolution(instance);
			if (sol == null)
				continue;
			else {
				if (!Pareto.add(sol)) {
					Pareto.addApprox(sol);
				}

				if (best == null || C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),
						sol.getDifTime(),sol.getNumRoutes()) < C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),
								best.getDifTime(),best.getNumRoutes())) {
					best = sol;
					bestSols.removeIf(u -> C1_WCK.evalWierzbicki(u.getTotalDist(), u.getDistanceLongestRoute(),
							u.getDifTime(),u.getNumRoutes()) < ratio * C1_WCK.evalWierzbicki(best.getTotalDist(),
									best.getDistanceLongestRoute(), best.getDifTime(),best.getNumRoutes()));
				} else if (best != null && C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),
						sol.getDifTime(),sol.getNumRoutes()) < ratio * C1_WCK.evalWierzbicki(best.getTotalDist(),
								best.getDistanceLongestRoute(), best.getDifTime(),best.getNumRoutes())) {
					bestSols.add(sol);
				}
			}
		}
		for (WCPSolution s : bestSols) {
			ls.improve(s);
			if (best == null
					|| C1_WCK.evalWierzbicki(s.getTotalDist(), s.getDistanceLongestRoute(), s.getDifTime(),s.getNumRoutes()) < C1_WCK
							.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(), best.getDifTime(),best.getNumRoutes())) {
				best = s;
			}
		}
		return best;
	}

	@Override
	public Solution getBestSolution() {
		return best;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + c + "," + ls + "," + iters + ")";
	}
}
