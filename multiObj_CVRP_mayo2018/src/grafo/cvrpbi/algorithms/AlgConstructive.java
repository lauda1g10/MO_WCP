package grafo.cvrpbi.algorithms;

import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.Timer;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class AlgConstructive implements Algorithm<WCPInstance_RealInstance> {

	private Constructive<WCPInstance_RealInstance, WCPSolution> c;
	private int iters;
	private WCPSolution best;

	public AlgConstructive(Constructive<WCPInstance_RealInstance, WCPSolution> c, int iters) {
		this.c = c;
		this.iters = iters;
	}

	@Override
	public Result execute(WCPInstance_RealInstance instance) {
		best = null;
		Result r = new Result(instance.getName());
		System.out.print(instance.getName() + "\t");
		Timer.initTimer();
		int i = 0;
		while (i < iters) {
			WCPSolution sol = c.constructSolution(instance);
			if (sol != null) {
				i++;
				if (best == null || sol.getTotalDist() < best.getTotalDist()) {
					best = sol;
				}
			}
		}
		double secs = Timer.getTime() / 1000.0;
		double fo = (best != null) ? best.getOF() : -1;
		System.out.println(fo + "\t" + secs + "\t");
		// System.out.println(best);
		r.add("TotalDist", fo);
		r.add("Time (s)", secs);
		return r;
	}

	@Override
	public Solution getBestSolution() {
		return best;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + c + "," + iters + ")";
	}
}
