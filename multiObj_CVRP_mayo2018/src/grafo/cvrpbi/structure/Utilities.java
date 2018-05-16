package grafo.cvrpbi.structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utilities {
	public static double[] refBestPoint;
	public static double[] refWorstPoint;

	public static void setRef(double[] refP) {
		refBestPoint = refP;
	}

	public static double[] getrefPoint() {
		return refBestPoint;
	}

	public static void setRefWorst(double[] refP) {
		refWorstPoint = refP;
	}

	public static double[] getrefWorstPoint() {
		return refWorstPoint;
	}

	public static double distanceToRefL2(WCPSolution s1) {
		return distanceToRefL2(s1.getTotalDist(), s1.getDistanceLongestRoute(), s1.getDifTime(), s1.getNumRoutes());
	}

	public static double distanceToRef(WCPSolution s1) {
		return distanceToRef(s1.getTotalDist(), s1.getDistanceLongestRoute(), s1.getDifTime(), s1.getNumRoutes());
	}

	public static double distanceToRef(double f1, double f2, double f3, int f4) {
		// en este cálculo no va a influir el número de vehículos.
		return Math.max(
				Math.max((f1 - refBestPoint[0]) / (refWorstPoint[0] - refBestPoint[0]),
						(f2 - refBestPoint[1]) / (refWorstPoint[1] - refBestPoint[1])),
				(f3 - refBestPoint[2]) / (refWorstPoint[2] - refBestPoint[2]));
	}

	public static double distanceToRefL2(double f1, double f2, double f3, int f4) {
		return Math.sqrt(Math.pow((f1 - refBestPoint[0]) / (refWorstPoint[0] - refBestPoint[0]), 2)
				+ Math.pow((f2 - refBestPoint[1]) / (refWorstPoint[1] - refBestPoint[1]), 2)
				+ Math.pow((f3 - refBestPoint[2]) / (refWorstPoint[2] - refBestPoint[2]), 2)
				+ Math.pow(((double) f4 - refBestPoint[3]) / (refWorstPoint[3] - refBestPoint[3]), 2));
	}

	public static List<double[]> readParetoSetFrom(String path) throws IOException {
		List<double[]> salida = new ArrayList<>();
		BufferedReader bfInstance = new BufferedReader(new FileReader(path));
		// Archivo
		String line = bfInstance.readLine().trim();
		while (!line.isEmpty()) {
			String[] values = line.split(" ");
			double[] Fvalue = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				Fvalue[i] = Double.parseDouble(values[i]);
			}
			salida.add(Fvalue);
			line = bfInstance.readLine().trim();
		}
		bfInstance.close();
		return salida;
	}
}
