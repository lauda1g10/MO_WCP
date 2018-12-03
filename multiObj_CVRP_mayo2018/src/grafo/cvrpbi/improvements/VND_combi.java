package grafo.cvrpbi.improvements;

import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance_RealInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;

/**
 * Created by Laura on 11/04/2018.
 */
public class VND_combi extends VND {

	private Improvement<WCPSolution>[] ls;

	public VND_combi(Improvement<WCPSolution>[] ls) {
		super(ls);
	}

	@Override
	public void improve(WCPSolution solution) {
		int k = 0;
		Random i = RandomManager.getRandom();
		double before;
		while (k < ls.length) {
			int p = i.nextInt(100);
			if (p % 3 == 0) {
				WCPSolution.currentOF = WCPSolution.ObjFunct.TOTAL_DIST;
			} else if (p % 3 == 1){
				WCPSolution.currentOF = WCPSolution.ObjFunct.LONGEST_ROUTE;
			}else if (p % 3 == 2){
				WCPSolution.currentOF = WCPSolution.ObjFunct.TIME;
			}
			before = solution.getOF();
			ls[k].improve(solution);
			if (solution.getOF() - before < -WCPInstance_RealInstance.EPSILON) {
				k = 0;
			}else{
					k++;
				}
		}
	}
}
