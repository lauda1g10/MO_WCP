package grafo.cvrpbi.improvements;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

public abstract class VNS_combi implements Improvement<WCPSolution> {

	private VND_combi ls;
	private WCPSolution best;
	private double kStep;
	private static final double kMax = 0.2;

	public VNS_combi(Improvement<WCPSolution> [] ls, double kStep) {
		this.ls = new VND_combi(ls);
		this.kStep = kStep;
	}

	@Override
	public void improve(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = shake(sol, pertSize);
			ls.improve(s);
			if (best == null) {
				best = s;
			}
			sol.copy(s);
			k = k + kStep;
		}
	}
	protected abstract WCPSolution shake(WCPSolution s, int k);
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + ls + ")";
	}
}
