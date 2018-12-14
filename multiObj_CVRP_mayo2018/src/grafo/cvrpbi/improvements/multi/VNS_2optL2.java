package grafo.cvrpbi.improvements.multi;

import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

// NUEVA APROXIMACIÓN 16 Abril: shaking = intercambia K nodos aleatoriamente entre rutas DISTINTAS.
public class VNS_2optL2 extends VNS_refL2 {

    public VNS_2optL2(double kStep) {
       super(kStep);
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
        StringBuilder stb = new StringBuilder();
        stb.append(WCPInstance.indexSolution+"VNS_2optrefL2").append("(");
        for (Improvement<WCPSolution> search : MultiLS_ref.getLS()) {
            stb.append(search).append(",");
        }
        stb.append(")");
        return stb.toString();
    }
}
