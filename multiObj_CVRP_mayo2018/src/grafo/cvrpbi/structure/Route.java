package grafo.cvrpbi.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {

	protected List<Integer> route;
	protected double distance; // Total distance
	protected int demand; // Total demand of the nodes
	protected WCPInstance instance; // Instances where the route belongs

	public Route(WCPInstance instance) {
		route = new ArrayList<>();
		route.add(0);
		route.add(0);
		this.instance = instance;
	}

	public Route(Route r) {
		route = new ArrayList<>(r.route);
		this.distance = r.distance;
		this.demand = r.demand;
		this.instance = r.instance;
	}

	public boolean contains(int v) {
		return this.route.contains(v);
	}

	public double evalMoveSubRoute(int start, int end, int dst) {
		double extractCost = 0;
		double insertCost = 0;
		if (dst == end + 1) {
			insertCost = instance.getDistance(getNodeAt(start - 1), getNodeAt(dst))
					+ instance.getDistance(getNodeAt(dst), getNodeAt(start))
					+ instance.getDistance(getNodeAt(end), getNodeAt(dst + 1));
			extractCost = instance.getDistance(getNodeAt(end), getNodeAt(dst))
					+ instance.getDistance(getNodeAt(dst), getNodeAt(dst + 1))
					+ instance.getDistance(getNodeAt(start - 1), getNodeAt(start));
		} else if (dst == start - 1) {
			insertCost = instance.getDistance(getNodeAt(dst - 1), getNodeAt(start))
					+ instance.getDistance(getNodeAt(end), getNodeAt(dst))
					+ instance.getDistance(getNodeAt(dst), getNodeAt(end + 1));
			extractCost = instance.getDistance(getNodeAt(dst - 1), getNodeAt(dst))
					+ instance.getDistance(getNodeAt(dst), getNodeAt(start))
					+ instance.getDistance(getNodeAt(end), getNodeAt(end + 1));
		} else {
			insertCost = instance.getDistance(getNodeAt(dst - 1), getNodeAt(start))
					+ instance.getDistance(getNodeAt(end), getNodeAt(dst))
					+ instance.getDistance(getNodeAt(start - 1), getNodeAt(end + 1));
			extractCost = instance.getDistance(getNodeAt(start - 1), getNodeAt(start))
					+ instance.getDistance(getNodeAt(end), getNodeAt(end + 1))
					+ instance.getDistance(getNodeAt(dst - 1), getNodeAt(dst));
		}
		// COMPROBACIÓN
	/*	Route aux = new Route(this);
		List<Integer> subList = new ArrayList<>(aux.route.subList(start, end + 1));
		if (dst < start) {
			aux.route.removeAll(subList);
			aux.route.addAll(dst, subList);
			if (Math.abs(this.distance - extractCost + insertCost
					- aux.distanceBetween(0, aux.route.size() - 1)) > CVRPInstance.EPSILON) {
				System.out.println("ERROR en moveSubroute si dst<start con diferencia = " + Math
						.abs(this.distance - extractCost + insertCost - aux.distanceBetween(0, aux.route.size() - 1)));
			}
		} else if (dst > end) {
			aux.route.removeAll(subList);
			if (dst == end + 1) {
				aux.route.addAll(dst - subList.size() + 1, subList);
			} else {
				aux.route.addAll(dst - subList.size(), subList);
			}
			if (Math.abs(this.distance - extractCost + insertCost
					- aux.distanceBetween(0, aux.route.size() - 1)) > CVRPInstance.EPSILON) {
				System.out.println("ERROR en moveSubroute si dst>end con diferencia = " + Math
						.abs(this.distance - extractCost + insertCost - aux.distanceBetween(0, aux.route.size() - 1)));
			}
		}*/
		return this.distance - extractCost + insertCost;
	}

	public void moveSubRoute(int start, int end, int dst, double newDistance) {
		List<Integer> subList = new ArrayList<>(route.subList(start, end + 1));// No
																				// es
																				// necesario
																				// actualizar
																				// la
																				// demanda
																				// de
																				// la
																				// ruta.
		if (dst < start) {
			route.removeAll(subList);
			route.addAll(dst, subList);
			this.distance = newDistance;
		} else if (dst > end) {
			route.removeAll(subList);
			if (dst == end + 1) {
				route.addAll(dst - subList.size() + 1, subList);
			} else {
				route.addAll(dst - subList.size(), subList);
			}
			this.distance = newDistance;
		}

		double d1 = this.distanceBetween(0, route.size() - 1);
		if (Math.abs(d1 - newDistance) >= CVRPInstance.EPSILON) {
			System.out.println("ERROR move subroute");
			this.distance = d1;
		}
	}

	public double evalMove2Opt(int start, int end) {
		double extractCost = instance.getDistance(getNodeAt(start - 1), getNodeAt(start))
				+ instance.getDistance(getNodeAt(end), getNodeAt(end + 1));
		double insertCost = instance.getDistance(getNodeAt(start - 1), getNodeAt(end))
				+ instance.getDistance(getNodeAt(start), getNodeAt(end + 1));
		extractCost += this.distanceBetween(start, end);
		insertCost += this.reverseDistanceBetween(start, end);
		// COMPROBACIÓN
	/*	Route rt = new Route(this);
		Collections.reverse(rt.route.subList(start, end + 1));
		if (Math.abs(this.getDistance() - extractCost + insertCost
				- rt.distanceBetween(0, rt.size() - 1)) > CVRPInstance.EPSILON) {
			System.out.println("ERROR en 2-opt con diferencia = "
					+ Math.abs(this.getDistance() - extractCost + insertCost - rt.distanceBetween(0, rt.size() - 1)));
		}*/
		return getDistance() - extractCost + insertCost;
	}

	public void move2Opt(int start, int end, double newDistance) {// No es
																	// necesario
																	// actualizar
																	// la
																	// demanda
																	// de la
																	// ruta.
		Collections.reverse(route.subList(start, end + 1));
		this.distance = newDistance;
	}

	public double distanceBetween(int start, int end) {
		double dist = 0;
		for (int i = start; i < end; i++) {
			dist += instance.getDistance(route.get(i), route.get(i + 1));
		}
		return dist;
	}

	public double demandBetween(int start, int end) {
		double demand = 0;
		for (int i = start; i < end + 1; i++) {
			demand += instance.getNode(route.get(i)).getDemand();
		}
		return demand;
	}

	public double demandBetween(List<Integer> subroute) {
		double demand = 0;
		for (int i : subroute) {
			demand += instance.getNode(i).getDemand();
		}
		return demand;
	}

	public double reverseDistanceBetween(int start, int end) {
		double dist = 0;
		for (int i = end; i > start; i--) {
			dist += instance.getDistance(this.getNodeAt(i), this.getNodeAt(i - 1));
		}
		return dist;
	}

	public List<Integer> getSubRoute(int start, int end) {
		return route.subList(start, end + 1);
	}

	public double evalRemove(int p){
		return this.getDistance()+instance.getDistance(this.getNodeAt(p-1),this.getNodeAt(p+1))-instance.getDistance(this.getNodeAt(p-1),this.getNodeAt(p))-instance.getDistance(this.getNodeAt(p),this.getNodeAt(p+1));
	}
	public void removeSubRoute(int start, int end) {
		demand -= demandBetween(start, end);
		for (int i = 0; i < end - start + 1; i++) {
			route.remove(start);
		}
	}

	public void removeAll(List<Integer> sublist) {
		this.route.removeAll(sublist);
		for (int v : sublist) {
			this.demand -= instance.getNode(v).getDemand();
		}
	}

	public void removeSubRoute(int start) {
		removeSubRoute(start, route.size() - 1);
	}

	public void removeSubRoute(int start, int end, double newDistance) {
		removeSubRoute(start, end);
		this.distance = newDistance;
	}

	public void removeSubRoute(int start, double newDistance) {
		removeSubRoute(start);
		this.distance = newDistance;
	}

	public void addSubRoute(List<Integer> subroute, int dst, double newDistance) {
		route.addAll(dst, subroute);
		this.distance = newDistance;
		demand += demandBetween(subroute);
	}

	public void addNode(int v) {
		this.distance -= instance.getDistance(route.get(route.size() - 2), route.get(route.size() - 1));
		this.distance += instance.getDistance(route.get(route.size() - 2), v);
		this.distance += instance.getDistance(v, route.get(route.size() - 1));
		this.demand += instance.getNode(v).getDemand();
		route.add(route.size() - 1, v);
	}

	public double evalAddNode(int v, int p) {
		return instance.getDistance(route.get(p - 1), v) + instance.getDistance(v, route.get(p))
				- instance.getDistance(route.get(p - 1), route.get(p));
	}

	public void addNode(int v, int p, double incDistance) {
		route.add(p, v);
		this.demand += instance.getNode(v).getDemand();
		this.distance += incDistance;

	}

	public void evaluateNaive() {
		this.distance = this.distanceBetween(0, this.size() - 1);
		this.demand = (int) this.demandBetween(0, this.size() - 1);
	}

	public double getDistance() {
		return distance;
	}

	public int getDemand() {
		return demand;
	}

	public int getNodeAt(int p) {
		if (p > this.size() - 1 || p < 0) {
			System.out.println("ERROR indice" + p);
			return -1;
		}
		return route.get(p);
	}

	public int size() {
		return route.size();
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append(this.instance.getCapacity() - demand).append(", ").append(distance).append(", ")
				.append(route.toString());
		return stb.toString();
	}
}
