package grafo.cvrpbi.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Pareto {

	private static List<WCPSolution> front;
	private static Set<WCPSolution> approxToFront;
	private static double ratio;// por defecto es 0.05

	public static List<WCPSolution> getFront() {
		return front;
	}

	public static void reset() {
		front = new ArrayList<>(1000);
		approxToFront = new HashSet<>(2000);
		ratio = 0.05;
	}

	public static void setRatio(double d) {
		ratio = d;
	}

	public static void resetApproxSet() {
		approxToFront = new HashSet<>(2000);
	}

	public static List<double[]> read(String paretoDir, WCPInstance_RealInstance instance) {
		String path = paretoDir + "/" + instance.getName();
		String[] files = new File(path).list();
		front = new ArrayList<>();
		List<double[]> values = new ArrayList<double[]>();
		for (String file : files) {
			try {
				BufferedReader bf = new BufferedReader(new FileReader(path+"/"+file));
				String line = bf.readLine();
				WCPSolution s = null;
				int indexR = 0;
				while (line != null && line != "") {
					if (line.contains("F1")) {
						if (s != null) {
							front.add(s);
							double[] val = { s.getTotalDist(), s.getDistanceLongestRoute(), s.getDifTime(),
									s.getNumRoutes()};
							values.add(val);
						}
						s = new WCPSolution(instance);
						indexR = 0;
						line = bf.readLine();
						continue;
					}
					if (line.contains("Route")) {
						if(indexR>s.getNumRoutes()) {s.setNumRoutes(indexR);}
						// WCPRoute rt = new WCPRoute(instance);
						String[] tokens = line.replaceAll("\\s+", "").split(":")[1].split(",");
						for (int i = 3; i < tokens.length; i++) {
							if (tokens[i].contains("[") || tokens[i].contains("]")) {
								continue;
							}
							s.addNode((int) Integer.parseInt(tokens[i]), indexR);
						}
						indexR++;
					}
					line = bf.readLine();
				}
				bf.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return values;
	}

	public static boolean checkDominance(double f1, double f2, double f3,int f4) {// Devuelve
																			// true
		// si entra en
		// el conjunto
		// de pareto.
		boolean enter = true;
		for (WCPSolution frontSol : front) {
			if (frontSol.dominates(f1, f2, f3,f4)) {
				return false;
			}
		}
		return enter;
	}

	public static boolean add(WCPSolution newSol) {
		if (newSol == null) {
			return false;
		}
		List<Integer> dominated = new ArrayList<>();
		boolean enter = true;
		int idx = 0;
		// System.out.println(newSol);
		for (WCPSolution frontSol : front) {
			// System.out.println(frontSol);
			if (newSol.equals(frontSol)) {
				enter = false;
			} else if (newSol.dominates(frontSol)) {
				dominated.add(idx);
			} else if (frontSol.dominates(newSol)) {
				enter = false;
				break;
			}
			idx++;
		}
		int removed = 0;
		for (int idRem : dominated) {
			front.remove(idRem - removed);
			removed++;
		}
		if (enter) {
			// front.add(newSol);
			front.add(new WCPSolution(newSol));// ¿no hace falta una copia de
												// esto?
		//	front.sort((Comparator.comparingDouble(WCPSolution::getTotalDist)));// ordena
																				// de
																				// forma
																				// creciente
			// updateApproxSet();
		}

		return enter;
	}

	public static Set<WCPSolution> getApproxToFront() {
		return approxToFront;
	}

	public static boolean checkApproxBetween(double[] FValue, WCPSolution sf1, WCPSolution sf2) {
		double maxF1 = Math.max(sf1.getTotalDist(), sf2.getTotalDist());
		double minF1 = Math.min(sf1.getTotalDist(), sf2.getTotalDist());
		double maxF2 = Math.max(sf1.getDistanceLongestRoute(), sf2.getDistanceLongestRoute());
		double minF2 = Math.min(sf1.getDistanceLongestRoute(), sf2.getDistanceLongestRoute());
		double maxF3 = Math.max(sf1.getDifTime(), sf2.getDifTime());
		double minF3 = Math.min(sf1.getDifTime(), sf2.getDifTime());
		double d1 = ratio*maxF1;
		double d2 = ratio*maxF2;
		double d3 = ratio*maxF3;
		if (maxF1 <= FValue[0] && FValue[0] <= maxF1 + d1 && maxF2 <= FValue[1] && FValue[1] <= minF2 + d2
				&& minF3 <= FValue[2] && FValue[2] <= maxF3 + d3) {
			return true;
		} else if (minF1 <= FValue[0] && FValue[0] <= maxF1 + d1 && minF2 <= FValue[1] && FValue[1] <= minF2 + d2
				&& minF3 <= FValue[2] && FValue[2] <= maxF3 + d3) {
			return true;
		} else if (minF1 <= FValue[0] && FValue[0] <= maxF1 + d1 && minF2 <= FValue[1] && FValue[1] <= maxF2 + d2
				&& maxF3 <= FValue[2] && FValue[2] <= maxF3 + d3) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkApproxBetween(double[] FValue, int p1, int p2) {
		// suponiendo que p1<p2 --> sf1_F1 < sf2_F1
		WCPSolution sf1 = Pareto.getFrontAt(p1);
		WCPSolution sf2 = Pareto.getFrontAt(p2);
		return checkApproxBetween(FValue, sf1, sf2);
	}

	public static boolean checkApproxBetween(WCPSolution s, WCPSolution sf1, WCPSolution sf2) {
		if (s != null) {
			double[] FValue = { s.getTotalDist(), s.getDistanceLongestRoute(), s.getDifTime() };
			return checkApproxBetween(FValue, sf1, sf2);
		}
		return false;
	}

	public static boolean checkApproxBetween(WCPSolution sol, int p1, int p2) {
		if (sol == null || sol.equals(Pareto.getFrontAt(p1)) || sol.equals(Pareto.getFrontAt(p2))) {
			return false;
		}
		return checkApproxBetween(new double[] { sol.getTotalDist(), sol.getDistanceLongestRoute() }, p1, p2);
	}

	public static boolean checkApprox(double[] Fvalue) {
		boolean enter = false;
		for (int p = 0; p < front.size() - 1; p++) {
			if (checkApproxBetween(Fvalue, p, p + 1)) {
				return true;
			}
		}
		return enter;
	}

	private static boolean checkApprox(WCPSolution sol) {
		return (sol != null)
				&& (checkApprox(new double[] { sol.getTotalDist(), sol.getDistanceLongestRoute(), sol.getDifTime() }));
	}

	public static boolean addApprox(WCPSolution sol) {
		if (checkApprox(sol)) {
			approxToFront.add(sol);
			return true;
		} else
			return false;
	}

	/*
	 * private static void updateApproxSet() { List<WCPSolution> dominated = new
	 * ArrayList<>(); for (WCPSolution sol : approxToFront) { if (!checkApprox(sol))
	 * { dominated.add(sol); } } approxToFront.removeAll(dominated); }
	 */
	public static WCPSolution getFrontAt(int p) {

		return front.get(p);
	}

	public static String toText() {
		StringBuilder stb = new StringBuilder();
		for (WCPSolution sol : front) {
			stb.append(sol.getTotalDist()).append(" ").append(sol.getDistanceLongestRoute()).append(" ")
					.append(sol.getDifTime()).append("\n");
		}
		for (WCPSolution sol : front) {
			stb.append(sol).append("\n");
		}
		return stb.toString();
	}

	public static void saveToFile(String path) {
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(toText());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String toTextApprox() {
		StringBuilder stb = new StringBuilder();
		for (WCPSolution sol : approxToFront) {
			stb.append(sol.getTotalDist()).append(" ").append(sol.getDistanceLongestRoute()).append(" ")
					.append(sol.getDifTime()).append("\n");
		}
		return stb.toString();
	}

	public static void saveToFileApprox(String path) {
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(toTextApprox());
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int size() {
		return front.size();
	}

	public static void clearApprox() {
		approxToFront.clear();
	}

	public static void clearApprox(Set<WCPSolution> analized) {
		approxToFront.removeAll(analized);
	}

	public static double[] findIdeal(List<WCPSolution> set) {
		double[] id = new double[4];
		for (int i = 0; i < 4; i++) {
			id[i] = Double.MAX_VALUE;
		}
		for (WCPSolution s : set) {
			if (s.getTotalDist() < id[0]) {
				id[0] = s.getTotalDist();
			}
			if (s.getDifTime() < id[2]) {
				id[2] = s.getDifTime();
			}
			if (s.getDistanceLongestRoute() < id[1]) {
				id[1] = s.getDistanceLongestRoute();
			}
			if (s.getNumRoutes() < id[3]) {
				id[3] = s.getNumRoutes();
			}
		}
		return id;
	}

	public static double[] findNadir(List<WCPSolution> set) {
		double[] id = new double[4];
		for (int i = 0; i < 4; i++) {
			id[i] = Double.MIN_VALUE;
		}
		for (WCPSolution s : set) {
			if (s.getTotalDist() > id[0]) {
				id[0] = s.getTotalDist();
			}
			if (s.getDifTime() > id[2]) {
				id[2] = s.getDifTime();
			}
			if (s.getDistanceLongestRoute() > id[1]) {
				id[1] = s.getDistanceLongestRoute();
			}
			if (s.getNumRoutes() > id[3]) {
				id[3] = s.getNumRoutes();
			}
		}
		return id;
	}

	public static void saveRoutes(String path) {
		if (path.lastIndexOf('/') > 0) {
			File folder = new File(path.substring(0, path.lastIndexOf('/')));
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}
		try {
			PrintWriter pw = new PrintWriter(path);
			for (WCPSolution sol : front) {
				pw.println(sol);
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveParetoFront(String path) {
		// TODO Auto-generated method stub
		
	}
}
