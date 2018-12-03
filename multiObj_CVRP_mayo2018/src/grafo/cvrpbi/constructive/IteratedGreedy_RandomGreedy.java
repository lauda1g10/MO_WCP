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
		int size = s.getInstance().getNodes();
		toRem = (int) Math.ceil(super.getAlpha() * size);
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
		// introducimos los nodos de la lista en la mejor posición en la mejor
		// ruta.
		double minCost = Double.MAX_VALUE;
		int minRoute = -1;
		int selected = -1;
		int selectedPos = -1;
		int posCL = 0;
		int nRoutes = s.getNumRoutes();
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
				//return cRand.constructSolution(s.getInstance());
				return super.iniSol;
			}
			super.getAvailableNodes().remove(selectedPos);
			s.addNode(selected, minRoute, costReconst[selected].pos[minRoute], costReconst[selected].dist[minRoute],costReconst[selected].time[minRoute] );
			updateInsertCost(s, super.getAvailableNodes(), costReconst, minRoute);
			minCost = Double.MAX_VALUE;
		}
		/*//comprobamos que las soluciones están completas y que todas las rutas tienen al menos un nodo!
		int c = 0;
		for(int r = 0;r<s.getNumRoutes();r++){
			int sizeR =s.getRoute(r).size()-2;
			if (sizeR<1){
				System.out.println("revisar reConstrucción");
			}
			c+=sizeR;
			
		}*/
		return s;
	}
@Override
	public String toString() {
		return "RandomGreedy (" + super.getAlpha() + "," + super.getBeta() + ")";
	}
}
