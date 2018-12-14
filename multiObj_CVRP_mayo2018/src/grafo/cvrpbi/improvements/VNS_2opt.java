package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

// NUEVA APROXIMACIÓN 16 Abril: shaking = hace K movimientos 2opt aleatoriamente en la MISMA RUTA.
public class VNS_2opt extends VNS {

    protected WCPSolution best;
    protected double kStep;
    protected static final double kMax = 0.2;

    public VNS_2opt(Improvement<WCPSolution>[] ls, double kStep) {
       super(ls,kStep);
    }

    protected WCPSolution shake(WCPSolution s, int k) {
    	WCPSolution sol = new WCPSolution(s);
        int nr = sol.getNumRoutes();
        Random rnd = RandomManager.getRandom();
        int i = 0;
        while (i < k) {
            int r1 = rnd.nextInt(nr);
            int size1 = sol.getRoute(r1).size();
            if (size1<3){
            	continue;
            }
            int p1 = 1+rnd.nextInt(size1-2);
            int p2 = 1+rnd.nextInt(size1-2);
            int min = Math.min(p1, p2);
            int max = Math.max(p1, p2);
            if (p1 == p2)
            {
            	continue;
            }
            else 
            {
            	double time = sol.evalTimeMove2Opt(r1, min, max);
				if (time > WCPSolution.workingTime) {
					continue;
				}
            double eval = sol.evalMove2Opt(r1, min, max);
            sol.move2Opt(r1, min, max, eval, time);
            }
            i++;
        }
        return sol;
    }

    @Override
    public String toString() {
        return super.toString()+"2opt";
    }
}
