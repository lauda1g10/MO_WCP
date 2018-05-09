package grafo.cvrpbi.algorithms;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.Utilities;
import grafo.cvrpbi.structure.WCPInstance ;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

/**
 * Created by lauradelgado on 08/10/2017.
 */
public class VNS_ref_Multi implements Algorithm<WCPInstance > {

	private Algorithm<WCPInstance > alg;// necesita ser un algoritmo MULTI que
										// nos estime una frontera de pareto
	private Improvement<WCPSolution> ls;// necesita ser una BL _ref para
										// aplicar a una frontera de pareto
	private int iters;
	private WCPSolution best;
	private double kStep;
	private double kMax;

	public VNS_ref_Multi(Algorithm<WCPInstance > a, Improvement<WCPSolution> ls, int iters, double kStep, double kMax) {
		this.alg = a;
		this.ls = ls;
		this.kStep = kStep;
		this.kMax = kMax;
		this.iters = iters;
		Utilities.setRef(new double[] { 0, 0, 0, 0 });
	}

	@Override
	public Result execute(WCPInstance  instance) {
		best = null;
		Result r = new Result(instance.getName());
		Timer.initTimer();
		for (int k = instance.numVeh(); k < instance.getMaxVeh(); k++) {
			alg.execute(instance);
			File folder = new File("./pareto/" + instance.getName());
			if (!folder.exists()) {
				folder.mkdirs();
			}
			String path = "./pareto/" + instance.getName() + "/" + this.getClass().getSimpleName() + "route"
					+ instance.currentVehicles + ".txt";
			Pareto.saveToFile(path);
			boolean[] enter = new boolean[2];
			enter[0] = true;
			int paretoSize = Pareto.size();

			boolean terminate = true;
			while (terminate) {
				int p = 0;
				terminate = false;
				while (p < paretoSize - 1) {
					WCPSolution s1 = Pareto.getFrontAt(p);
					WCPSolution s2 = Pareto.getFrontAt(p + 1);
					// tomo como referencia el ideal de 2 soluciones consecutivas.
					double[] ref = { Math.min(s1.getTotalDist(), s2.getTotalDist()),
							Math.min(s1.getDistanceLongestRoute(), s2.getDistanceLongestRoute()),
							Math.min(s1.getDifTime(), s2.getDifTime()), instance.numVeh() };
					double[] refWorst = { Math.max(s1.getTotalDist(), s2.getTotalDist()),
							Math.max(s1.getDistanceLongestRoute(), s2.getDistanceLongestRoute()),
							Math.max(s1.getDifTime(), s2.getDifTime()), instance.getMaxVeh()};
					Utilities.setRef(ref);
					Utilities.setRefWorst(refWorst);
					// la BL sólo se la aplico a las soluciones "cercanas" en el
					// conjunto de approxToFront
					Set<WCPSolution> setSol = new HashSet<>(Pareto.getApproxToFront());
					Set<WCPSolution> analized = new HashSet<>();
					for (WCPSolution s : setSol) {
						if (Pareto.checkApproxBetween(s, s1, s2)) {
							analized.add(s);
							System.out.println("PARTIMOS DE: " + s);
							VNS(s);
						}
					}
					Pareto.clearApprox(analized);
					if (Pareto.size() != paretoSize) {
						// p = Math.max(0, p-1);
						terminate = true;
						paretoSize = Pareto.size();
					} else {
						p++;
					}
				}
			}
			WCPInstance.currentVehicles = WCPInstance.currentVehicles+1;
		}
		double secs = Timer.getTime() / 1000.0;
		System.out.println(Pareto.size() + "\t" + secs);
		r.add("Pareto", Pareto.size());
		r.add("Time (s)", secs);
		File folder = new File("./pareto/" + instance.getName());
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String path = "./pareto/" + instance.getName() + "/" + this.getClass().getSimpleName() + ".txt";
		Pareto.saveToFile(path);
		return r;
	}
	/*
	 * VNS
	 */

	private void VNS(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		boolean[] enter = new boolean[2];
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = new WCPSolution(sol);
			shake(sol, pertSize);
			ls.improve(s);
			// System.out.println("se podría mover al punto: "+s);
			if (Utilities.distanceToRefL2(s) < Utilities.distanceToRefL2(sol)) {
				enter[0] = Pareto.add(s);
				if (!enter[0]) {
					enter[1] = Pareto.addApprox(s);
				} else {
					enter[1] = Pareto.addApprox(s);
					break;
				}
			}
			if (best == null || Utilities.distanceToRefL2(s) < Utilities.distanceToRefL2(best)) {
				best = s;
			}

			k = k + kStep;
		}
	}

	private WCPSolution shake(WCPSolution s, int k) {
		WCPSolution sol = new WCPSolution(s);
		int nr = sol.getInstance().getVehicles();
		Random rnd = RandomManager.getRandom();
		int i = 0;
		while (i < k) {
			int r1 = rnd.nextInt(nr);
			int size1 = sol.getRoute(r1).size();
			int r2 = rnd.nextInt(nr);
			int size2 = sol.getRoute(r2).size();
			if (size1 < 3 || size2 < 3) {
				continue;
			}
			int p1 = 1 + rnd.nextInt(size1 - 2);
			int v1 = sol.getRoute(r1).getNodeAt(p1);
			int p2 = 1 + rnd.nextInt(size2 - 2);
			int v2 = sol.getRoute(r2).getNodeAt(p2);
			// ESta restricción es muy fuerte y no consigue los movimientos
			// oportunos en MUCHAS iteraciones.
			// if (r1 == r2 && v1 == v2 || !sol.isFeasibleInterchange(r1, v1,
			// r2, v2)) {
			// continue;
			// }
			// PROBAMOS con esta otra.
			if (r1 == r2 && v1 == v2) {
				continue;
			} else if (sol.isFeasibleInterchange(r1, v1, r2, v2)) {
				double[] time = sol.evalTimeInterchange(r1, r2, p1, p1, p2, p2);
				if (time[0] > WCPSolution.workingTime) {
					continue;
				}
				double eval[] = sol.evalInterchange(r1, r2, p1, p1, p2, p2);// Para
																			// que
																			// interchange
																			// me
																			// evalúe
																			// el
																			// intercambio
																			// de
																			// 1
																			// sólo
																			// nodo
																			// de
																			// cada
																			// ruta,
																			// el
																			// principio
																			// y
																			// final
																			// de
																			// las
																			// posiciones
																			// de
																			// la
																			// subcadena
																			// a
																			// evaluar
																			// debe
																			// ser
																			// el
																			// mismo.
				sol.moveInterchange(r1, r2, p1, p1, p2, p2, eval,time);
			}
			i++;
		}
		return sol;
	}

	@Override
	public Solution getBestSolution() {
		return best;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + alg + "," + ls + "," + iters + ")";
	}
}
