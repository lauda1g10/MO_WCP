package grafo.cvrpbi.constructive;

import java.util.Collections;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

public class IteratedGreedy_RandomRandom extends ConstIteratedGreedy {

	public IteratedGreedy_RandomRandom(Constructive<WCPInstance, WCPSolution> c, double a, double b, int iter) {
		super(c, a, b, iter);
	}

	@Override
	public WCPSolution destroy(WCPSolution sol) {
		WCPSolution s = new WCPSolution(sol);
		// Eliminamos un % de nodos de c/ruta, seleccionados aleatoriamente.
		int toRem;
		int v;
		for (int r = 0; r < s.getInstance().getVehicles(); r++) {
			double beforeD = s.getRoute(r).getDistance();
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
			s.updateLongestRoute(r, s.getRoute(r).getDistance(), beforeD);
		}
		return s;
	}

	@Override
	public WCPSolution reBuild(WCPSolution s) {
		// introducimos los nodos de la lista en una ruta y posición factibles.
		int pos = -1;
		int rt = -1;
		int selected = -1;
		int nRoutes = s.getInstance().getVehicles();
		Collections.shuffle(super.getAvailableNodes());
		s.getInstance();
		while (!super.getAvailableNodes().isEmpty()) {
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
				return cRand.constructSolution(s.getInstance());
			}
		}
		return s;
	}
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + super.getAlpha() + ","+super.getBeta()+")";
	}
}
