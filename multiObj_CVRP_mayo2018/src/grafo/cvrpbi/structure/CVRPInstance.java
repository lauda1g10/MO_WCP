package grafo.cvrpbi.structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import grafo.optilib.structure.Instance;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class CVRPInstance implements Instance {

    public static final double EPSILON = 0.0001;

    protected static String name;					// Instance name
    protected int n;							// Number of nodes
    public int vehicles;
    protected double capacity;					// Maximum capacity of each route
    protected Node[] nodes;					// Nodes indexed by their identifier
    protected double[][] distances;			// Distances between nodes

    public CVRPInstance(String path) {
        readInstance(path);
    }

    public int numVeh(){
    	return vehicles;
    }
    public String getName() {
        return name;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getDistance(int v1, int v2) {
        return distances[v1][v2];
    }

    public Node getNode(int v) {
        return nodes[v];
    }

    public int getNodes() {
        return n;
    }
    
    public List<Node> orderByDemand(){
	List<Node> ordered = new ArrayList<>();
	for(Node n:nodes){
		ordered.add(n);
	}
	ordered.sort(Comparator.comparingDouble(Node::getDemand)); //orden creciente.
	Collections.reverse(ordered);//la pongo en orden decreciente.
	return ordered;
}
    public int getVehicles() {
        return vehicles;
    }
    
    @Override
    public void readInstance(String path) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(path));
            String line = bf.readLine();
            String[] tokens = null;

            while (!line.contains("NODE_COORD_SECTION")) {
                if (line.contains("NAME")) {
                    tokens = line.split(":");
                    name = tokens[1].trim();
                } else if (line.contains("DIMENSION")) {
                    tokens = line.split(":");
                    n = Integer.parseInt(tokens[1].trim());
                } else if (line.contains("CAPACITY")) {
                    tokens = line.split(":");
                    capacity = Double.parseDouble(tokens[1].trim());
                } else if (line.contains("VEHICLE")) {
                    tokens = line.split(":");
                    vehicles = Integer.parseInt(tokens[1].trim());
                }
                line = bf.readLine();
            }
            nodes = new Node[n];
            double xDepot = 0;
            double yDepot = 0;
            for (int i=0;i<n;i++) {
                line = bf.readLine().trim();
                tokens = line.split("\\s+");
                int id = Integer.parseInt(tokens[0])-1;
                double x = Double.parseDouble(tokens[1]);
                double y = Double.parseDouble(tokens[2]);
                // Node with coordinates relative to the depot. Depot is in (0,0)
                if (id == 0) {
                    xDepot = x;
                    yDepot = y;
                }
                Node node = new Node(id, x-xDepot, y-yDepot, 0);
                //Node node = new Node(id, x, y, 0);
                nodes[id] = node;
            }
            bf.readLine();
            for (int i=0;i<n;i++) {
                line = bf.readLine().trim();
                tokens = line.split("\\s+");
                int id = Integer.parseInt(tokens[0])-1;
                int demand = Integer.parseInt(tokens[1]);
                nodes[id].setDemand(demand);
            }
            bf.close();
            this.calculateDistances();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void calculateDistances() {
        distances = new double[n+1][n+1];
        for (int i=0;i<n;i++) {
            Node vi = nodes[i];
            for (int j=0;j<n;j++) {
                Node vj = nodes[j];
                // Euclidean distance
                distances[i][j] = Math.sqrt(Math.pow(vi.getX()-vj.getX(),2)+Math.pow(vi.getY()-vj.getY(),2));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("NAME: ").append(name).append("\n");
        stb.append("NODES: ").append(n).append("\n");
        stb.append("CAPACITY: ").append(capacity).append("\n");
        stb.append("VEHICLES: ").append(vehicles).append("\n");
        stb.append("NODES\n");
        for (int i=1;i<nodes.length;i++) {
            stb.append("Node ").append(i).append(": ").append(nodes[i]).append("\n");
        }
        stb.append("DISTANCES\n");
        for (int i=1;i<=n;i++) {
            for (int j=1;j<=n;j++) {
                stb.append("[").append(distances[i][j]).append("]");
            }
            stb.append("\n");
        }
        return stb.toString();
    }
}
