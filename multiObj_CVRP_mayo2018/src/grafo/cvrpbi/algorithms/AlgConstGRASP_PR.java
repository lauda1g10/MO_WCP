package grafo.cvrpbi.algorithms;

import java.io.File;
import java.util.Random;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class AlgConstGRASP_PR implements Algorithm<WCPInstance_RealInstance> {

    private Constructive<WCPInstance_RealInstance, WCPSolution> c1;
    private Constructive<WCPInstance_RealInstance, WCPSolution> c2;
    private Constructive<WCPInstance_RealInstance, WCPSolution> c3;
    private Improvement<WCPSolution> ls;
    private WCPSolution best;
    private int iters;

    public AlgConstGRASP_PR(Constructive<WCPInstance_RealInstance, WCPSolution> c1,Constructive<WCPInstance_RealInstance, WCPSolution> c2, Constructive<WCPInstance_RealInstance, WCPSolution> c3, Improvement<WCPSolution> ls, int iters){
         this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.ls = ls;
        this.iters = iters;
    }

    public Result execute(WCPInstance_RealInstance instance) {
        Pareto.reset();
        Result r = new Result(instance.getName());
        System.out.print(instance.getName()+"\t");
        Timer.initTimer();
        int it = 0;
        Random rnd = RandomManager.getRandom();
        while(it < iters){
        	int i = rnd.nextInt(100);
        	if (i%3 == 0)
        	{
        		WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
        		WCPSolution sol = (WCPSolution) c1.constructSolution(instance);
            boolean enter = Pareto.add(sol);
            if (!enter){
            	Pareto.addApprox(sol);
            }
            if (sol == null) continue;
            ls.improve(sol);
            }
        	else if (i%3 == 1)
        	{
        		WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
        		WCPSolution sol = (WCPSolution) c2.constructSolution(instance);
                 boolean enter = Pareto.add(sol);
                 if (!enter){
                 	Pareto.addApprox(sol);
                 }
                 if (sol == null) continue;
                 ls.improve(sol);
        	}
        	else
        	{
        		WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
        		WCPSolution sol = (WCPSolution) c3.constructSolution(instance);
                boolean enter = Pareto.add(sol);
                if (!enter){
                	Pareto.addApprox(sol);
                }
                if (sol == null) continue;
                ls.improve(sol);
        	}
        	}
        double secs = Timer.getTime()/1000.0;
        System.out.print(Pareto.size()+"\t"+secs+"\t");
        r.add("Pareto", Pareto.size());
        r.add("Time (s)", secs);
    	File folder = new File("./pareto/"+ instance.getName());
		if (!folder.exists()) {
			folder.mkdirs();
			}
		String path = "./pareto/" + instance.getName() +"/"+this.getClass().getSimpleName()+".txt";
		Pareto.saveToFile(path);
		return r;
    }


    public Solution getBestSolution() {
        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"("+c1+","+c2+","+ls+","+iters+")";
    }
}
