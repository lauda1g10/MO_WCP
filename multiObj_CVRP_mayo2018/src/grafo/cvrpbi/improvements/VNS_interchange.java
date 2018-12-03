package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

// NUEVA APROXIMACIÓN 16 Abril: shaking = intercambia K nodos aleatoriamente entre rutas DISTINTAS.
public class VNS_interchange extends VNS {

    protected WCPSolution best;
    protected double kStep;
    protected static final double kMax = 0.2;

    public VNS_interchange(Improvement<WCPSolution>[] ls, double kStep) {
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
            //double dif1 = sol.getRoute(r1).getDistance()-sol.getRoute(r1).distanceBetween(0, size1-1);
            int r2 = rnd.nextInt(nr);
            int size2 = sol.getRoute(r2).size();
            //double dif2 = sol.getRoute(r2).getDistance()-sol.getRoute(r2).distanceBetween(0, size2-1);
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
       // System.out.println("nueva solución = " + sol);
        return sol;
    }
    @Override
    public String toString() {
    	  return super.toString()+"1to1";
    }
}
