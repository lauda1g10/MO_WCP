package grafo.cvrpbi.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoverageMetric {

    public static double getMetric(List<WCPSolution> A, List<WCPSolution> B){
        int dom = 0;
            for (WCPSolution sB: B){
            	 for (WCPSolution sA: A){
            		 if (sA.dominates(sB)){
            			 dom = dom+1;
            			 break;
            			 }
            		 }
            	 }
        return ((double)dom)/B.size();
    }

    public static double getMetricValues(List<double[]> A, List<double[]> B){
    	 List<double[]> dominated = new ArrayList<>();
         //devuelve el % de elementos de B que están dominados por algún elemento de A.
             for (double[] sB: B){
             	for (double[] sA: A){
             	boolean dom = true;
                 for (int p =0; p<sB.length;p++){
                     if (sB[p]-sA[p]<CVRPInstance.EPSILON)//resulta que sB[p] es más pequeño que sA[p] y, por tanto A no domina a B (en un problema de minimización)
                     {
                     	dom = false;
                     	break;
                     }
                 }
                 if (dom){
                 	dominated.add(sB);
                 	break;
                 }
             }
         }
         return ((double)(dominated.size()))/B.size();
    }

    public static void createExcel(String paretoDir, String outputFile) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(outputFile,true));
            String[] files = new File(paretoDir).list();//(dir, name) -> name.endsWith(".txt"));
            //paretoDir.substring(paretoDir.lastIndexOf('/'));
            pw.print(paretoDir.substring(paretoDir.lastIndexOf('/'))+";");
            Arrays.sort(files,String.CASE_INSENSITIVE_ORDER);
            
            for (int i = 0; i < files.length; i++) {
                String pf1 = files[i];
                List<double[]> pf1OF = readParetoFront(paretoDir + "/" + pf1);
                for (int j = 0; j < files.length; j++) {
                    String pf2 = files[j];
                    if (pf1.equals(pf2)) continue;
                    List<double[]> pf2OF = readParetoFront(paretoDir + "/" + pf2);
                    double coverage = getMetricValues(pf1OF, pf2OF);
                    pw.print(pf1+";"+pf2+";"+pf1OF.size()+";"+pf2OF.size()+";"+coverage+";");
                }
            }
            pw.println();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<double[]> readParetoFront(String file) {
        try {
            List<double[]> front = new ArrayList<>();
            BufferedReader bf = new BufferedReader(new FileReader(file));
            String line = bf.readLine();
            while (line != null && line != "") {
                String[] tokens = line.split("\\s+");
                double[] elems = new double[tokens.length];
                if (elems.length<2){break;}
                for (int i = 0; i < tokens.length; i++) {
                    elems[i] = Double.parseDouble(tokens[i]);
                }
                front.add(elems);
                line = bf.readLine();
            }
            bf.close();
            return front;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
    	createExcel("./pareto/"+CVRPInstance.name, "./instancesResults.csv");
        //createExcel("/Users/jesussanchezoro/Downloads/paretos", "./salida.csv");
    }
}
