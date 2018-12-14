package grafo.cvrpbi.algorithms;

import grafo.cvrpbi.improvements.multi.VNS_2optL2;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.structure.Solution;


public class VNS_refOpt2 extends VNS_ref{
	private double kstep = 0.05;
	
	public VNS_refOpt2(Algorithm<WCPInstance> a) {
		super(a);
	}

	public boolean improvement(WCPSolution s){
		int sizeR = Pareto.size();
		VNS_2optL2 bl = new VNS_2optL2(kstep);
		bl.improve(s);
		return Pareto.size()==sizeR;
	}

	@Override
	public Solution getBestSolution() {
		// TODO Auto-generated method stub
		return null;
	}
@Override
public String toString(){
	return "("+super.toString()+ ", 2opt)";
}
}
