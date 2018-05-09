package grafo.cvrpbi.constructive;

import java.util.Collections;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

public class IteratedGreedy_RandomGreedy extends ConstIteratedGreedy {

	private InsertCost[] costReconst;

	public IteratedGreedy_RandomGreedy(Constructive<WCPInstance, WCPSolution> c, double a, double b, int iter) {
		super(c, a, b, iter);
	}

	@Override
	public WCPSolution destroy(WCPSolution sol) {
		WCPSolution s = new WCPSolution(sol);
		// Eliminamos un % de nodos de c/ruta, seleccionados aleatoriamente.
		int toRem;
		int v;
		for (int r = 0; r < s.getInstance().getVehicles(); r++) {
			//double beforeD = s.getRoute(r).getDistance();
			int size = s.getRoute(r).size();
			toRem = (int) Math.ceil(super.getAlpha() * size);
			int k = 0; // conteo de los nodos eliminados de la ruta r.
			while (k < toRem && size > 2) {
				int pos = RandomManager.getRandom().nextInt(size - 2) + 1;
				v = s.getRoute(r).getNodeAt(pos);
				double eval = s.getRoute(r).evalRemove(pos);
				double evalTime = s.getRoute(r).evalTimeRemove(pos);
				s.removeSubRoute(r, pos, pos, eval, evalTime);
				super.getAvailableNodes().add(v);
				k++;
				size--;
			}
		//	s.updateLongestRoute(r, s.getRoute(r).getDistance(), beforeD);
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
		return this.getClass().getSimpleName() + "(" + super.getAlpha() + ","+super.getBeta()+")";
	}
}
