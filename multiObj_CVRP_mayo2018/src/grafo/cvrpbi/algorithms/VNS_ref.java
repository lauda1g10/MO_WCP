package grafo.cvrpbi.algorithms;

import java.util.List;
import java.util.Random;
import java.util.Set;

import grafo.cvrpbi.improvements.multi.MultiLS_ref;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

public abstract class VNS_ref implements Algorithm<WCPInstance> {
	protected Algorithm<WCPInstance> alg; // debe ser un algoritmo que genere
	// una
	// frontera de pareto.
	protected WCPInstance instance;
	private final int MAX = 10;
	protected Improvement<WCPSolution>[] ls;

	public VNS_ref(Algorithm<WCPInstance> a) {
		this.alg = a;
		this.ls = MultiLS_ref.getLS();
	}

	public String getAlgName() {
		return this.alg.toString();
	}

	protected abstract boolean improvement(WCPSolution s); // devuelve TRUE si
															// se ha modificado
															// la frontera de
															// Pareto.

	protected void VNS(List<WCPSolution> paretoSet) {
		Random rd = RandomManager.getRandom();
		int sizeP = paretoSet.size();
		int iter = 0;
		while (sizeP > 1 && iter < MAX) {
			int index1 = rd.nextInt(sizeP);
			int index2 = rd.nextInt(sizeP);
			while (index1 == index2) {
				index2 = rd.nextInt(sizeP);
			}
			WCPSolution s1 = paretoSet.get(index1);
			WCPSolution s2 = paretoSet.get(index2);
			double[] ref = new double[4];
			ref[0] = Math.min(s1.getTotalDist(), s2.getTotalDist());
			ref[1] = Math.min(s1.getDistanceLongestRoute(), s2.getDistanceLongestRoute());
			ref[2] = Math.min(s1.getDifTime(), s2.getDifTime());
			ref[3] = Math.min(s1.getNumRoutes(), s2.getNumRoutes());

			MultiLS_ref.setBestPoint(ref);
			Set<WCPSolution> approx = Pareto.getApproxToFront();
			if (approx.isEmpty()) {
				sizeP = 0;
			} else {
				boolean entra = false;
				for (WCPSolution s : approx) {
					if (Pareto.checkApproxBetween(s, s1, s2)) {
						entra = improvement(s);
						if (entra) {// si las dos fronteras coinciden en tamaño, continúo
							continue;
						} else {// si cambio la frontera
							sizeP = paretoSet.size();
							break;
						}
					}
				}
				
				if (!entra) {
					iter++;
				}
			}
		}
	}

	@Override
	public Result execute(WCPInstance instance) {
		this.instance = instance;
		Result r = new Result(instance.getName());
		Timer.initTimer();
		Pareto.reset();
		alg.execute(instance);
		VNS(Pareto.getFront());
		String path = "./pareto/" + instance.getName() + "/" + this.toString() + ".txt"; // este THIS a quién se refiere
																							// si estoy corriendo una
																							// clase herencia¿?
		Pareto.saveToFile(path);
		double secs = Timer.getTime() / 1000.0;
		System.out.println(Pareto.size() + "\t" + secs);
		r.add("Pareto", Pareto.size());
		r.add("Time (s)", secs);
		WCPInstance.incrementIndex();
		return r;
	}

	@Override
	public String toString() {
		// System.out.println(this.getAlgName());
		String output = WCPInstance.indexSolution + "VNS_ref (" + this.getAlgName() + "RefLS";
		/*
		 * for(Improvement<WCPSolution> l: this.ls) { output+= l.toString()+","; }
		 */
		output += ")";
		return output;
	}

}
