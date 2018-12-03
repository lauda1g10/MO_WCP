package grafo.cvrpbi.improvements.multi;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

public abstract class VNS_refL2 implements Improvement<WCPSolution> {

	private VND_refL2 ls;
	private WCPSolution best;
	private double kStep;
	private static final double kMax = 0.2;

	public VNS_refL2(double kStep) {
		this.ls = new VND_refL2();//toma directamente las búsquedas locales de MultiLS_Ref
		this.kStep = kStep;
	}

	@Override
	public void improve(WCPSolution sol) {
		int n = sol.getInstance().getNodes();
		double k = 0;
		double bestF = sol.distanceL2To(MultiLS_ref.getBestPoint());
		while (k <= kMax) {
			int pertSize = (int) Math.ceil(k * n);
			WCPSolution s = shake(sol, pertSize);
			ls.improve(s);
			double v = s.distanceL2To(MultiLS_ref.getBestPoint());
			if (best == null || v< bestF) {
				best = s;
				bestF = v;
				sol.copy(s);
			}
			k = k + kStep;
		}
	}

	protected abstract WCPSolution shake(WCPSolution s, int k);
	
	 @Override
	    public String toString() {
	        StringBuilder stb = new StringBuilder();
	        stb.append(WCPInstance.indexSolution+"VNS_refL2").append("(");
	        for (Improvement<WCPSolution> search : MultiLS_ref.getLS()) {
	            stb.append(search).append(",");
	        }
	        stb.append(")");
	        return stb.toString();
	    }
}
