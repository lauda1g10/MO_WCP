package grafo.cvrpbi.constructive;

import java.util.Collections;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

public class IteratedGreedy_GreedyRandom extends ConstIteratedGreedy {

	public IteratedGreedy_GreedyRandom(Constructive<WCPInstance, WCPSolution> c, double a, double b, int iter) {
		super(c, a, b, iter);
	}

		@Override
	public WCPSolution destroy(WCPSolution sol) {
		WCPSolution s = new WCPSolution(sol);
		// Eliminamos un % de nodos de c/ruta, seleccionando aquellos que
		// aporten mayor coste (en la f.o) a la ruta.
		int toRem;
		int p = -1;
		int rt = -1;
		int v = -1;
		double newD = 0;
		double newT = 0;
		int k = 0;
		double maxExtCost;
		int size = s.getInstance().getNodes();
		toRem = (int) Math.ceil(super.getAlpha() * size);
		while (k < toRem) {
			p = -1;
			rt = -1;
			maxExtCost = -Double.MAX_VALUE;
			Random rd = RandomManager.getRandom();
			int r = rd.nextInt(s.getNumRoutes());
				double ic = Double.MAX_VALUE;
				int sizeR = s.getRoute(r).size();
				if (sizeR > 3) {
					for (int pos = 1; pos < sizeR - 1; pos++) {
						double newTime = s.getRoute(r).evalRemoveT(pos);
						// double extTime = s.getRoute(r).getTime()-newTime;
						double newDist = s.getRoute(r).evalRemove(pos);
						double extDist = s.getRoute(r).getDistance() - newDist;
						double f1 = s.getTotalDist() - extDist;
						int longRoute = s.longestRouteIf(r, newDist, s.getRoute(r).getDistance());
						if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
							ic = extDist;
						} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
							double f2 = s.getRoute(longRoute).getDistance() + ((longRoute == r) ? -extDist : 0);
							ic = s.getOF() - f2;
						} else if (WCPSolution.currentOF == ObjFunct.TIME) {
							double f3 = s.difTimeIf(r, newTime);
							ic = s.getOF() - f3;
						} else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
							double f2 = s.getRoute(longRoute).getDistance() + ((longRoute == r) ? -extDist : 0);
							double f3 = s.difTimeIf(r, newTime);
							ic = s.getOF() - C1_WCK.evalWierzbicki(f1, f2, f3, s.getNumRoutes());
						}
						if (ic > maxExtCost) {
							p = pos;
							rt = r;
							v = s.getRoute(rt).getNodeAt(p);
							maxExtCost = ic;
							newD = newDist;
							newT = newTime;
						}
					}
				}
			if (p > 0) {
				s.removeSubRoute(rt, p, p, newD, newT);
				super.getAvailableNodes().add(v);
				k++;
			}
		}
		return s;
	}

	@Override
	public WCPSolution reBuild(WCPSolution s) {
		// introducimos los nodos de la lista en una ruta y posición factibles.
		int pos = -1;
		int rt = -1;
		
		int nRoutes = s.getNumRoutes();
		Collections.shuffle(super.getAvailableNodes());
		while (!super.getAvailableNodes().isEmpty()) {
			int selected = -1;
			int p= 0;
			for (int v : super.getAvailableNodes()) {
			rt = RandomManager.getRandom().nextInt(nRoutes);
			if(s.isFeasibleAdd(v, rt)){
				selected = v;
				pos = RandomManager.getRandom().nextInt(s.getRoute(rt).size()-1)+1;
				s.addNode(selected, rt, pos, s.evalAddNode(v, rt, pos));
				super.getAvailableNodes().remove(p);
				break;
			}
			p++;
			}				
			if (selected < 0) {
				//si no se encuentra ningún nodo con esas características, se genera una solución NUEVA de forma aleatoria.
				//return cRand.constructSolution(s.getInstance());
				return super.iniSol;
			}
		}
		return s;
	}
	@Override
	public String toString() {
		return "GreedyRandom (" + super.getAlpha() + "," + super.getBeta() + ")";
	}
}
