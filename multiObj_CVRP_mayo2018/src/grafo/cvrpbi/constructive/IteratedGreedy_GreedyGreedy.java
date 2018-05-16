package grafo.cvrpbi.constructive;

import java.util.Collections;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Constructive;

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
		int v = -1;
		int p = -1;
		double extD;
		double extT;
		int k = 0;
		double maxExtCost;
		double beforeD;
		double beforeT;
		int size;
		for (int r = 0; r < s.getInstance().getVehicles(); r++) {
			extD = 0;
			extT = 0;
			beforeD = s.getRoute(r).getDistance();
			beforeT = s.getRoute(r).getTime();
			size = s.getRoute(r).size();
			toRem = (int) Math.ceil(super.getAlpha() * size);
			maxExtCost = Double.MIN_VALUE;
			k = 0; // conteo de los nodos eliminados de la ruta r.
			while (k < toRem && size > 2) {
				for (int pos = 1; pos < size - 1; pos++) {
					double extraTime = s.getRoute(r).getTime() - sol.getRoute(r).evalRemoveT(pos);
					double eval = s.getRoute(r).getDistance() - sol.getRoute(r).evalRemove(pos);
					double ic = Double.MAX_VALUE;
					double f1 = sol.getTotalDist() - eval;
					int longRoute = sol.longestRouteIf(r, eval, sol.getRoute(r).getDistance());
					if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
						ic = eval;
					} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
						double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? -eval : 0);
						ic = f2 - sol.getOF();
					} else if (WCPSolution.currentOF == ObjFunct.TIME) {
						double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() - extraTime);
						ic = f3 - sol.getOF();
					} else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
						double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? -eval : 0);
						double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() - extraTime);
						ic = C1_WCK.evalWierzbicki(f1, f2, f3, WCPInstance.currentVehicles) - sol.getOF();
					}
					if (ic > maxExtCost) {
						p = pos;
						v = s.getRoute(r).getNodeAt(p);
						maxExtCost = ic;
						extD = eval;
						extT = extraTime;
					}
				}
				if (p > 0) {
					s.removeSubRoute(r, p, p, beforeD- extD, beforeT-extT);
					super.getAvailableNodes().add(v);
					k++;
					size--;
				}
			}
			s.updateLongestRoute(r, s.getRoute(r).getDistance(), beforeD);
			s.updateTimingRoutes(r, s.getRoute(r).getTime());
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
		int nRoutes = s.getInstance().getVehicles();
		Collections.shuffle(super.getAvailableNodes());
		s.getInstance();
		costReconst = super.createInsertCost(s, getAvailableNodes(), s.getInstance().getNodes(),s.getNumRoutes());
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
				return cRand.constructSolution(s.getInstance());
			}
			super.getAvailableNodes().remove(selectedPos);
			s.addNode(selected, minRoute, costReconst[selected].pos[minRoute], costReconst[selected].dist[minRoute],costReconst[selected].time[minRoute] );
			updateInsertCost(s, super.getAvailableNodes(), costReconst, minRoute);
			minCost = Double.MAX_VALUE;
		}
		return s;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + super.getAlpha() + "," + super.getBeta() + ")";
	}
}
