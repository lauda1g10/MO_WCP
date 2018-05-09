package grafo.cvrpbi.structure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WCPRoute extends Route {

	private double time; // Total distance
	
	
	public WCPRoute(WCPInstance instance) {
		super(instance);
	}
	@Override
	 public double evalAddNode(int v, int p) {
	        return instance.getDistance(route.get(p-1), v) + instance.getDistance(v, route.get(p)) - instance.getDistance(route.get(p-1), route.get(p));
	    }
	public WCPRoute(WCPRoute r) {
		super(r);
		this.time = r.time;
	}
	public void copy(WCPRoute r){
		this.demand = r.demand;
		this.route = new ArrayList<Integer>(r.route);
		this.distance = r.distance;
		this.instance = r.instance;
		this.time = r.time;
	}
	public double evalTimeMoveSubRoute(int start, int end, int dst) {
		double extractCost = 0;
		double insertCost = 0;
		if (dst == end + 1) {
			insertCost = instance.getTime(getNodeAt(start - 1), getNodeAt(dst))
					+ instance.getTime(getNodeAt(dst), getNodeAt(start))
					+ instance.getTime(getNodeAt(end), getNodeAt(dst + 1));
			extractCost = instance.getTime(getNodeAt(end), getNodeAt(dst))
					+ instance.getTime(getNodeAt(dst), getNodeAt(dst + 1))
					+ instance.getTime(getNodeAt(start - 1), getNodeAt(start));
		} else if (dst == start - 1) {
			insertCost = instance.getTime(getNodeAt(dst - 1), getNodeAt(start))
					+ instance.getTime(getNodeAt(end), getNodeAt(dst))
					+ instance.getTime(getNodeAt(dst), getNodeAt(end + 1));
			extractCost = instance.getTime(getNodeAt(dst - 1), getNodeAt(dst))
					+ instance.getTime(getNodeAt(dst), getNodeAt(start))
					+ instance.getTime(getNodeAt(end), getNodeAt(end + 1));
		} else {
			insertCost = instance.getTime(getNodeAt(dst - 1), getNodeAt(start))
					+ instance.getTime(getNodeAt(end), getNodeAt(dst))
					+ instance.getTime(getNodeAt(start - 1), getNodeAt(end + 1));
			extractCost = instance.getTime(getNodeAt(start - 1), getNodeAt(start))
					+ instance.getTime(getNodeAt(end), getNodeAt(end + 1))
					+ instance.getTime(getNodeAt(dst - 1), getNodeAt(dst));
		}
		return this.time - extractCost + insertCost;
	}

	  public void moveSubRoute(int start, int end, int dst, double newDistance, double newTime) {
	       this.moveSubRoute(start, end, dst, newDistance);
	        this.time = newTime;
	    }
	  public double evalRemoveT(int p){
			return this.getTime()+instance.getTime(this.getNodeAt(p-1),this.getNodeAt(p+1))-instance.getTime(this.getNodeAt(p-1),this.getNodeAt(p))-instance.getTime(this.getNodeAt(p),this.getNodeAt(p+1))-WCPInstance.loadingTime;
		}
	  
	  public void move2Opt(int start, int end, double newDistance, double newTime) {//No es necesario actualizar la demanda de la ruta.
	    	this.move2Opt(start, end, newDistance);
	        this.time = newTime;
	    }

	public double getTime() {
		return time;
	}

	public double evalTimeMove2Opt(int start, int end) {
		double extractCost =  instance.getTime(getNodeAt(start - 1), getNodeAt(start))
				+  instance.getTime(getNodeAt(end), getNodeAt(end + 1));
		double insertCost =  instance.getTime(getNodeAt(start - 1), getNodeAt(end))
				+  instance.getTime(getNodeAt(start), getNodeAt(end + 1));
		extractCost += this.timeBetween(start, end);
		insertCost += this.reverseTimeBetween(start, end);
		
		return getTime() - extractCost + insertCost;
	}

	public double timeBetween(int start, int end) {
		double dist = 0;
		for (int i = start; i < end; i++) {
			dist += instance.getTime(route.get(i), route.get(i + 1));
		}
		return dist + (end - start + 1) * WCPInstance.loadingTime;
	}

	public double reverseTimeBetween(int start, int end) {
		double dist = 0;
		for (int i = end; i > start; i--) {
			dist += instance.getTime(this.getNodeAt(i), this.getNodeAt(i - 1));
		}
		return dist + (end - start + 1) * WCPInstance.loadingTime;
	}

	public void removeSubRoute(int start, int end, double newDistance, double newTime) {
		removeSubRoute(start, end, newDistance);
		this.time = newTime;
	}
	public double evalTimeRemove(int p){
		return this.getTime()+instance.getTime(this.getNodeAt(p-1),this.getNodeAt(p+1))-instance.getTime(this.getNodeAt(p-1),this.getNodeAt(p))-instance.getTime(this.getNodeAt(p),this.getNodeAt(p+1));
	}
	
	public void removeSubRoute(int start, double newDistance, double newTime) {
		removeSubRoute(start,newDistance);
		this.time = newTime;
	}

	public void addSubRoute(List<Integer> subroute, int dst, double newDistance, double newTime) {
		this.addSubRoute(subroute, dst, newDistance);
		this.time = newTime;
	}
@Override
	public void addNode(int v) {
	this.time -= instance.getTime(route.get(route.size() - 2), route.get(route.size() - 1));
	this.time += instance.getTime(route.get(route.size() - 2), v)+instance.getTime(v,route.get(route.size() - 1));
	this.time+= WCPInstance.loadingTime;
	super.addNode(v);

		
	}

	public double evalTimeAddNode(int v, int p) {
		return instance.getTime(route.get(p - 1), v) + instance.getTime(v, route.get(p))
				- instance.getTime(route.get(p - 1), route.get(p)) + WCPInstance.loadingTime;
	}
public void setTime(double t){
	this.time = t;
}
	public void addNode(int v, int p, double incDistance, double incTime) {
		this.addNode(v, p, incDistance);	
		this.time += incTime;
	}

	public void evaluateNaive() {
		this.distance = this.distanceBetween(0, this.size() - 1);
		this.demand = 0;
	}

	public String saveRNEVAformat() {
		StringBuilder stb = new StringBuilder();
			for(int p = 0;p<this.size();p++) {
				Node n = instance.getNode(this.getNodeAt(p));
				stb.append(n.getX()).append("\t").append(n.getY()).append("\t").append(1).append("\t").append(0).append("\n");
			}
			stb.append("\n");
		return stb.toString();
	}
	public void savetxtLINE(String path) {
		StringBuilder stb = new StringBuilder();
		int min = instance.getNodes();
		for(int p = 1;p<this.size()-1;p++) {
			if(this.getNodeAt(p)<min) {
				min = this.getNodeAt(p);
			}
		}
		for(int p = 0;p<this.size();p++) {
			if(p==0 || p == this.size()-1) {
				stb.append(this.getNodeAt(p)).append(" ");
				continue;
			}
			stb.append(this.getNodeAt(p)-min).append(" ");
		}
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(stb);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void saveCOORDtxt(String path) {
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(this.saveRNEVAformat());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append(this.instance.getCapacity() - demand).append(", ").append(distance).append(", ").append(time).append(", ").append(route.toString());
		return stb.toString();
	}
}
