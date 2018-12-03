package grafo.cvrpbi.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import grafo.cvrpbi.algorithms.IteratedGreedy_Multi_WCK;
import grafo.cvrpbi.algorithms.VNS_refOpt2;
import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.constructive.C1_LR;
import grafo.cvrpbi.constructive.C1_TIME;
import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyRandom;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomRandom;
import grafo.cvrpbi.improvements.BLSIntra2Opt;
import grafo.cvrpbi.improvements.BLS_1to0;
import grafo.cvrpbi.improvements.BLS_1to1;
import grafo.cvrpbi.improvements.FLSIntra2Opt;
import grafo.cvrpbi.improvements.FLS_1to0;
import grafo.cvrpbi.improvements.FLS_1to1;
import grafo.cvrpbi.improvements.VNS_2opt;
import grafo.cvrpbi.improvements.VNS_interchange;
import grafo.cvrpbi.improvements.VNS_move1_0;
import grafo.cvrpbi.improvements.multi.BMLSIntra2OptrefL2;
import grafo.cvrpbi.improvements.multi.BMLS_1to0refL2;
import grafo.cvrpbi.improvements.multi.BMLS_1to1refL2;
import grafo.cvrpbi.improvements.multi.MultiLS_ref;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPInstanceFactory;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.tools.RandomManager;


public class TestAlgorithmVNS_ref {

	public static void main(String[] args) throws FileNotFoundException {
	 
		RandomManager.setSeed(1234);
		
	        Calendar cal = Calendar.getInstance();
	        int day = cal.get(Calendar.DAY_OF_MONTH);
	        int month = cal.get(Calendar.MONTH)+1;
	        int year = cal.get(Calendar.YEAR);

	        String date = String.format("%04d-%02d-%02d", year, month, day);

	        WCPInstanceFactory factory = new WCPInstanceFactory();
	        int construcciones = 50;
			int maxIter = 5;
			int lambdaIntervals = 10;
	        String instanceSet = (args.length == 0) ? "prueba" : args[0];
	        String dir ="./instancias/"+instanceSet;
	        		        
	        
	        String outDir = "./experiments/"+date;
	        File outDirCreator = new File(outDir);
	        outDirCreator.mkdirs();
	        String[] extensions = new String[]{".txt"};
	      

	     // constructores
	     		C1 c1 = new C1(-1);
	     		C1_LR c2 = new C1_LR(-1);
	     		C1_TIME c3 = new C1_TIME(-1);
	     		C1_WCK cW = new C1_WCK(-1);
	     		//CRandom c = new CRandom();

	     	
	     		double beta = 0.05;
	     //búsquedas locales single objective.
	     		Improvement<WCPSolution>[] bls = new Improvement[3];
	     		bls[0] = new BLS_1to1();
	     		bls[1] = new BLSIntra2Opt();
	     		bls[2] = new BLS_1to0();
	     		Improvement<WCPSolution>[] fls = new Improvement[3];
	     		fls[0] = new FLS_1to1();
	     		fls[1] = new FLSIntra2Opt();
	     		fls[2] = new FLS_1to0();
	     		List<Improvement<WCPSolution>> vns = new ArrayList<>();
	     		String path = "./instancias/X-n837-k142vrp.txt";
	     		WCPInstance instance = new WCPInstance(path);
	     		
	     	Improvement<WCPSolution> vns2opt = new VNS_2opt(bls,0.03);
	     	Improvement<WCPSolution> vns_interchange = new VNS_interchange(bls,0.03);
	     	Improvement<WCPSolution> vns1to0 = new VNS_move1_0(bls,0.03);
	     	Improvement<WCPSolution> vns2optR = new VNS_2opt(fls,0.03);
	     	Improvement<WCPSolution> vns_interchangeR = new VNS_interchange(fls,0.03);
	     	Improvement<WCPSolution> vns1to0R = new VNS_move1_0(fls,0.03);
	     
	     	Improvement<WCPSolution> v = vns2opt;
	     	
	     	//busquedas locales multiobjc
	     	Improvement<WCPSolution>[] mls = new Improvement[3];
	     	mls[0] = new BMLS_1to0refL2();
	     	mls[1] = new BMLS_1to1refL2();
	     	mls[2] = new BMLSIntra2OptrefL2();
	     	MultiLS_ref multiLS = new MultiLS_ref(mls);
	     	
	     	double a = 0.5;//,0.25,0.5;
	     		//algoritmos		
	     	    	IteratedGreedy_RandomGreedy igRG = new IteratedGreedy_RandomGreedy(c1,a,beta,maxIter);
	     	   		IteratedGreedy_GreedyGreedy igGG = new IteratedGreedy_GreedyGreedy(c1,a,beta,maxIter);
	     	   		IteratedGreedy_GreedyRandom igGR = new IteratedGreedy_GreedyRandom(c1,a,beta,maxIter);
	     	   		IteratedGreedy_RandomRandom igRR = new IteratedGreedy_RandomRandom(c1,a,beta,maxIter);
	     	   	VNS_refOpt2 a1 = new VNS_refOpt2(new IteratedGreedy_Multi_WCK(c1,c2,c3,cW,lambdaIntervals,construcciones,igRG,v));
	     	   VNS_refOpt2 a2 =  new VNS_refOpt2(new IteratedGreedy_Multi_WCK(c1,c2,c3,cW,lambdaIntervals,construcciones,igRR,v));
	     	  VNS_refOpt2 a3 =  new VNS_refOpt2(new IteratedGreedy_Multi_WCK(c1,c2,c3,cW,lambdaIntervals,construcciones,igGG,v));
	     	 VNS_refOpt2 a4 =  new VNS_refOpt2(new IteratedGreedy_Multi_WCK(c1,c2,c3,cW,lambdaIntervals,construcciones,igGR,v));
	
	     	a1.execute(instance); 
	     	a2.execute(instance); 
	     	a3.execute(instance); 
	     	a4.execute(instance);     	   		
	}
	     	       
}