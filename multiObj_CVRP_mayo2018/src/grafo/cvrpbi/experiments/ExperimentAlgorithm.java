package grafo.cvrpbi.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import grafo.cvrpbi.algorithms.IteratedGreedy_Multi_WCK;
import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.constructive.C1_LR;
import grafo.cvrpbi.constructive.C1_TIME;
import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_GreedyRandom;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomGreedy;
import grafo.cvrpbi.constructive.IteratedGreedy_RandomRandom;
import grafo.cvrpbi.structure.CoverageMetric;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPInstanceFactory;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.results.Experiment;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class ExperimentAlgorithm {

	public static int indexAlg = 0;
	public static int getAlgIndex() {
		return indexAlg;		
	}
	public static void sumAlgIndex(){
		indexAlg++;
	}
    public static void main(String[] args) throws FileNotFoundException {
 
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);

        String date = String.format("%04d-%02d-%02d", year, month, day);

        WCPInstanceFactory factory = new WCPInstanceFactory();
        int construcciones = 100;
        String instanceSet = (args.length == 0) ? "preliminar" : args[0];
        String dir ="./instancias/"+instanceSet;
        		        
        
        String outDir = "./experiments/"+date;
        File outDirCreator = new File(outDir);
        outDirCreator.mkdirs();
        String[] extensions = new String[]{".txt"};
        //constructores
        C1 c1 = new C1(-1);
        C1_LR c2 = new C1_LR(-1);
        C1_TIME c3 = new C1_TIME(-1);
       // C1_Combi cCombi = new C1_Combi(-1);
        //C1_CombiRandom cCombiR = new C1_CombiRandom(-1);
        C1_WCK cW = new C1_WCK(-1);

		double beta = 0.1;
    	int maxIter = 5;
		int lambdaIntervals = 20;
		double[] alpha = new double[]{0.1,0.25,0.5};
		 
       List<Algorithm<WCPInstance>> l = new ArrayList<>();
       for(double a: alpha){
    	   IteratedGreedy_RandomGreedy igRG = new IteratedGreedy_RandomGreedy(c1,a,beta,maxIter);
   		IteratedGreedy_GreedyGreedy igGG = new IteratedGreedy_GreedyGreedy(c1,a,beta,maxIter);
   		IteratedGreedy_GreedyRandom igGR = new IteratedGreedy_GreedyRandom(c1,a,beta,maxIter);
   		IteratedGreedy_RandomRandom igRR = new IteratedGreedy_RandomRandom(c1,a,beta,maxIter);
   		
   		
          l.add(new IteratedGreedy_Multi_WCK(c1,c2,c3,cW,lambdaIntervals,construcciones,igRG));
          l.add(new IteratedGreedy_Multi_WCK(c1,c2,c3, cW,lambdaIntervals,construcciones,igGR));
          l.add(new IteratedGreedy_Multi_WCK(c1,c2,c3, cW,lambdaIntervals,construcciones,igRR));
          l.add(new IteratedGreedy_Multi_WCK(c1,c2,c3, cW,lambdaIntervals,construcciones,igGG));
    	   
       }
        
       /* Algorithm<CVRPInstance>[] execution = new Algorithm[] {
        		a1,
        		a2,
        		a3,
        		a4,
        		alg1_pathRelinking,
        		alg11_pathRelinking,
        		alg2_pathRelinking,
        		alg21_pathRelinking,
        		alg3_pathRelinking,
        		alg31_pathRelinking,
        		alg4_pathRelinking,
        		alg41_pathRelinking
        };*/
       Algorithm<WCPInstance>[] execution = l.toArray(new Algorithm[1]);
       
        for (int i=0;i<execution.length;i++) {
        	String outputFile = outDir+"/"+instanceSet+"_"+execution[i].toString()+".xlsx";
            Experiment<WCPInstance, WCPInstanceFactory> experiment = new Experiment<WCPInstance, WCPInstanceFactory>(execution[i], factory);
            experiment.launch(dir, outputFile, extensions);
        }
       String paretoDir = "./pareto";
        String[] files = new File(paretoDir).list();
        for(String s:files){
        	CoverageMetric.createExcel(paretoDir+"/"+s, outDir+"/ContrasteIteratedGreedy.csv");
        }
        
    }
}
