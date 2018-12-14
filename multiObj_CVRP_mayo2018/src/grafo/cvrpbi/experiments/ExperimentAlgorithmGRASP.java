
package grafo.cvrpbi.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import grafo.cvrpbi.algorithms.AlgConstWCK_Multi_LS;
import grafo.cvrpbi.algorithms.PathRelinking_length;
import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.constructive.C1_LR;
import grafo.cvrpbi.constructive.C1_TIME;
import grafo.cvrpbi.constructive.C1_WCK;
import grafo.cvrpbi.improvements.BLSIntra2Opt;
import grafo.cvrpbi.improvements.BLS_1to0;
import grafo.cvrpbi.improvements.BLS_1to1;
import grafo.cvrpbi.improvements.FLSIntra2Opt;
import grafo.cvrpbi.improvements.FLS_1to0;
import grafo.cvrpbi.improvements.FLS_1to1;
import grafo.cvrpbi.improvements.VNS_2opt;
import grafo.cvrpbi.improvements.VNS_interchange;
import grafo.cvrpbi.improvements.VNS_move1_0;
import grafo.cvrpbi.structure.CoverageMetric;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPInstanceFactory;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Experiment;

/**
 * Created by jesussanchezoro on 05/10/2017.
 */
public class ExperimentAlgorithmGRASP {

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
		int maxIter = 10;
		int lambdaIntervals = 20;
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
     //búsquedas locales.
     		Improvement<WCPSolution>[] bls = new Improvement[3];
     		bls[0] = new BLS_1to1();
     		bls[1] = new BLSIntra2Opt();
     		bls[2] = new BLS_1to0();
     		Improvement<WCPSolution>[] fls = new Improvement[3];
     		fls[0] = new FLS_1to1();
     		fls[1] = new FLSIntra2Opt();
     		fls[2] = new FLS_1to0();
     		List<Improvement<WCPSolution>> vns = new ArrayList<>();
     		
     	Improvement<WCPSolution> vns2opt = new VNS_2opt(bls,0.03);
     	Improvement<WCPSolution> vns_interchange = new VNS_interchange(bls,0.03);
     	Improvement<WCPSolution> vns1to0 = new VNS_move1_0(bls,0.03);
     	Improvement<WCPSolution> vns2optR = new VNS_2opt(fls,0.03);
     	Improvement<WCPSolution> vns_interchangeR = new VNS_interchange(fls,0.03);
     	Improvement<WCPSolution> vns1to0R = new VNS_move1_0(fls,0.03);
     
     	
     	
     	vns.add(vns2opt);
     	vns.add(vns_interchange);
     	vns.add(vns1to0);
     /*	vns.add(vns2optR);
     	vns.add(vns_interchangeR);
     	vns.add(vns1to0R);*/
     		//algoritmos		
     	/*
     		 List<Algorithm<WCPInstance>> l = new ArrayList<>();
     	   		for(Improvement<WCPSolution> v:vns){
     	          l.add(new AlgConstWCK_Multi_LS(cW,c1,c2,c3,v,maxIter,lambdaIntervals));
     	   		}
     	   	List<Algorithm<WCPInstance>> alg = new ArrayList<>();
     	   	for(Algorithm<WCPInstance> a:l){
     	   		alg.add(new PathRelinking_length(a));
     	   	}
     	       Algorithm<WCPInstance>[] execution = alg.toArray(new Algorithm[1]);
     	       WCPInstance.indexSolution = 0;
     	        for (int i=0;i<execution.length;i++) {
     	        	String outputFile = outDir+"/"+instanceSet+"_"+execution[i].toString()+".xlsx";
     	            Experiment<WCPInstance, WCPInstanceFactory> experiment = new Experiment<WCPInstance, WCPInstanceFactory>(execution[i], factory);
     	            experiment.launch(dir, outputFile, extensions);
     	            WCPInstance.incrementIndex();
     	        }*/
     	       String paretoDir = "./pareto";
     	        String[] files = new File(paretoDir).list();
     	        for(String s:files){
     	        	CoverageMetric.createExcel(paretoDir+"/"+s, outDir+"/ContrasteGRASPpr.csv");
     	        }   
    }
}
