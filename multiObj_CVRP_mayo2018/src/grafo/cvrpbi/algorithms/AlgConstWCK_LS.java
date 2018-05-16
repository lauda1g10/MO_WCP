package grafo.cvrpbi.algorithms;

import java.util.ArrayList;
import java.util.List;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.Pareto;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.Timer;

/**
 * Created by LauraDelgado on 27/10/2017.
 */
public class AlgConstWCK_LS implements Algorithm<WCPInstance_RealInstance> {

    private C1_WCK c;
    private Improvement<WCPSolution> ls;
    private int iters;
    private WCPSolution best;
    private final double ratio =1.15;

    public AlgConstWCK_LS(C1_WCK c, Improvement<WCPSolution> ls, int iters) {
        this.c = c;
        this.ls = ls;
        this.iters = iters;
    }

    @Override
    public Result execute(WCPInstance_RealInstance instance) {
    	 best = null;
         Result r = new Result(instance.getName());
         Pareto.reset();
         System.out.print(instance.getName()+"\t");
         Timer.initTimer();
         List<WCPSolution> bestSols = new ArrayList<WCPSolution>();
         for (int i = 0; i < iters; i++) {
             WCPSolution sol = c.constructSolution(instance);
             if (sol == null) continue;
             if (best == null || C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),sol.getDifTime(),sol.getNumRoutes())<C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes())) {
                 best = sol;
                 bestSols.removeIf(u->C1_WCK.evalWierzbicki(u.getTotalDist(), u.getDistanceLongestRoute(),u.getDifTime(),u.getNumRoutes())<ratio*C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes()));
             }
             else if (best != null &&  C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),sol.getDifTime(),sol.getNumRoutes())<ratio*C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes())) {
            	 bestSols.add(sol);
             }
         }
             for (WCPSolution s:bestSols){
             ls.improve(s);
             if (best == null || C1_WCK.evalWierzbicki(s.getTotalDist(), s.getDistanceLongestRoute(), s.getDifTime(),s.getNumRoutes())<C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes())) {
                 best = s;
             }
         }
         double secs = Timer.getTime()/1000.0;
         System.out.println(C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes())+"\t"+secs);
         r.add("TotalCost", C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes()));
         r.add("Time (s)", secs);
         return r;
    }

    @Override
    public Solution getBestSolution() {
        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"("+c+","+ls+","+iters+")";
    }
}
