package grafo.cvrpbi.algorithms;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.results.Result;
import grafo.optilib.structure.Solution;
import grafo.optilib.tools.RandomManager;
import grafo.optilib.tools.Timer;

/**
 * Created by lauradelgado on 08/10/2017.
 */
public abstract class AlgPathRelinking implements Algorithm<WCPInstance> {

	protected Algorithm<WCPInstance> alg; // debe ser un algoritmo que genere
											// una
											// frontera de pareto.
	protected WCPSolution guide;
	protected WCPSolution initial;
	protected final int maxTries = 30;
	protected WCPInstance instance;
	protected int[][] symDistance;
	protected int[] nodeRouteG;
	protected int[] nodePosG;
	protected int[] nodeRouteI;
	protected int[] nodePosI;
	

	public AlgPathRelinking(Algorithm<WCPInstance> a) {
		this.alg = a;
	}
public int getGuideSolutionVeh(){
	return guide.getNumRoutes();
}
public int getInitialSolutionVeh(){
	return initial.getNumRoutes();
}
	@Override
	public Result execute(WCPInstance instance) {
		this.instance = instance;
		Result r = new Result(instance.getName());
		Timer.initTimer();
		Pareto.reset();
		alg.execute(instance);
		String path0 = "./pareto/" + instance.getName() +"/sol"+WCPInstance.indexSolution+this.alg+ ".txt";
		System.out.print(instance.getName()+"\t"+Pareto.size()+"\t");
		Pareto.saveToFile(path0);
		nodeRouteG = new int[instance.getNodes()];
		nodePosG = new int[instance.getNodes()];
		nodeRouteI = new int[instance.getNodes()];
		nodePosI = new int[instance.getNodes()];
		initial = new WCPSolution(instance);
		WCPSolution s1 = new WCPSolution(instance);
		WCPSolution s2 = new WCPSolution(instance);
		// primero hacemos en base al primer objetivo: TOTAL DISTANCE
		int p = 0;
		int iter = 0;		
		int sizeP = Pareto.size();
		while (sizeP>1 && iter<maxTries){
			s1.copy(Pareto.getFrontAt(p));
			s2.copy(Pareto.getFrontAt(p + 1)); 
			
			WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
			if (s1.getOF() < s2.getOF()) {
				guide = s1;
				initial.copy(s2);// necesito hacer una copia xq si no, al
									// modificarla, me cambia la solución que ya
									// tenía!
			} else {
				guide = s1;
				initial.copy(s2);
			}
			
			
			this.createNodeInRouteG();
			this.createNodeInRouteI();
			this.obtainSymDistanceM();
			this.PathRelinking();

			WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
			if (s1.getOF() < s2.getOF()) {
				guide = s1;
				initial.copy(s2);// necesito hacer una copia xq si no, al
									// modificarla, me cambia la solución que ya
									// tenía!
			} else {
				guide = s2;
				initial.copy(s1);
			}
			
			this.createNodeInRouteG();
			this.createNodeInRouteI();
			this.obtainSymDistanceM();
			this.PathRelinking();
			
			WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
			if (s1.getOF() < s2.getOF()) {
				guide = s1;
				initial.copy(s2);// necesito hacer una copia xq si no, al
									// modificarla, me cambia la solución que ya
									// tenía!
			} else {
				guide = s2;
				initial.copy(s1);
			}
			
			this.createNodeInRouteG();
			this.createNodeInRouteI();
			this.obtainSymDistanceM();
			this.PathRelinking();
			
			
			if(Pareto.size()!=sizeP){
				sizeP = Pareto.size();
				iter = 0;
				p=RandomManager.getRandom().nextInt(sizeP-1);
			}
			else{
				iter++;
				if(Pareto.size()-1>p+1)
				p++;
				}
			
		}
		String path = "./pareto/" + instance.getName() +"/PR"+WCPInstance.indexSolution+alg.getClass().getSimpleName()+ ".txt";
		Pareto.saveToFile(path);
		double secs = Timer.getTime() / 1000.0;
		System.out.println(Pareto.size() + "\t" + secs);
		r.add("Pareto", Pareto.size());
		r.add("Time (s)", secs);
		return r;
	}

	@Override
	public Solution getBestSolution() {
		return guide;
	}
//Esta función NO tiene en cuenta las posiciones.
	private int countCommon(int rG, int rI) {
		int c = 0;
		for (int p = 1; p < Math.min(guide.getRoute(rG).size(), initial.getRoute(rI).size()) - 1; p++) {
			if (initial.getRoute(rI).contains(guide.getRoute(rG).getNodeAt(p))) {
				c++;
			}
		}
		return c;
	}

	protected void obtainSymDistanceM() {
		this.symDistance = new int[guide.getNumRoutes()][initial.getNumRoutes()]; //Esta definición varía en el caso MOWCP
		for (int rI = 0; rI < initial.getNumRoutes(); rI++) {
			updateSymDistanceM(rI);
		}
	}

	protected void updateSymDistanceM(int rI) {
		for (int rG = 0; rG < guide.getNumRoutes(); rG++) {
			symDistance[rG][rI] = countCommon(rG, rI);
		}
	}

	protected int findRouteCommonWith(int rG) {
		int r = 0;
		int rmax = 0;
		int max = 0;
		for (int c : this.symDistance[rG]) {
			if (c > max) {
				max = c;
				rmax = r;
			}
			r++;
		}
		return rmax;
	}

	protected void createNodeInRouteG() {
		for (int r = 0; r < guide.getNumRoutes(); r++) {
			for (int p = 1; p < guide.getRoute(r).size() - 1; p++) {
				int v = guide.getRoute(r).getNodeAt(p);
				this.nodePosG[v] = p;
				this.nodeRouteG[v] = r;
			}
		}
	}

	protected void createNodeInRouteI() {
		for (int r = 0; r < initial.getNumRoutes(); r++) {
			updateNodeInRouteI(r);
		}
	}

	protected void updateNodeInRouteI(int r) {
		for (int pos = 1; pos < initial.getRoute(r).size() - 1; pos++) {
			nodePosI[initial.getRoute(r).getNodeAt(pos)] = pos;
			nodeRouteI[initial.getRoute(r).getNodeAt(pos)] = r;
		}
	}

	protected abstract void PathRelinking();
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + alg + "_PathRelinking)";
	}
}

