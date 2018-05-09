package grafo.cvrpbi.algorithms;

import java.io.File;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.constructive.ConstIteratedGreedy;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

public class IteratedGreedy_Multi_WCK implements Algorithm<WCPInstance> {

	private ConstIteratedGreedy algIG;

	private int lambdaIntervals;
	private int iters;
	private WCPSolution localBest;
	private Constructive<WCPInstance, WCPSolution> c1;
	private Constructive<WCPInstance, WCPSolution> c2;
	private Constructive<WCPInstance, WCPSolution> c3;
	private C1_WCK c_W;
		
	public IteratedGreedy_Multi_WCK(Constructive<WCPInstance, WCPSolution> c1,Constructive<WCPInstance, WCPSolution> c2,Constructive<WCPInstance, WCPSolution> c3,C1_WCK c_W, int lambdas,int iters, ConstIteratedGreedy iteratedG){
	this.c1 = c1;
	this.c3 = c3;
	this.iters = iters;
	this.c2 = c2;
	this.c_W = c_W;
	this.algIG = iteratedG;
	this.lambdaIntervals = lambdas;
	}
	public Result execute(WCPInstance instance) {
		RandomManager.setSeed(1234);
		Pareto.reset();
		Result r = new Result(instance.getName());
		System.out.print(instance.getName() + "\t");
		Timer.initTimer();
		double[] min = new double[4];
		double[] max = new double[4];
		min[3] = instance.numVeh();
		max[3]= instance.getMaxVeh();
		min[0] = Double.MAX_VALUE;
		min[1] = Double.MAX_VALUE;
		min[2] = Double.MAX_VALUE;
		max[0] = Double.MIN_VALUE;
		max[1] = Double.MIN_VALUE;
		max[2] = Double.MIN_VALUE;
		for (int k = instance.numVeh(); k < instance.getMaxVeh(); k++) {
		// Initial values for ideal1&2&3
		WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
		WCPSolution sol;
		algIG.setConstructive(c1);
		for (int i = 0; i < iters; i++) {
			sol = algIG.constructSolution(instance);
			if (sol != null) {
				if (min[0] > sol.getOF()) {
					min[0] = sol.getOF();
				} else if (max[0] < sol.getOF()) {
					max[0] = sol.getOF();
				}
				if (!Pareto.add(sol)) {
					Pareto.addApprox(sol);
				}
			}
		}

		WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
		algIG.setConstructive(c2);
		
		for (int i = 0; i < iters; i++) {
			sol =  algIG.constructSolution(instance);
			if (sol != null) {
				if (min[1] > sol.getOF()) {
					min[1] = sol.getOF();
				} else if (max[1] < sol.getOF()) {
					max[1] = sol.getOF();
				}
				if (!Pareto.add(sol)) {
					Pareto.addApprox(sol);
				}
			}
		}
		WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
		algIG.setConstructive(c3);
		for (int i = 0; i < iters; i++) {
			sol = algIG.constructSolution(instance);
			if (sol != null) {
				if (min[2] > sol.getOF()) {
					min[2] = sol.getOF();
				} else if (max[2] < sol.getOF()) {
					max[2] = sol.getOF();
				}
				if (!Pareto.add(sol)) {
					Pareto.addApprox(sol);
				}
			}
		}
		
		C1_WCK.setIdeal(min);
		C1_WCK.setNadir(max);
		WCPSolution.currentOF = WCPSolution.ObjFunct.WIERZBICKI;
		algIG.setConstructive(c_W);
		for (int i = 0; i < lambdaIntervals; i++) {
			c_W.setLambda();
			localBest = null;
			for (int j = 0; j < iters; j++) {
				sol =  algIG.constructSolution(instance);
				if (sol != null && localBest !=null)
					{
					if (sol.getOF()<localBest.getOF()){
					}
						localBest = sol;
					}
					if (!Pareto.add(sol)) {
						Pareto.addApprox(sol);
					}
				}
		}
		WCPInstance.currentVehicles = WCPInstance.currentVehicles+1;
		}
		 double secs = Timer.getTime()/1000.0;
	        System.out.print(Pareto.size()+"\t"+secs+"\n");
	        r.add("Pareto", Pareto.size());
	        r.add("Time (s)", secs);
	        File folder = new File("./pareto/"+ instance.getName());
			if (!folder.exists()) {
				folder.mkdirs();
				}
			String path = "./pareto/" + instance.getName() +"/"+this.toString()+".txt";
			Pareto.saveToFile(path);
	        return r;
	}

	@Override
	public Solution getBestSolution() {
		return localBest;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + algIG +")";
	}

}
