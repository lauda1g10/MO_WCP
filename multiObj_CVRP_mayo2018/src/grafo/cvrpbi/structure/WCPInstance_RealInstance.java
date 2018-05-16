package grafo.cvrpbi.structure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.EndianUtils;

/**
 * LauraDelgado on 10/11/2017.
 */
public class WCPInstance_RealInstance extends WCPInstance {
	public LinearRegression LR;

	
public WCPInstance_RealInstance(String path) {
		super(path);
	}

	
	@Override
	public void readInstance(String path) {
		String[] div = path.split("/");
		String name = div[div.length - 1];
		String dir = path.replace(name, "");
		readInstance(dir, name);
	}
	private void readFile(String dir, String instancia) throws IOException {
		BufferedReader bfInstance = new BufferedReader(new FileReader(dir + instancia));
		// Archivo
		this.name = bfInstance.readLine().split(":")[1].trim();
		// Coord
		String coord = dir + bfInstance.readLine().split(":")[1].trim();
		// NumRoutes
		vehicles = Integer.parseInt(bfInstance.readLine().split(":")[1].trim());
		// MaxCapacity
		capacity = Double.parseDouble(bfInstance.readLine().split(":")[1].trim());
		// NumContainers
		this.n = Integer.parseInt(bfInstance.readLine().split(":")[1].trim());
		// BinFile
		String binFile = dir + bfInstance.readLine().split(":")[1].trim();
		this.distances = new double[n][n];
		this.time = new double[n][n];
		// Demand (first line empty)
		BufferedReader bfCoord = new BufferedReader(new FileReader(coord));
		bfInstance.readLine();
		this.nodes = new Node[n];
		double demand = 0;
		for (int c = 0; c < n; c++) {
			String lineCoord = bfCoord.readLine().trim();
			String lineInst = bfInstance.readLine().trim();
			String[] tokensCoord = lineCoord.split("\\s+");
			this.nodes[c] = new Node(c, Double.parseDouble(tokensCoord[0]), Double.parseDouble(tokensCoord[1]),
					Double.parseDouble(lineInst));
			demand += nodes[c].getDemand();
		}
		if (vehicles < (int) Math.ceil(demand / this.capacity)) {
			vehicles = (int) Math.ceil(demand / this.capacity);
		}
		bfCoord.close();
		try {
			readBin(binFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bfInstance.close();
	}

	private void readBin(String path) throws IOException {
		InputStream in = new FileInputStream(path);
		Set<int[]> pairErrors = new HashSet<>();
		List<Double> distList = new ArrayList<Double>();
		List<Double> timeList = new ArrayList<Double>();
		for (int i = 0; i < this.nodes.length; i++) {
			for (int j = 0; j < this.nodes.length; j++) {
				double distance = Math.round(EndianUtils.readSwappedDouble(in));
				double time = Math.round(EndianUtils.readSwappedDouble(in) * 60);
				if (time >= 999999999 || distance >= 999999999 || time < 0 || distance < 0) {
					distance = getGeodesicDistance(i, j);
					pairErrors.add(new int[] { i, j });
				}
				else{
					distList.add(distance);
					timeList.add(time);
				}
				this.time[i][j] = (float) time;
				this.distances[i][j] = (float) distance;
			}
		}
		LR = new LinearRegression(distList,timeList);
		// para los casos erróneos, estimamos la distancia geodésica y buscamos,
		// entre las distancias conocidas, el tiempo que se corresponda a la
		// menor diferencia.
		for (int[] pair : pairErrors) {
			double distanceRef = this.distances[pair[0]][pair[1]];
			this.time[pair[0]][pair[1]] = LR.getSR().predict(distanceRef); 
		}
		pairErrors.clear();
		in.close();
	}
	private float getGeodesicDistance(int i, int j) {
		return (float) (Math.acos(Math.sin(nodes[i].getX() * Math.PI / 180)) * Math.sin(nodes[j].getX() * Math.PI / 180)
				+ Math.cos(nodes[i].getX() * Math.PI / 180) * Math.cos(nodes[j].getX() * Math.PI / 180)
						* Math.cos(nodes[i].getY() * Math.PI / 180 - nodes[j].getX() * Math.PI / 180) * 6378);
	}

	private void readInstance(String dir, String name) {
		try {
			readFile(dir, name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		for (int i = 1; i < nodes.length; i++) {
			stb.append("Node ").append(i).append(": ").append(nodes[i]).append("\n");
		}
		stb.append("DISTANCES\n");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				stb.append("[").append(distances[i][j]).append("]");
			}
			stb.append("\n");
		}
		return stb.toString();
	}
}