package grafo.cvrpbi.algorithms;

import java.io.File;

import grafo.cvrpbi.experiments.ExperimentAlgorithm;
import grafo.cvrpbi.improvements.VND;
import grafo.cvrpbi.improvements.VND_combi;
import grafo.cvrpbi.improvements.VNS;
import grafo.cvrpbi.improvements.VNS_multi;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Pareto;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.Timer;

/**
 * Created by LauraDelgado on 12/04/2018
 */
public class AlgConstGRASP_PO_RandomLS implements Algorithm<WCPInstance_RealInstance> {

    private Constructive<WCPInstance_RealInstance, WCPSolution> c1;
    private Constructive<WCPInstance_RealInstance, WCPSolution> c2;
    private Constructive<WCPInstance_RealInstance, WCPSolution> c3;
    private Improvement<WCPSolution>[] ls;
    private Improvement<WCPSolution> vns;
    private WCPSolution best;
    private int iters;

    public AlgConstGRASP_PO_RandomLS(Constructive<WCPInstance_RealInstance, WCPSolution> c1,Constructive<WCPInstance_RealInstance, WCPSolution> c2,Constructive<WCPInstance_RealInstance, WCPSolution> c3, Improvement<WCPSolution>[] ls, int iters) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.ls = ls;
        this.vns = new VNS_multi(new VND_combi(ls),0.05);
        this.iters = iters;
    }

    public Result execute(WCPInstance_RealInstance instance) {
        Pareto.reset();
        Result r = new Result(instance.getName());
        System.out.print(instance.getName()+"\t");
        Timer.initTimer();
        VNS bl = new VNS(ls,0.05); 
        for (int i = 0; i < iters; i++) {
        	WCPSolution sol = new WCPSolution(instance);
        	if (i%3 == 0)
        	{
        		WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
        		 sol = c1.constructSolution(instance);
        	}
        	else if (i%3 == 1)
        	{
        		WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
        		sol = c2.constructSolution(instance);
        	}else if (i%3 == 2){
        		WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
        		sol = c3.constructSolution(instance);
        	}
        	 if (!Pareto.add(sol)){
             	Pareto.addApprox(sol);
             }
        	if (sol == null) {continue;}
            bl.improve(sol); //mejora en base al objetivo
            //Ahora probamos a mejorar alternando las direcciones de búsqueda.
            this.vns.improve(sol);
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
        return this.getClass().getSimpleName()+"("+c1+","+c2+","+ls+","+"index"+ExperimentAlgorithm.indexAlg+")"+iters;
    }
    }
