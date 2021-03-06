package grafo.cvrpbi.improvements;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

public abstract class VNS implements Improvement<WCPSolution> {

	private VND ls;
	private WCPSolution best;
	private double kStep;
	private static final double kMax = 0.2;

	public VNS(Improvement<WCPSolution>[] improvements, double kStep) {
		this.ls = new VND(improvements);
		this.kStep = kStep;
	}

	@Override
	public void improve(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		double Value = sol.getOF();
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = shake(sol, pertSize);
			ls.improve(s);
			if (best == null || s.getOF() < Value) {
				best = s;
				Value = s.getOF();
				sol.copy(s);
			}
			k = k + kStep;
		}
	}

	protected abstract WCPSolution shake(WCPSolution s, int k);
	@Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("VNS").append("_"+this.ls);
        return stb.toString();
    }
}
