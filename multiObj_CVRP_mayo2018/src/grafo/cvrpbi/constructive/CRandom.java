package grafo.cvrpbi.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class CRandom implements Constructive<WCPInstance, WCPSolution> {
    
    @Override
    public WCPSolution constructSolution(WCPInstance instance) {
        Random rnd = RandomManager.getRandom();
        WCPSolution sol = new WCPSolution(instance);
        int n = instance.getNodes();
        List<Integer> cl = new ArrayList<>(n);
        for (int v = 1; v < n; v++) {
            cl.add(v);
        }
        Collections.shuffle(cl, rnd);
        int nRoutes = instance.getVehicles();
        List<Integer> routes = new ArrayList<>(nRoutes);
        for (int i = 0; i < nRoutes; i++) {
            routes.add(i);
        }
        Collections.shuffle(routes, rnd);
        boolean incomplete = true;
        int iter = 0;
        while (incomplete && iter<100){
        	incomplete = false;
        	int c = 1;//depot
        	for (int v : cl) {
        		for (int r : routes) {
        			if (sol.isFeasibleAdd(v, r)) {
        				sol.addNode(v, r);
        				c++;
        				break;
        				}
        			}
        		}
        	if (c<instance.getNodes()){
        		sol = new WCPSolution(instance);
        		Collections.shuffle(cl, rnd);
        		 Collections.shuffle(routes, rnd);
        		 incomplete = true;
        	}
        	}
        sol.findLongestRoute();
        sol.findTimes();
        return sol;
    }
}
