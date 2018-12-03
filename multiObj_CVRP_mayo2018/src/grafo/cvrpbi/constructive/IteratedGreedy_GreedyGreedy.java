package grafo.cvrpbi.constructive;

import java.util.Collections;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

public class IteratedGreedy_GreedyGreedy extends ConstIteratedGreedy {

	private InsertCost[] costReconst;

	public IteratedGreedy_GreedyGreedy(Constructive<WCPInstance, WCPSolution> c, double a, double b, int iter) {
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
		// introducimos los nodos de la lista en la mejor posición en la mejor
		// ruta.
		double minCost = Double.MAX_VALUE;
		int minRoute = -1;
		int selected = -1;
		int selectedPos = -1;
		int posCL = 0;
		int nRoutes = s.getNumRoutes();
		Collections.shuffle(super.getAvailableNodes());
		costReconst = super.createInsertCost(s, getAvailableNodes(), s.getInstance().getNodes(), s.getNumRoutes());
		while (!super.getAvailableNodes().isEmpty()) {
			posCL = 0;
			selected = -1;
			for (int v : super.getAvailableNodes()) {
				for (int r = 0; r < nRoutes; r++) {
					if ((costReconst[v].pos[r] >= 0) && (costReconst[v].value[r] < minCost)) {
						minCost = costReconst[v].value[r];
						minRoute = r;
						selected = v;
						selectedPos = posCL;
					}
				}
				posCL++;
			}
			if (selected < 0) {
				// return cRand.constructSolution(s.getInstance());
				return super.iniSol;
			}
			super.getAvailableNodes().remove(selectedPos);
			s.addNode(selected, minRoute, costReconst[selected].pos[minRoute], costReconst[selected].dist[minRoute],
					costReconst[selected].time[minRoute]);
			updateInsertCost(s, super.getAvailableNodes(), costReconst, minRoute);
			minCost = Double.MAX_VALUE;
		}
		return s;
	}

	@Override
	public String toString() {
		return "GreedyGreedy (" + super.getAlpha() + "," + super.getBeta() + ")";
	}
}
