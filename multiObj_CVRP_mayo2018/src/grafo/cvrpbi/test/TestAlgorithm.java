package grafo.cvrpbi.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;

import grafo.cvrpbi.structure.CoverageMetric;

/**
 * Created by jesussanchezoro on 05/10/2017. Modificado 2.11.2017
 */
public class TestAlgorithm {

	public static void main(String[] args) throws FileNotFoundException {
		 Calendar cal = Calendar.getInstance();
	        int day = cal.get(Calendar.DAY_OF_MONTH);
	        int month = cal.get(Calendar.MONTH)+1;
	        int year = cal.get(Calendar.YEAR);

	        String date = String.format("%04d-%02d-%02d", year, month, day);
		 String instanceSet = (args.length == 0) ? "prueba" : args[0];
	        String dir ="./instancias/"+instanceSet;
	        		        
	        
	        String outDir = "./experiments/"+date;
	        File outDirCreator = new File(outDir);
	        outDirCreator.mkdirs();
	        String[] extensions = new String[]{".txt"};
	        String paretoDir = "./pareto";
		        String[] files = new File(paretoDir).list();
		        for(String s:files){
		        	CoverageMetric.createExcel(paretoDir+"/"+s, outDir+"/ContrasteIteratedGreedySept2018.csv");
		        }
		        	
	}
}
