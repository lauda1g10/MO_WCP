package grafo.cvrpbi.algorithms;

import java.io.File;

import grafo.cvrpbi.experiments.ExperimentAlgorithm;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.Timer;

public class AlgConstGRASP_Combi implements Algorithm<WCPInstance_RealInstance> {

    private Constructive<WCPInstance_RealInstance, WCPSolution> c;
    private Improvement<WCPSolution> ls;
    private WCPSolution best;
    private int iters;

    public AlgConstGRASP_Combi(Constructive<WCPInstance_RealInstance, WCPSolution> cCombi, Improvement<WCPSolution> ls, int iters) {
        this.c = cCombi;
        this.ls = ls;
        this.iters = iters;
    }

    public Result execute(WCPInstance_RealInstance instance) {
        Pareto.reset();
        Result r = new Result(instance.getName());
        System.out.print(instance.getName()+"\t");
        Timer.initTimer();
        for (int i = 0; i < iters; i++) {
        	WCPSolution sol = new WCPSolution(instance);
        	sol = c.constructSolution(instance);
        	 if (!Pareto.add(sol)){
             	Pareto.addApprox(sol);
             }
        	if (sol == null) {continue;}
            ls.improve(sol);
            }
        double secs = Timer.getTime()/1000.0;
        System.out.print(Pareto.size()+"\t"+secs+"\t");
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
        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"("+c+","+ls+","+"index"+ExperimentAlgorithm.indexAlg+")"+iters;
    }
}

