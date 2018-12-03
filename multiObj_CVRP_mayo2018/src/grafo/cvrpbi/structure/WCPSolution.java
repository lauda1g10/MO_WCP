package grafo.cvrpbi.structure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import grafo.cvrpbi.constructive.C1_WCK;
import grafo.optilib.structure.Solution;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class WCPSolution implements Solution {

	private static final double EPSILON = -0.00001;

	public enum ObjFunct {
		TOTAL_DIST, LONGEST_ROUTE, WIERZBICKI, TIME
	}

	public static ObjFunct currentOF = ObjFunct.TOTAL_DIST;// por defecto.

	public static double workingTime = 27000;// 7.5h = 27000segundos
	private double totalDist;
	private int numRoutes;
	private int longestRoute;
	private int maxTimeR;
	private int minTimeR;
	private WCPRoute[] routes;
	private WCPInstance instance;

	public WCPSolution(WCPInstance instance) {
		this.numRoutes = WCPInstance.currentVehicles;
		this.totalDist = 0;
		this.longestRoute = -1;
		this.instance = instance;
		this.routes = new WCPRoute[numRoutes];
		for (int i = 0; i < numRoutes; i++) {
			this.routes[i] = new WCPRoute(instance);
		}
	}

	public double evalAddNode(int v, int r, int p) {
		return routes[r].evalAddNode(v, p);
	}

	public WCPSolution(WCPSolution sol) {
		copy(sol);/*
		//comprobamos que las soluciones están completas y que todas las rutas tienen al menos un nodo!
				int c = 0;
				for(int r = 0;r<sol.getNumRoutes();r++){
					int sizeR =sol.getRoute(r).size()-2;
					if (sizeR<1){
						System.out.println("revisar reConstrucción");
						this.removeRoute(r);
					}
					c+=sizeR;
				}
				if (c<instance.getNodes()-1){
					System.out.println("revisar");
				}*/
	}

	public void copy(WCPSolution sol) {
		this.instance = sol.instance;
		numRoutes = sol.numRoutes;
		this.totalDist = sol.totalDist;
		this.longestRoute = sol.longestRoute;
		this.routes = new WCPRoute[sol.routes.length];
		for (int i = 0; i < sol.numRoutes; i++) {
			this.routes[i] = new WCPRoute(sol.routes[i]);
		}
		this.maxTimeR = sol.maxTimeR;
		this.minTimeR = sol.minTimeR;
		
	}

	public double getTotalDist() {
		return totalDist;
	}

	public int getLongestRoute() {
		return longestRoute;
	}

	public double getDistanceLongestRoute() {
		return routes[longestRoute].getDistance();
	}

	public double getDifTime() {
		return routes[maxTimeR].getTime() - routes[minTimeR].getTime();
	}

	public void removeRoute(int rt) {
		WCPRoute[] newRoutes = new WCPRoute[numRoutes-1];
		for (int r = 0; r < rt; r++) {
			newRoutes[r] = new WCPRoute(this.routes[r]);
		}
		for (int r = rt + 1; r < numRoutes; r++) {
			newRoutes[r - 1] = new WCPRoute(this.routes[r]);
		}
		this.numRoutes = newRoutes.length;
		this.routes = newRoutes;
		this.findTimes();
		this.findLongestRoute();
		for (Route r : this.routes){
			this.totalDist +=r.distance;
		}
	}

	public int getLargestRoute() {
		return maxTimeR;
	}

	public int getShortestRoute() {
		return minTimeR;
	}

	public WCPInstance getInstance() {
		return instance;
	}

	public WCPRoute getRoute(int id) {
		return routes[id];
	}

	public void setNumRoutes(int r) {
		WCPRoute[] newRoutes = new WCPRoute[r];
		for (int rt = 0; rt < r; rt++) {
			newRoutes[rt] = new WCPRoute(this.routes[rt]);
		}
		this.numRoutes = r;
		this.routes = newRoutes;
		findTimes();
		this.findLongestRoute();
	}
/* FUNCIÓN ORIGINAL
	public boolean dominates(WCPSolution sol) {
		double diffDist = getTotalDist() - sol.getTotalDist();
		double diffLongestRoute = getDistanceLongestRoute() - sol.getDistanceLongestRoute();
		double diffTime = getDifTime() - sol.getDifTime();
		int diffV = this.getNumRoutes() - sol.getNumRoutes();
		boolean d1 = (diffDist <= -EPSILON);
		boolean d2 = (diffLongestRoute <= -EPSILON);
		boolean d3 = (diffTime <= -EPSILON);
		return d1 && d2 && d3 && (diffV <= -EPSILON);
	}
*/
	
	public boolean dominates(WCPSolution sol) {
		boolean dominates = false;
			if (getTotalDist() - sol.getTotalDist()<=-EPSILON) {
				if (getDistanceLongestRoute() - sol.getDistanceLongestRoute()<=-EPSILON) {
					if ( getDifTime() - sol.getDifTime()<=-EPSILON) {
						if (this.getNumRoutes() - sol.getNumRoutes()<=-EPSILON) {
							dominates = true;
						}
					}
				}
			}
		return dominates;
	}
	public double distanceTchebyTo(double[] bestPoint, double[] worstPoint){
		return Math.max((this.totalDist - bestPoint[0]) / (worstPoint[0] - bestPoint[0]), Math.max((this.getDistanceLongestRoute() - bestPoint[1]) / (worstPoint[1] - bestPoint[1]),Math.max((this.getDifTime() - bestPoint[2]) / (worstPoint[2] - bestPoint[2]), (double) (this.getNumRoutes() - bestPoint[3]) / (worstPoint[3] - bestPoint[3]))));
	}
	public double distanceL2To(double[] bestPoint){
		return Math.sqrt(Math.pow(this.totalDist-bestPoint[0], 2)+Math.pow(this.getDistanceLongestRoute()-bestPoint[1], 2)+Math.pow(this.getDifTime()-bestPoint[2], 2)+Math.pow(this.getNumRoutes()-bestPoint[3], 2));
	}
	/* VERSIÓN ORIGINAL:
	 public boolean dominates(double f1, double f2, double f3, int f4) {
		double diffDist = getTotalDist() - f1;
		double diffLongestRoute = getDistanceLongestRoute() - f2;
		double diffTime = getDifTime() - f3;
		int diffV = this.getNumRoutes() - f4;
		boolean d1 = (diffDist <= -EPSILON);
		boolean d2 = (diffLongestRoute <= -EPSILON);
		boolean d3 = (diffTime <= -EPSILON);
		return d1 && d2 && d3 && (diffV <= -EPSILON);
	}*/
	
	public boolean dominates(double f1, double f2, double f3, int f4) {
		boolean dominates = false;
			if (getTotalDist() - f1<=-EPSILON) {
				if (getDistanceLongestRoute() - f2<=-EPSILON) {
					if ( getDifTime() - f3<=-EPSILON) {
						if (this.getNumRoutes() - f4<=-EPSILON) {
							dominates = true;
						}
					}
				}
			}
		return dominates;
	}
	public boolean isDominated() { // devuelve TRUE si hay alguna solución de la
									// frontera actual que la domina.
		boolean dom = false;
		if (this != null) {
			for (int p = 0; p < Pareto.size(); p++) {
				if (Pareto.getFrontAt(p).dominates(this)) {
					return true;
				}
			}
		}
		return dom;
	}

	public double getOF() {
		if (currentOF == ObjFunct.TOTAL_DIST) {
			return totalDist;
		} else if (currentOF == ObjFunct.LONGEST_ROUTE) {
			return routes[longestRoute].getDistance();
		} else if (currentOF == ObjFunct.TIME) {
			return getDifTime();
		}else if (currentOF == ObjFunct.WIERZBICKI){
			return C1_WCK.evalWierzbicki(totalDist, this.getDistanceLongestRoute(), getDifTime(), this.getNumRoutes());
		}
		return -1;
	}

	public double evalMoveSubRoute(int route, int start, int end, int dst) {
		return routes[route].evalMoveSubRoute(start, end, dst);
	}

	public double evalTimeMoveSubRoute(int route, int start, int end, int dst) {
		return routes[route].evalTimeMoveSubRoute(start, end, dst);
	}

	public void moveSubRoute(int route, int start, int end, int dst, double newDistance, double newTime) {
		double before = routes[route].getDistance();
		totalDist -= routes[route].getDistance();
		routes[route].moveSubRoute(start, end, dst, newDistance, newTime);
		totalDist += newDistance;
		updateLongestRoute(route, newDistance, before);
		findTimes();
		/*this.getRoute(route).evaluateNaive();
		if (Math.abs(this.getRoute(route).getDistance()-newDistance)>CVRPInstance.EPSILON){
			System.out.println("error estimación moveSubroute");
		}
		this.getRoute(route).evaluateNaiveTime();
		if (Math.abs(this.getRoute(route).getTime()-newTime)>CVRPInstance.EPSILON){
			System.out.println("error estimación tiempos moveSubroute");
		}*/
	}

	public void removeSubRoute(int r, int start, int end, double newDistance, double newTime) {
		double prevDR = getRoute(r).getDistance();
		this.totalDist = this.totalDist - prevDR + newDistance;
		getRoute(r).removeSubRoute(start, end, newDistance, newTime);
		updateLongestRoute(r, newDistance, prevDR);
		findTimes();
	}

	public void updateLongestRoute(int route, double newDistance, double beforeMove) {
		if (longestRoute < 0) {
			longestRoute = route;
		} else {
			if (longestRoute != route && newDistance > routes[longestRoute].getDistance()) {
				longestRoute = route;
			} else if (longestRoute == route && newDistance < beforeMove) {
				for (int r = 0; r < routes.length; r++) {
					if (r!=longestRoute && routes[r].getDistance() > newDistance) {
						longestRoute = r;
					}
				}
			}
		}
	}

	public int getNumRoutes() {
		return this.numRoutes;
	}

	public void findLongestRoute() {
		double max = Double.MIN_VALUE;
		for (int r = 0; r < numRoutes; r++) {
			if (routes[r].getDistance() > max) {
				this.longestRoute = r;
				max = routes[r].getDistance();
			}
		}
	}

	public void findTimes() {
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		int p = 0;
		for (WCPRoute r:this.routes) {
			if (r.getTime() < min) {
				minTimeR = p;
				min = routes[p].getTime();
			}
			if (routes[p].getTime() > max) {
				maxTimeR = p;
				max = routes[p].getTime();
			}
			p++;
		}
	}

	public void updateTimingRoutes(int route, double newTime) {
		routes[route].setTime(newTime);
		this.findTimes();
	}

	public double difTimeIf(int route, double newTime) {
		if ((route == minTimeR) && (newTime < routes[minTimeR].getTime())) {
			return routes[maxTimeR].getTime() - newTime;
		} else if ((route == maxTimeR) && (newTime > routes[maxTimeR].getTime())) {
			return newTime - routes[minTimeR].getTime();
		}
		int maxR = -1;
		int minR = -1;
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int r = 0; r < numRoutes; r++) {
			if (r != route) {
				if (min > routes[r].getTime()) {
					min = routes[r].getTime();
					minR = r;
				}
				if (max < routes[r].getTime()) {
					max = routes[r].getTime();
					maxR = r;
				}
			} else {
				if (newTime < min) {
					minR = route;
					min = newTime;
				}
				if (newTime > max) {
					maxR = route;
					max = newTime;
				}
			}
		}
		if (maxR == -1) {
			maxR = maxTimeR;
		}
		if (minR == -1) {
			minR = minTimeR;
		}
		return routes[maxR].getTime() - routes[minR].getTime();
	}

	public double difTimeIf(int r1, double newTime1, int r2, double newTime2) {
		if ((r1 == minTimeR) && (newTime1 < routes[minTimeR].getTime())) {
			return routes[maxTimeR].getTime() - newTime1;
		} else if ((r1 == maxTimeR) && (newTime1 > routes[maxTimeR].getTime())) {
			return newTime1 - routes[minTimeR].getTime();
		}

		if ((r2 == minTimeR) && (newTime2 < routes[minTimeR].getTime())) {
			return routes[maxTimeR].getTime() - newTime2;
		} else if ((r2 == maxTimeR) && (newTime2 > routes[maxTimeR].getTime())) {
			return newTime2 - routes[minTimeR].getTime();
		}
		int maxR = -1;
		int minR = -1;
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int r = 0; r < numRoutes; r++) {
			if (r != r1) {
				if (min > routes[r].getTime()) {
					min = routes[r].getTime();
					minR = r;
				}
				if (max < routes[r].getTime()) {
					max = routes[r].getTime();
					maxR = r;
				}
			} else if (r == r1) {
				if (newTime1 < min) {
					minR = r1;
					min = newTime1;
				}
				if (newTime1 > max) {
					maxR = r1;
					max = newTime1;
				}
			} else if (r == r2) {
				if (newTime2 < min) {
					minR = r2;
					min = newTime2;
				}
				if (newTime2 > max) {
					maxR = r2;
					max = newTime2;
				}
			}
		}
		if (maxR == -1) {
			maxR = maxTimeR;
		}
		if (minR == -1) {
			minR = minTimeR;
		}
		return routes[maxR].getTime() - routes[minR].getTime();
	}

	public double evalMove2Opt(int route, int start, int end) {
		return routes[route].evalMove2Opt(start, end);
	}

	public double evalTimeMove2Opt(int route, int start, int end) {
		return routes[route].evalTimeMove2Opt(start, end);
	}

	public int longestRouteIf(int route, double newDistance, double before) {
		if (longestRoute != route) {
			if (newDistance > routes[longestRoute].getDistance()) {
				return route;
			}
		} else if (longestRoute == route) {
			if (newDistance < routes[longestRoute].getDistance()) {
				double max = newDistance;
				int rt = route;
				for (int r = 0; r < this.numRoutes; r++) {
					if (r != route) {
						if (routes[r].getDistance() > max) {
							rt = r;
							max = routes[r].getDistance();
						}
					}
				}
				return rt;
			}
		}
		return longestRoute;
	}

	public double[] evalMoveK(int routeFROM, int routeTO, int start, int end, int dst) {
		if (routeFROM == routeTO) {
			double d = evalMoveSubRoute(routeFROM, start, end, dst);
			return new double[] { d, d };
		}
		Route r1 = routes[routeFROM];
		Route r2 = routes[routeTO];
		double distStartEnd = r1.distanceBetween(start, end);
		double extractCost = instance.getDistance(r1.getNodeAt(start - 1), r1.getNodeAt(start))
				+ instance.getDistance(r1.getNodeAt(end), r1.getNodeAt(end + 1))
				- instance.getDistance(r1.getNodeAt(start - 1), r1.getNodeAt(end + 1)) + distStartEnd;
		double insertCost = instance.getDistance(r2.getNodeAt(dst - 1), r1.getNodeAt(start))
				+ instance.getDistance(r1.getNodeAt(end), r2.getNodeAt(dst))
				- instance.getDistance(r2.getNodeAt(dst - 1), r2.getNodeAt(dst)) + distStartEnd;
		// COMPROBACION
		/*
		 * Route rt1 = new Route(this.routes[routeFROM]);
		 * 
		 * List<Integer> slR1 = new ArrayList<>(rt1.getSubRoute(start, end));
		 * rt1.removeSubRoute(start, end, 0); rt1.evaluateNaive(); Route rt2 =
		 * new Route(this.routes[routeTO]); rt2.addSubRoute(slR1, dst, 0);
		 * rt2.evaluateNaive(); if (Math.abs(r1.getDistance() -
		 * extractCost-rt1.distance)>CVRPInstance.EPSILON){ System.out.println(
		 * "ERROR movimiento k en R1 con diferencia = "
		 * +Math.abs(r1.getDistance() - extractCost-rt1.distance)); } if
		 * (Math.abs(r2.getDistance() +
		 * insertCost-rt2.distance)>CVRPInstance.EPSILON){ System.out.println(
		 * "ERROR movimiento k en R2 con diferencia = "
		 * +Math.abs(r2.getDistance() + insertCost-rt2.distance)); }
		 */
		return new double[] { r1.getDistance() - extractCost, r2.getDistance() + insertCost };

	}

	public double[] evalTimeMoveK(int routeFROM, int routeTO, int start, int end, int dst) {
		if (routeFROM == routeTO) {
			double d = evalTimeMoveSubRoute(routeFROM, start, end, dst);
			return new double[] { d, d };
		}
		WCPRoute r1 = routes[routeFROM];
		WCPRoute r2 = routes[routeTO];
		double distStartEnd = r1.timeBetween(start, end);
		double extractCost = instance.getTime(r1.getNodeAt(start - 1), r1.getNodeAt(start))
				+ instance.getTime(r1.getNodeAt(end), r1.getNodeAt(end + 1))
				- instance.getTime(r1.getNodeAt(start - 1), r1.getNodeAt(end + 1)) + distStartEnd;
		double insertCost = instance.getTime(r2.getNodeAt(dst - 1), r1.getNodeAt(start))
				+ instance.getTime(r1.getNodeAt(end), r2.getNodeAt(dst))
				- instance.getTime(r2.getNodeAt(dst - 1), r2.getNodeAt(dst)) + distStartEnd;
		// COMPROBACION
		/*
		  WCPRoute rt1 = new WCPRoute(this.routes[routeFROM]);
		  
		  List<Integer> slR1 = new ArrayList<>(rt1.getSubRoute(start, end));
		  rt1.removeSubRoute(start,end,0,rt1.getTime()-extractCost);
		  rt1.evaluateNaive(); 
		  WCPRoute rt2 = new WCPRoute(this.routes[routeTO]); 
		  rt2.addSubRoute(slR1, dst, rt2.getTime()+insertCost);
		  rt2.evaluateNaiveTime();
		  if (Math.abs(r1.getTime()-extractCost - rt1.getTime())>CVRPInstance.EPSILON){ 
			  System.out.println("ERROR movimiento k en R1 con diferencia = "+Math.abs(r1.getTime() - rt1.getTime())); 
			  }
		  if (Math.abs(r2.getTime()+insertCost - rt2.getTime())>CVRPInstance.EPSILON){
			  System.out.println("ERROR movimiento k en R2 con diferencia = "+Math.abs(r2.getTime() - rt2.getTime())); 
			  }*/
		 
		return new double[] { r1.getTime() - extractCost, r2.getTime() + insertCost };

	}

	public void moveK(int routeFROM, int routeTO, int start, int end, int dst, double[] newDistance, double[] newTime) {
		this.moveK(routeFROM, routeTO, start, end, dst, newDistance);
		updateTimingRoutes(routeFROM, newTime[0]);
		updateTimingRoutes(routeTO, newTime[1]);
		/*this.getRoute(routeFROM).evaluateNaive();
		if (Math.abs(this.getRoute(routeFROM).getDistance()-newDistance[0])>CVRPInstance.EPSILON){
			System.out.println("error estimación moveFROMroute");
		}
		this.getRoute(routeFROM).evaluateNaiveTime();
		if (Math.abs(this.getRoute(routeFROM).getTime()-newTime[0])>CVRPInstance.EPSILON){
			System.out.println("error estimación tiempo moveFROMroute");
		}
		this.getRoute(routeTO).evaluateNaive();
		if (Math.abs(this.getRoute(routeTO).getDistance()-newDistance[1])>CVRPInstance.EPSILON){
			System.out.println("error estimación moveTOroute");
		}
		this.getRoute(routeTO).evaluateNaiveTime();
		if (Math.abs(this.getRoute(routeTO).getTime()-newTime[1])>CVRPInstance.EPSILON){
			System.out.println("error estimación tiempo moveTOroute");
		}*/
	}

	public void moveK(int routeFROM, int routeTO, int start, int end, int dst, double[] newDistance) {
		Route r1 = routes[routeFROM];
		Route r2 = routes[routeTO];
		double before1 = r1.getDistance();
		double before2 = r2.getDistance();
		List<Integer> slR1 = new ArrayList<>(r1.getSubRoute(start, end));
		totalDist -= (before1 + before2);
		r1.removeSubRoute(start, end, newDistance[0]);
		r2.addSubRoute(slR1, dst, newDistance[1]);
		totalDist += (newDistance[0] + newDistance[1]);
		updateLongestRoute(routeFROM, newDistance[0], before1);
		updateLongestRoute(routeTO, newDistance[1], before2);
	}

	// Si r1== r2 devuelve sólo la longitud de la ruta afectada, r1.
	public double[] evalInterchange(int route1, int route2, int s1, int e1, int s2, int e2) {
		WCPRoute r1 = routes[route1];
		WCPRoute r2 = routes[route2];
		double distS1E1 = r1.distanceBetween(s1, e1);
		double distS2E2 = r2.distanceBetween(s2, e2);
		double extractCost1 = 0;
		double extractCost2 = 0;
		double insertCost1 = 0;
		double insertCost2 = 0;
		if (route1 == route2) {
			// cuando las rutas son iguales, los costes acumulados NO varían!
			if (s1 < s2) {
				if (e1 + 1 == s2) {
					double cost = this.evalMoveSubRoute(route1, s1, e1, s2);
					return new double[]{cost, cost};
				} else {
					extractCost1 = instance.getDistance(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
							+ instance.getDistance(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1))
							+ instance.getDistance(r1.getNodeAt(s2 - 1), r1.getNodeAt(s2))
							+ instance.getDistance(r1.getNodeAt(e2), r1.getNodeAt(e2 + 1));
					insertCost1 = instance.getDistance(r1.getNodeAt(s1 - 1), r1.getNodeAt(s2))
							+ instance.getDistance(r1.getNodeAt(e2), r1.getNodeAt(e1 + 1))
							+ instance.getDistance(r1.getNodeAt(e1), r1.getNodeAt(e2 + 1))
							+ instance.getDistance(r1.getNodeAt(s2 - 1), r1.getNodeAt(s1));
				}
			} else {
				if (e2 + 1 == s1) {
					double cost = this.evalMoveSubRoute(route2, s2, e2, s1);
					return new double[]{cost, cost};
				} else {
					extractCost1 = instance.getDistance(r1.getNodeAt(s2 - 1), r1.getNodeAt(s2))
							+ instance.getDistance(r1.getNodeAt(e2), r1.getNodeAt(e2 + 1))
							+ instance.getDistance(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
							+ instance.getDistance(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1));
					insertCost1 = instance.getDistance(r1.getNodeAt(s2 - 1), r1.getNodeAt(s1))
							+ instance.getDistance(r1.getNodeAt(e1), r1.getNodeAt(e2 + 1))
							+ instance.getDistance(r1.getNodeAt(e2), r1.getNodeAt(e1 + 1))
							+ instance.getDistance(r1.getNodeAt(s1 - 1), r1.getNodeAt(s2));
				}
			}

			return new double[] { r1.getDistance() - extractCost1 + insertCost1,
					r1.getDistance() - extractCost1 + insertCost1 };
		} else {
			extractCost1 = instance.getDistance(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
					+ instance.getDistance(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1)) - distS1E1;
			extractCost2 = instance.getDistance(r2.getNodeAt(s2 - 1), r2.getNodeAt(s2))
					+ instance.getDistance(r2.getNodeAt(e2), r2.getNodeAt(e2 + 1)) - distS2E2;
			insertCost1 = instance.getDistance(r1.getNodeAt(s1 - 1), r2.getNodeAt(s2))
					+ instance.getDistance(r2.getNodeAt(e2), r1.getNodeAt(e1 + 1)) + distS2E2;
			insertCost2 = instance.getDistance(r2.getNodeAt(s2 - 1), r1.getNodeAt(s1))
					+ instance.getDistance(r1.getNodeAt(e1), r2.getNodeAt(e2 + 1)) + distS1E1;
		}
		return new double[] { r1.getDistance() - extractCost1 + insertCost1,
				r2.getDistance() - extractCost2 + insertCost2 };
	}

	public double[] evalTimeInterchange(int route1, int route2, int s1, int e1, int s2, int e2) {
		WCPRoute r1 = routes[route1];
		WCPRoute r2 = routes[route2];
		double distS1E1 = r1.timeBetween(s1, e1);
		double distS2E2 = r2.timeBetween(s2, e2);
		double extractCost1 = 0;
		double extractCost2 = 0;
		double insertCost1 = 0;
		double insertCost2 = 0;
		if (route1 == route2) {
			// cuando las rutas son iguales, los costes acumulados NO varían!
			if (s1 < s2) {
				if (e1 + 1 == s2) {
					double cost = this.evalTimeMoveSubRoute(route1, s1, e1, s2);
					return new double[]{cost, cost};
				} else {
					extractCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
							+ instance.getTime(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1))
							+ instance.getTime(r1.getNodeAt(s2 - 1), r1.getNodeAt(s2))
							+ instance.getTime(r1.getNodeAt(e2), r1.getNodeAt(e2 + 1));
					insertCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s2))
							+ instance.getTime(r1.getNodeAt(e2), r1.getNodeAt(e1 + 1))
							+ instance.getTime(r1.getNodeAt(e1), r1.getNodeAt(e2 + 1))
							+ instance.getTime(r1.getNodeAt(s2 - 1), r1.getNodeAt(s1));
				}
			} else {
				if (e2 + 1 == s1) {
					double cost = this.evalTimeMoveSubRoute(route2, s2, e2, s1);
					return new double[]{cost, cost};
				} else {
					extractCost1 = instance.getTime(r1.getNodeAt(s2 - 1), r1.getNodeAt(s2))
							+ instance.getTime(r1.getNodeAt(e2), r1.getNodeAt(e2 + 1))
							+ instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
							+ instance.getTime(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1));
					insertCost1 = instance.getTime(r1.getNodeAt(s2 - 1), r1.getNodeAt(s1))
							+ instance.getTime(r1.getNodeAt(e1), r1.getNodeAt(e2 + 1))
							+ instance.getTime(r1.getNodeAt(e2), r1.getNodeAt(e1 + 1))
							+ instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s2));
				}
			}

			return new double[] { r1.getTime() - extractCost1 + insertCost1,
					r1.getTime() - extractCost1 + insertCost1 };
		} else {
			extractCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1))
					+ instance.getTime(r1.getNodeAt(e1), r1.getNodeAt(e1 + 1)) + distS1E1;
			extractCost2 = instance.getTime(r2.getNodeAt(s2 - 1), r2.getNodeAt(s2))
					+ instance.getTime(r2.getNodeAt(e2), r2.getNodeAt(e2 + 1)) + distS2E2;
			insertCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r2.getNodeAt(s2))
					+ instance.getTime(r2.getNodeAt(e2), r1.getNodeAt(e1 + 1)) + distS2E2;
			insertCost2 = instance.getTime(r2.getNodeAt(s2 - 1), r1.getNodeAt(s1))
					+ instance.getTime(r1.getNodeAt(e1), r2.getNodeAt(e2 + 1)) + distS1E1;
		}
		
		
		//comprobamos
		/*WCPRoute rt1 = new WCPRoute(r1);
		WCPRoute rt2= new WCPRoute(r2);
		List<Integer> slR1 = new ArrayList<>(rt1.getSubRoute(s1, e1));
		List<Integer> slR2 = new ArrayList<>(rt2.getSubRoute(s2, e2));
		
		rt1.addSubRoute(slR2, s1, 0, r1.getTime() - extractCost1 + insertCost1);
		rt2.addSubRoute(slR1, s2, 0, r2.getTime() - extractCost2 + insertCost2);
		rt1.removeAll(slR1);// actualiza la demanda!
		rt2.removeAll(slR2);
		rt1.evaluateNaiveTime();
		if (Math.abs(rt1.getTime()-(r1.getTime() - extractCost1 + insertCost1))>CVRPInstance.EPSILON){
			System.out.print("error estimacion intercambio tiempos r1");
		}
		rt2.evaluateNaiveTime();
		if (Math.abs(rt2.getTime()-(r2.getTime() - extractCost2 + insertCost2))>CVRPInstance.EPSILON){
			System.out.print("error estimacion tiempos intercambio r2");
		}*/
		return new double[] { r1.getTime() - extractCost1 + insertCost1,
				r2.getTime() - extractCost2 + insertCost2 };	}

	public void moveInterchange(int route1, int route2, int s1, int e1, int s2, int e2, double[] newDistance,
			double[] newTime) {
		WCPRoute r1 = routes[route1];
		if (route1 != route2) {
			WCPRoute r2 = routes[route2];
			double before1 = r1.getDistance();
			double before2 = r2.getDistance();
			List<Integer> slR1 = new ArrayList<>(r1.getSubRoute(s1, e1));
			List<Integer> slR2 = new ArrayList<>(r2.getSubRoute(s2, e2));
			totalDist -= (r1.getDistance() + r2.getDistance());
			r1.addSubRoute(slR2, s1, newDistance[0], newTime[0]);
			r2.addSubRoute(slR1, s2, newDistance[1], newTime[1]);
			r1.removeAll(slR1);// actualiza la demanda!
			r2.removeAll(slR2);
			totalDist += (newDistance[0] + newDistance[1]);
			updateLongestRoute(route1, newDistance[0], before1);
			updateLongestRoute(route2, newDistance[1], before2);
			findTimes();
			//COMPROBAMOS:
			/*r1.evaluateNaive();
			r2.evaluateNaive();
			if (Math.abs(r1.getDistance()-newDistance[0])>CVRPInstance.EPSILON){
				System.out.print("error estimacion intercambio r1");
			}
			if (Math.abs(r2.getDistance()-newDistance[1])>CVRPInstance.EPSILON){
				System.out.print("error estimacion intercambio r2");
			}
			r1.evaluateNaiveTime();
			if (Math.abs(r1.getTime()-newTime[0])>CVRPInstance.EPSILON){
				System.out.print("error estimacion intercambio tiempos r1");
			}
			r2.evaluateNaiveTime();
			if (Math.abs(r2.getTime()-newTime[1])>CVRPInstance.EPSILON){
				System.out.print("error estimacion tiempos intercambio r2");
			}*/
			
		} else if (newDistance[0] < Double.MAX_VALUE) {
			double before1 = r1.getDistance();
			List<Integer> slR1 = new ArrayList<>(r1.getSubRoute(s1, e1));
			List<Integer> slR2 = new ArrayList<>(r1.getSubRoute(s2, e2));
			totalDist -= r1.getDistance();
			if (e1 < s2) {
				r1.removeSubRoute(s2, e2);
				r1.addSubRoute(slR2, s1, before1);
				r1.removeSubRoute(s1 + slR2.size(), e1 + slR2.size());
				r1.addSubRoute(slR1, s2, newDistance[0]);
			} else {
				r1.removeSubRoute(s1, e1);
				r1.addSubRoute(slR1, s2, before1);
				r1.removeSubRoute(s2 + slR1.size(), e2 + slR1.size());
				r1.addSubRoute(slR2, s1, newDistance[0]);
			}
			totalDist += newDistance[0];
			r1.setTime(newTime[0]);
			updateLongestRoute(route1, newDistance[0], before1);
			updateTimingRoutes(route1, newTime[0]);
			//COMPROBAMOS:
			/*r1.evaluateNaive();
			if (Math.abs(r1.getDistance()-newDistance[0])>CVRPInstance.EPSILON){
				System.out.print("error estimacion intercambio r1");
			}
			
			r1.evaluateNaiveTime();
			if (Math.abs(r1.getTime()-newTime[0])>CVRPInstance.EPSILON){
				System.out.print("error estimacion intercambio tiempos r1");
			}*/
			
		}
	}

	public void addNode(int v, int r, int p, double incDistance) {
		double before = routes[r].getDistance();
		routes[r].addNode(v, p, incDistance);
		totalDist += incDistance;
		this.routes[r].setTime(routes[r].evalTimeAddNode(v, p));
		findTimes();
		updateLongestRoute(r, before + incDistance, before);
	}

	public boolean isFeasibleAdd(List<Integer> subList, int r) {
		double cumDemand = routes[r].getDemand();
		for (int v : subList) {
			cumDemand += instance.getNode(v).getDemand();
			if (cumDemand > instance.getCapacity()) {
				return false;
			}
		}
		return true;
	}

	public boolean isFeasibleInterchange(int r1, int v1, int r2, int v2) {
		if (r1 == r2) {
			return true;
		} else {
			return ((routes[r1].getDemand() + instance.getNode(v2).getDemand()
					- instance.getNode(v1).getDemand()) <= instance.getCapacity())
					&& ((routes[r2].getDemand() + instance.getNode(v1).getDemand()
							- instance.getNode(v2).getDemand()) <= instance.getCapacity());
		}
	}

	public double evalTotalDistNaive() {
		totalDist = 0;
		for (Route r : routes) {
			for (int i = 0; i < r.size() - 1; i++) {
				totalDist += instance.getDistance(r.getNodeAt(i), r.getNodeAt(i + 1));
			}
		}
		return totalDist;
	}

	public void move2Opt(int route, int start, int end, double newDistance, double newTime) {
		double before = routes[route].getDistance();
		totalDist -= before;
		routes[route].move2Opt(start, end, newDistance);
		totalDist += newDistance;
		updateLongestRoute(route, newDistance, before);
		updateTimingRoutes(route, newTime);
		//COMPROBAMOS:
		/*this.getRoute(route).evaluateNaive();
		if (Math.abs(this.getRoute(route).getDistance()-newDistance)>CVRPInstance.EPSILON){
			System.out.print("error estimacion 2opt");
		}
		
		this.getRoute(route).evaluateNaiveTime();
		if (Math.abs(this.getRoute(route).getTime()-newTime)>CVRPInstance.EPSILON){
			System.out.print("error estimacion tiempos 2opt");
		}*/
	}

	public double[] evalTimeCross(int route1, int route2, int s1, int s2) {
		WCPRoute r1 = routes[route1];
		WCPRoute r2 = routes[route2];
		double cumDist1 = r1.timeBetween(s1, r1.size() - 1);
		double cumDist2 = r2.timeBetween(s2, r2.size() - 1);
		double extractCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r1.getNodeAt(s1)) + cumDist1;
		double extractCost2 = instance.getTime(r2.getNodeAt(s2 - 1), r2.getNodeAt(s2)) + cumDist2;
		double insertCost1 = instance.getTime(r1.getNodeAt(s1 - 1), r2.getNodeAt(s2)) + cumDist2;
		double insertCost2 = instance.getTime(r2.getNodeAt(s2 - 1), r1.getNodeAt(s1)) + cumDist1;
		return new double[] { r1.getTime() - extractCost1 + insertCost1, r1.getTime() - extractCost2 + insertCost2 };
	}

	public void moveCross(int route1, int route2, int s1, int s2, double[] newDistance) {
		Route r1 = routes[route1];
		Route r2 = routes[route2];
		List<Integer> slR1 = new ArrayList<>(r1.getSubRoute(s1, r1.size() - 1));
		List<Integer> slR2 = new ArrayList<>(r2.getSubRoute(s2, r2.size() - 1));
		r1.removeSubRoute(s1);
		r2.removeSubRoute(s2);
		r1.addSubRoute(slR2, s1, newDistance[0]);
		r2.addSubRoute(slR1, s2, newDistance[1]);
	}

	public void addNode(int v, int r, double newTime) {
		WCPRoute route = routes[r];
		double before = route.getDistance();
		totalDist -= route.getDistance();
		updateTimingRoutes(r, newTime);
		route.addNode(v);
		totalDist += route.getDistance();
		updateLongestRoute(r, route.getDistance(), before);

	}

	public void addNode(int v, int r) {
		if (r > this.numRoutes - 1) {
			numRoutes = r+1;
			WCPRoute[] newRoutes = new WCPRoute[numRoutes];
			for (int rt = 0; rt < numRoutes-1; rt++) {
				newRoutes[rt] = new WCPRoute(this.routes[rt]);
			}
			newRoutes[numRoutes-1] = new WCPRoute(instance);
			this.routes = newRoutes;
			findTimes();
		}
		WCPRoute route = routes[r];
		double before = route.getDistance();
		//double newTime = route.getTime() + route.evalTimeAddNode(v, route.size() - 1);
		//System.out.println("tiempo previo a la inserción:"+ this.getDifTime());
		totalDist -= route.getDistance();
		route.addNode(v); //ya actualiza el tiempo pero no el índice de la ruta más larga/corta, para eso necesito findTimes!
		
		//updateTimingRoutes(r,newTime);
		this.findTimes();
		//System.out.println("tiempo posterior a la inserción:"+ this.getDifTime());
		totalDist += route.getDistance();
		updateLongestRoute(r, route.getDistance(), before);
	}

	public boolean isFeasibleAdd(int v, int r) {
		return (routes[r].getDemand() + instance.getNode(v).getDemand()) <= instance.getCapacity();
	}

	public void addNode(int v, int r, int p, double incDistance, double incTime) {
		double before = routes[r].getDistance();
		routes[r].addNode(v, p, incDistance);
		totalDist += incDistance;
		updateLongestRoute(r, before + incDistance, before);
		//System.out.println("tiempo previo a la inserción:"+ this.getDifTime());
		updateTimingRoutes(r, routes[r].getTime() + incTime);
		//System.out.println("tiempo posterior a la inserción:"+ this.getDifTime());
	}

	public void incrementVehicle() {
		this.numRoutes = this.numRoutes + 1;
	}

	public double evalTimeAddNode(int v, int r, int p) {
		return routes[r].evalTimeAddNode(v, p);
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof WCPSolution) {
			WCPSolution that = (WCPSolution) other;
			result = (this.totalDist == that.totalDist
					&& this.getDistanceLongestRoute() == that.getDistanceLongestRoute()
					&& this.getDifTime() == that.getDifTime());
		}
		return result;
	}

	public String saveNEVAformat(String path) {
		StringBuilder stb = new StringBuilder();
		int index = 0;
		for (WCPRoute r : routes) {
			for (int p = 0; p < r.size(); p++) {
				stb.append(r.getNodeAt(p)).append(" ");
			}
			stb.append("\n");
			r.saveCOORDtxt(path + "coord" + index + ".txt");
			r.savetxtLINE(path + "seq" + index + ".rts");
			index++;
		}
		return stb.toString();
	}

	public void saveroutesToNEVAFile(String path) {
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(this.saveNEVAformat(path));
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		// int idx = 0;
		stb.append("F1 = ").append(totalDist).append("\t F2 = ").append(routes[longestRoute].getDistance())
				.append("\t F3 = ").append(this.getDifTime()).append("\t F4 = ").append(this.numRoutes).append("\n");
		/*
		 * for (Route route : routes) { stb.append("Route "
		 * ).append(idx).append(":"); stb.append(route).append("\n"); idx++; }
		 */
		return stb.toString();
	}

	public int findMinDemandRoute() {
		double minDemand = Double.MAX_VALUE;
		int r = -1;
		for (int p = 0; p < this.numRoutes; p++) {
			if (this.routes[p].getDemand() < minDemand) {
				minDemand = this.routes[p].getDemand();
				r = p;
			}
		}
		return r;
	}

	public void moveInterchangeP(int route1, int route2, int v1, int p1, int v2, int p2, double[] newDistance,
			double[] bestTime) {
		WCPRoute r1 = this.getRoute(route1);
		double before1 = r1.getDistance();
		WCPRoute r2 = this.getRoute(route2);
		double before2 = r2.getDistance();
		double inc = newDistance[0] + newDistance[1] - (before1 + before2);

		int posV1 = this.routes[route1].route.indexOf(v1);
		int posV2 = this.routes[route2].route.indexOf(v2);
		r2.removeSubRoute(posV2, posV2);
		r1.removeSubRoute(posV1, posV1);
		if (p2 <= posV2) {
			r2.addNode(v1, p2, newDistance[1] - before2);
		} else {
			r2.addNode(v1, p2 - 1, newDistance[1] - before2);
		}
		if (p1 <= posV1) {
			r1.addNode(v2, p1, newDistance[0] - before1);
		} else {
			r1.addNode(v2, p1 - 1, newDistance[0] - before1);
		}
		/*
		 * r1.evaluateNaive(); if(Math.abs(r1.distance-newDistance[0])>0.1){
		 * System.out.println("REVISAR 3: cálculo de r1"); } r2.evaluateNaive();
		 * if(Math.abs(r2.distance-newDistance[1])>0.1){ System.out.println(
		 * "REVISAR 4: cálculo de r2"); }
		 */
		this.totalDist += inc;
		updateTimingRoutes(route1, bestTime[0]);
		updateTimingRoutes(route2, bestTime[1]);
		this.updateLongestRoute(route1, newDistance[0], before1);
		this.updateLongestRoute(route2, newDistance[1], before2);
	}

	public double[] evalTimeInterchangeP(int route1, int route2, int v1, int p1, int v2, int p2) {
		// se supone route1 != route2 y que quiero insertar el nodo v2 en la
		// posición p1 de la ruta route1
		WCPRoute r1 = routes[route1];
		int posV1 = r1.route.indexOf(v1);
		WCPRoute r2 = routes[route2];
		int posV2 = r2.route.indexOf(v2);
		if (posV1 < 0 || posV2 < 0) {
			System.out.println("Revisar pertenencia");
		}
		if (p1 > r1.size() - 1 || p2 > r2.size() - 1) {
			System.out.println("revisar punto de inserción");
		}
		double extractCost1 = 0;
		double extractCost2 = 0;
		double insertCost1 = 0;
		double insertCost2 = 0;
		extractCost1 = instance.getTime(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1))
				+ instance.getTime(r1.getNodeAt(posV1), r1.getNodeAt(posV1 + 1))
				- instance.getTime(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1 + 1));
		extractCost2 = instance.getTime(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2))
				+ instance.getTime(r2.getNodeAt(posV2), r2.getNodeAt(posV2 + 1))
				- instance.getTime(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2 + 1));
		if (p2 == posV2 + 1 || p2 == posV2) {
			insertCost2 = instance.getTime(r2.getNodeAt(posV2 - 1), v1) + instance.getTime(v1, r2.getNodeAt(posV2 + 1))
					- instance.getTime(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2 + 1));
		} else {
			insertCost2 = this.evalTimeAddNode(v1, route2, p2);
		}
		if (p1 == posV1 || p1 == posV1 + 1) {
			insertCost1 = instance.getTime(r1.getNodeAt(posV1 - 1), v2) + instance.getTime(v2, r1.getNodeAt(posV1 + 1))
					- instance.getTime(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1 + 1));
		} else {
			insertCost1 = this.evalTimeAddNode(v2, route1, p1);
		}
		return new double[] { r1.getTime() - extractCost1 + insertCost1, r2.getTime() - extractCost2 + insertCost2 };
	}

	public double[] evalInterchangeP(int route1, int route2, int v1, int p1, int v2, int p2) {
		// se supone route1 != route2 y que quiero insertar el nodo v2 en la
		// posición p1 de la ruta route1
		WCPRoute r1 = routes[route1];
		int posV1 = r1.route.indexOf(v1);
		WCPRoute r2 = routes[route2];
		int posV2 = r2.route.indexOf(v2);
		if (posV1 < 0 || posV2 < 0) {
			System.out.println("Revisar pertenencia");
		}
		if (p1 > r1.size() - 1 || p2 > r2.size() - 1) {
			System.out.println("revisar punto de inserción");
		}
		double extractCost1 = 0;
		double extractCost2 = 0;
		double insertCost1 = 0;
		double insertCost2 = 0;
		extractCost1 = instance.getDistance(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1))
				+ instance.getDistance(r1.getNodeAt(posV1), r1.getNodeAt(posV1 + 1))
				- instance.getDistance(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1 + 1));
		extractCost2 = instance.getDistance(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2))
				+ instance.getDistance(r2.getNodeAt(posV2), r2.getNodeAt(posV2 + 1))
				- instance.getDistance(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2 + 1));
		if (p2 == posV2 + 1 || p2 == posV2) {
			insertCost2 = instance.getDistance(r2.getNodeAt(posV2 - 1), v1)
					+ instance.getDistance(v1, r2.getNodeAt(posV2 + 1))
					- instance.getDistance(r2.getNodeAt(posV2 - 1), r2.getNodeAt(posV2 + 1));
		} else {
			insertCost2 = this.evalAddNode(v1, route2, p2);
		}
		if (p1 == posV1 || p1 == posV1 + 1) {
			insertCost1 = instance.getDistance(r1.getNodeAt(posV1 - 1), v2)
					+ instance.getDistance(v2, r1.getNodeAt(posV1 + 1))
					- instance.getDistance(r1.getNodeAt(posV1 - 1), r1.getNodeAt(posV1 + 1));
		} else {
			insertCost1 = this.evalAddNode(v2, route1, p1);
		}
		return new double[] { r1.getDistance() - extractCost1 + insertCost1,
				r2.getDistance() - extractCost2 + insertCost2 };
	}

}
