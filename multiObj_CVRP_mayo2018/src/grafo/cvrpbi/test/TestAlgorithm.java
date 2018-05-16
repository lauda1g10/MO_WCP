package grafo.cvrpbi.test;

import java.io.FileNotFoundException;

import grafo.cvrpbi.algorithms.IteratedGreedy_Multi_WCK;
import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.constructive.C1_LR;
import grafo.cvrpbi.constructive.C1_TIME;
import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyRandom;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomRandom;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.optilib.tools.RandomManager;

/**
 * Created by jesussanchezoro on 05/10/2017. Modificado 2.11.2017
 */
public class TestAlgorithm {

	public static void main(String[] args) throws FileNotFoundException {

		RandomManager.setSeed(1234);
		int construcciones = 500;
		int maxIter = 5;
		int lambdaIntervals = 20;

		String path = "./instancias/preliminar/X-n219-k73vrp.txt";
		WCPInstance instance = new WCPInstance(path);
		//System.out.println(instance);

		// constructores
		C1 c1 = new C1(-1);
		C1_LR c2 = new C1_LR(-1);
		C1_TIME c3 = new C1_TIME(-1);
		C1_WCK cW = new C1_WCK(-1);
		//CRandom c = new CRandom();

		double a = 0.1;
		double beta = 0.05;

		IteratedGreedy_RandomGreedy igRG = new IteratedGreedy_RandomGreedy(c1,a,beta,maxIter);
   		IteratedGreedy_GreedyGreedy igGG = new IteratedGreedy_GreedyGreedy(c1,a,beta,maxIter);
   		IteratedGreedy_GreedyRandom igGR = new IteratedGreedy_GreedyRandom(c1,a,beta,maxIter);
   		IteratedGreedy_RandomRandom igRR = new IteratedGreedy_RandomRandom(c1,a,beta,maxIter);
		IteratedGreedy_Multi_WCK a1 = new IteratedGreedy_Multi_WCK(c1, c2, c3, cW, lambdaIntervals, construcciones,
				igRG);
		IteratedGreedy_Multi_WCK a2 = new IteratedGreedy_Multi_WCK(c1, c2, c3, cW, lambdaIntervals, construcciones,
				igGG);
		IteratedGreedy_Multi_WCK a3 = new IteratedGreedy_Multi_WCK(c1, c2, c3, cW, lambdaIntervals, construcciones,
				igGR);
		IteratedGreedy_Multi_WCK a4 = new IteratedGreedy_Multi_WCK(c1, c2, c3, cW, lambdaIntervals, construcciones,
				igRR);
		
		for(int iter = 0; iter <construcciones;iter++){		
		c3.constructSolution(instance);
		}
		//a1.execute(instance);
		//a2.execute(instance);
		//a3.execute(instance);
		//a4.execute(instance);
/*
		String paretoDir = path + "./pareto";
		String[] files = new File(paretoDir).list();
		for (String s : files) {
			CoverageMetric.createExcel(paretoDir + "/" + s, paretoDir + "/ContrasteFronterasAbril2018.csv");
		}

		for(int i=0;i<construcciones;i++){
			System.out.println(c.constructSolution(instance));
		}*/
	}
}
