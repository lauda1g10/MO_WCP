package grafo.cvrpbi.constructive;

import java.util.ArrayList;
import java.util.List;

import grafo.cvrpbi.structure.Pareto;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.cvrpbi.structure.WCPSolution.ObjFunct;
import grafo.optilib.metaheuristics.Constructive;

public abstract class ConstIteratedGreedy implements Constructive<WCPInstance, WCPSolution> {
private double alpha; //porcentaje de destrucción (fijo)
private double beta; // criterio de aceptación: admito la solución hasta ser un beta% peor que la anterior
private int maxIter;
protected WCPSolution bestSol;
protected WCPSolution iniSol;
protected List<Integer> removedNodes;
protected CRandom cRand;
protected Constructive<WCPInstance, WCPSolution> c;

public ConstIteratedGreedy(Constructive<WCPInstance, WCPSolution> c, double a, double b, int iter){
	this.c = c;
	cRand = new CRandom();
	this.alpha = a;
	this.beta = b;
	this.maxIter = iter;
	this.removedNodes = new ArrayList<>();
}
public double getAlpha(){
	return this.alpha;
}
public double getBeta(){
	return this.beta;
}
public List<Integer> getAvailableNodes(){
	return removedNodes;
}
public void setConstructive(Constructive<WCPInstance,WCPSolution> newC){
	this.c = newC;
}
@Override
public WCPSolution constructSolution(WCPInstance instance) {
     //inicializamos los valores para WCK:
     WCPSolution s = c.constructSolution(instance);
     if (s==null){
    	 return null;
     }
     iniSol = new WCPSolution(s);
     bestSol = new WCPSolution(s);
    // System.out.println("Valor F.O: "+bestSol.getOF());
	//mejora?
	int iterSinMejora = 0;
	while(iterSinMejora<maxIter){
		this.removedNodes.clear();
		WCPSolution incompleteSolution = destroy(s);
		WCPSolution newSol = reBuild(incompleteSolution);
		Pareto.addApprox(newSol);
		if (Math.abs(s.getOF()-newSol.getOF())>WCPInstance.EPSILON && newSol.getOF()<(1+beta)*bestSol.getOF()){
			s.copy(newSol);
			if(newSol.getOF()-bestSol.getOF()<-WCPInstance.EPSILON){
				bestSol.copy(newSol);
			//	System.out.println("nuevo valor F.O:"+bestSol.getOF());
				iterSinMejora = 0;
			}else{
				iterSinMejora++;
			}
		}
		else{
			iterSinMejora++;
		}
	} 
	//System.out.println(bestSol);
     return bestSol;
}

public abstract WCPSolution destroy(WCPSolution s);
public abstract WCPSolution reBuild(WCPSolution s);

protected class InsertCost {
	int[] pos;
	double[] dist;
	double[] time;
	double[] value;

	public InsertCost(int r) {
		this.pos = new int[r];
		this.dist = new double[r];
		this.time = new double[r];
		this.value = new double[r];
	}
}

//estos costes son la variación en la función de WCK
protected InsertCost[] createInsertCost(WCPSolution sol, List<Integer> cl, int n, int nRoutes) {
	InsertCost[] insertCost = new InsertCost[n];
	for (int v : cl) {
		insertCost[v] = new InsertCost(nRoutes);
		for (int r = 0; r < nRoutes; r++) {
			if (!sol.isFeasibleAdd(v, r)) {
				insertCost[v].pos[r] = -1;
				continue;
			}
			int size = sol.getRoute(r).size();
			insertCost[v].value[r] = Double.MAX_VALUE;
			insertCost[v].pos[r] = -1;
			for (int p = 1; p < size; p++) {				
				double extraTime = sol.evalTimeAddNode(v, r, p);
				if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
					double eval = sol.evalAddNode(v, r, p);
					double ic = Double.MAX_VALUE;
					double f1 = sol.getTotalDist() + eval;
					int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
							sol.getRoute(r).getDistance());
					if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
						ic = eval;
					} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
						double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
						ic = f2-sol.getOF();
					} else if (WCPSolution.currentOF == ObjFunct.TIME){
						double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
						ic = f3-sol.getOF();
					}else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
						double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
						double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
						ic = C1_WCK.evalWierzbicki(f1, f2,f3, sol.getNumRoutes()) - sol.getOF();
					}
					if (ic < insertCost[v].value[r]) {
						insertCost[v].value[r] = ic;
						insertCost[v].pos[r] = p;
						insertCost[v].dist[r] = eval;
						insertCost[v].time[r] = extraTime;
					}
				}
			}
		}
	}
	return insertCost;
}

protected void updateInsertCost(WCPSolution sol, List<Integer> cl, InsertCost[] insertCost, int r) {
	for (int v : cl) {
		if (!sol.isFeasibleAdd(v, r)) {
			insertCost[v].pos[r] = -1;
			continue;
		}
		int size = sol.getRoute(r).size();
		insertCost[v].value[r] = Double.MAX_VALUE;
		insertCost[v].pos[r] = -1;
		for (int p = 1; p < size; p++) {
			double extraTime = sol.evalTimeAddNode(v, r, p);
			if (sol.getRoute(r).getTime() + extraTime <= WCPSolution.workingTime) {
				double eval = sol.evalAddNode(v, r, p);
				double ic = Double.MAX_VALUE;
				double f1 = sol.getTotalDist() + eval;
				int longRoute = sol.longestRouteIf(r, sol.getRoute(r).getDistance() + eval,
						sol.getRoute(r).getDistance());
				if (WCPSolution.currentOF == ObjFunct.TOTAL_DIST) {
					ic = eval;
				} else if (WCPSolution.currentOF == ObjFunct.LONGEST_ROUTE) {
					double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
					ic = f2-sol.getOF();
				} else if (WCPSolution.currentOF == ObjFunct.TIME){
					double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
					ic = f3-sol.getOF();
				}else if (WCPSolution.currentOF == ObjFunct.WIERZBICKI) {
					double f2 = sol.getRoute(longRoute).getDistance() + ((longRoute == r) ? eval : 0);
					double f3 = sol.difTimeIf(r, sol.getRoute(r).getTime() + extraTime);
					ic = C1_WCK.evalWierzbicki(f1, f2,f3, sol.getNumRoutes()) - sol.getOF();
				}
				if (ic < insertCost[v].value[r]) {
					insertCost[v].value[r] = ic;
					insertCost[v].pos[r] = p;
					insertCost[v].dist[r] = eval;
					insertCost[v].time[r] = extraTime;
				}
			}
		}
	}
}
/*@Override
public String toString() {
	return this.getClass().getSimpleName() + "(" + alpha + ","+beta+")";
}*/
}
