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
		int v;
		int size = s.getInstance().getNodes();
		int toRem = (int) Math.ceil(super.getAlpha() * size);
		int k = 0; // conteo de los nodos eliminados de la ruta r.
		while (k < toRem) {
		int r = RandomManager.getRandom().nextInt(s.getNumRoutes());
			/*double beforeD = s.getRoute(r).getDistance();
			double beforeT = s.getRoute(r).getTime();
			s.getRoute(r).evaluateNaive();
			s.getRoute(r).evaluateNaiveTime();
			if (Math.abs(beforeD-s.getRoute(r).getDistance())>CVRPInstance.EPSILON || Math.abs(beforeT-s.getRoute(r).getTime())>CVRPInstance.EPSILON){
				System.out.println("REVISAR CONSTRUCCIÓN");
			}*/
		int sizeR = s.getRoute(r).size();
			if (sizeR>3){
				int pos = RandomManager.getRandom().nextInt(sizeR - 2) + 1;
				v = s.getRoute(r).getNodeAt(pos);
				double eval = s.getRoute(r).evalRemove(pos);
				double evalTime = s.getRoute(r).evalTimeRemove(pos);
				s.removeSubRoute(r, pos, pos, eval, evalTime);
				super.getAvailableNodes().add(v);
				k++;
			}
		//	s.updateLongestRoute(r, s.getRoute(r).getDistance(), beforeD);
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
		return "RandomRandom (" + super.getAlpha() + "," + super.getBeta() + ")";
	}
}
