package grafo.cvrpbi.algorithms;

import java.io.File;
import java.util.Random;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class VNS_WCK_Multi implements Algorithm<WCPInstance_RealInstance> {

    private C1_WCK c;
    private Improvement<WCPSolution> ls;
    private int iters;
    private WCPSolution best;
    private int lambdaInterval;
    private double kStep;
    private static final double kMax = 0.2;

    public VNS_WCK_Multi(C1_WCK c, Improvement<WCPSolution> ls, int iters, int interval, double kStep) {
        this.c = c;
        this.ls = ls;
        this.kStep = kStep;
        this.iters = iters;
        this.lambdaInterval = interval;
    }

   @Override
    public Result execute(WCPInstance_RealInstance instance) {
        Pareto.reset();
        best = null;
        Result r = new Result(instance.getName());
        System.out.print(instance.getName()+"\t");
        Timer.initTimer();
        // Init values for ideal1&2
        // Initial values for ideal1&2&3
        c.setLambda(0,0);//ideal3
        WCPSolution best3 = constructN(instance, iters);
        c.setIdeal3(best3.getDifTime());
        c.setLambda(0,1);
        WCPSolution best2 = constructN(instance, iters);
        c.setIdeal2(best2.getDistanceLongestRoute());
        c.setLambda(1,0);
        WCPSolution best1 = constructN(instance, iters);
        c.setIdeal1(best1.getTotalDist());
        for (int i = 0; i<lambdaInterval;i++){
            c.setLambda();
            constructN(instance, iters);
        }
        
        double secs = Timer.getTime()/1000.0;
        System.out.println(Pareto.size()+"\t"+secs);
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

    private WCPSolution constructN(WCPInstance_RealInstance instance, int n) {
        WCPSolution localBest = null;
        for (int i = 0; i < n; i++) {
            WCPSolution sol = c.constructSolution(instance);
            VNS(sol,kStep,kMax);
            if (sol != null) {
                if (!Pareto.add(sol)){
                	Pareto.addApprox(sol);
                }
            }
            if (sol != null && (localBest == null ||
                    C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(), sol.getDifTime(),sol.getNumRoutes())
                            < C1_WCK.evalWierzbicki(localBest.getTotalDist(), localBest.getDistanceLongestRoute(), localBest.getDifTime(),localBest.getNumRoutes()))) {
                localBest = sol;
            }
        }
        return localBest;
    }

    /*
                VNS
     */

    private void VNS(WCPSolution sol, double kStep, double kMax) {
        int n = sol.getInstance().getNodes();
        double k = 0;
        double WValue = C1_WCK.evalWierzbicki(sol.getTotalDist(), sol.getDistanceLongestRoute(),sol.getDifTime(),sol.getNumRoutes());
        while (k <= kMax) {
            int pertSize = (int) Math.ceil(k * n);
            WCPSolution s = shake(sol,pertSize);
            ls.improve(s);
            if (best == null){
            	best = s;
            	WValue = C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes());
            	}
        else {
        	 if(C1_WCK.evalWierzbicki(s.getTotalDist(), s.getDistanceLongestRoute(),s.getDifTime(),s.getNumRoutes()) < WValue){
        		 best = s;
        		 sol.copy(s);
        		 WValue = C1_WCK.evalWierzbicki(best.getTotalDist(), best.getDistanceLongestRoute(),best.getDifTime(),best.getNumRoutes());
        	 }
        }
           
           k = k + kStep;
        }
        
    }

    private WCPSolution shake(WCPSolution s, int k) {
    	WCPSolution sol = new WCPSolution(s);
        int nr = sol.getNumRoutes();
        Random rnd = RandomManager.getRandom();
        int i = 0;
        while (i < k) {
            int r1 = rnd.nextInt(nr);
            int size1 = sol.getRoute(r1).size();
            int r2 = rnd.nextInt(nr);
            int size2 = sol.getRoute(r2).size();
            if (size1<3 || size2<3){
            	continue;
            }
            int p1 = 1+rnd.nextInt(size1-2);
            int v1 = sol.getRoute(r1).getNodeAt(p1);
            int p2 = 1+rnd.nextInt(size2-2);
            int v2 = sol.getRoute(r2).getNodeAt(p2);
            
            if (r1 == r2 && v1 == v2)
            {
            	continue;
            }
            else if (sol.isFeasibleInterchange(r1, v1, r2, v2))
            {
            	double[] time = sol.evalTimeInterchange(r1, r2, p1, p1, p2, p2);
				if (time[0] > WCPSolution.workingTime) {
					continue;
				}
            double eval[] = sol.evalInterchange(r1, r2, p1, p1, p2, p2);//Para que interchange me evalúe el intercambio de 1 sólo nodo de cada ruta, el principio y final de las posiciones de la subcadena a evaluar debe ser el mismo.
            sol.moveInterchange(r1, r2, p1, p1, p2, p2, eval,time);
            }
            i++;
        }
        return sol;
    }

    @Override
    public Solution getBestSolution() {
        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"("+c+","+ls+","+iters+","+lambdaInterval+")";
    }
}
