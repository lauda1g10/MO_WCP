package grafo.cvrpbi.improvements.multi;

import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;

public class MultiLS_ref {

	private static double[] bestPoint;
	private static double[] worstPoint;
	private static Improvement<WCPSolution> [] ls;
	
	public static double[] getBestPoint(){
		return bestPoint;
		}
	public MultiLS_ref(Improvement<WCPSolution> [] bl){
		ls = bl; // Recordar que deben tener en cuenta el punto de referencia
	}
	public static Improvement<WCPSolution> [] getLS(){
		return ls;
	}
	public static double[] getWorstPoint(){
		return worstPoint;
	}
	public static void setBestPoint(double[] f){
		bestPoint = f;
	}
	public static void setWorstPoint(double[] f){
		worstPoint = f;
	}
	public static double distanceL2ToBest(double f1, double f2, double f3, int f4) {
		return Math.sqrt(Math.pow((f1 - bestPoint[0]), 2)
				+ Math.pow((f2 - bestPoint[1]), 2)
				+ Math.pow((f3 - bestPoint[2]), 2)
				+ Math.pow(((double) f4 - bestPoint[3]), 2));
	}
	@Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("MultiLS_ref").append("(");
        for (Improvement<WCPSolution> search : ls) {
            stb.append(search).append(",");
        }
        stb.append(")");
        return stb.toString();
    }
}
