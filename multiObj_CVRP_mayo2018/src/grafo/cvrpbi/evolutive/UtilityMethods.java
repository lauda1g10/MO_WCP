package grafo.cvrpbi.evolutive;

import org.apache.commons.math3.stat.StatUtils;
import org.uma.jmetal.solution.PermutationSolution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


/**
 * Utility methods class.
 *
 * @author J. Manuel Colmenar
 */
public class UtilityMethods {

    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(fileName);
            props.load(in);
            in.close();
        } catch (IOException e) {
            System.err.println("Error in properties file: " + e.getMessage());
        }
        return props;
    }


    public static void writeStatsToFile(double[] results, String name, String directory) {
        double bestCost = StatUtils.min(results);
        double avgCost = StatUtils.mean(results);
        double stdDev = Math.sqrt(StatUtils.variance(results));

        String text = "\n@;;Best;"+bestCost+";;Avg.;"+avgCost+";;Std. Dev.;"+stdDev+"\n";

        try {
            Files.write(Paths.get(directory + File.separator + name + ".txt"),
                    text.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            System.err.println("Solutions file error: " + ex.getLocalizedMessage());
        }
    }


    public static List<String> obtainFiles(String dir) {
        File f = new File(dir);

        List<String> parsedList = new ArrayList<>();
        for (String s : Arrays.asList(f.list())) {
            if (!s.startsWith(".") && s.endsWith(".txt")) {
                parsedList.add(s);
            }
        }

        // Return only ONE file (for development)
        return parsedList.subList(0,1);

// TODO: return all the files of the directory !!
//        return parsedList;


    }


    /**
     * Returns data of the current execution as String, for the records.
     */
    public static String executionDataAsString(Properties props) {
        StringBuilder str = new StringBuilder();
        str.append("================================================================================");
        str.append("\nExecution started at: "+new Date());
        str.append("\n\nProperties of the execution:");
        for (Map.Entry s : props.entrySet()) {
            str.append("\n\t"+s);
        }
        str.append("\n================================================================================\n\n");

        return str.toString();
    }


    public static String solutionToString(PermutationSolution<Integer> s) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            str.append(Math.abs(s.getObjective(i))).append("\t");
        }
        str.append("1->");
        Integer customers = 1; //(Integer) s.getAttribute(CUSTOMERS);
        for (int i=0; i<customers; i++) {
            int v = s.getVariableValue(i);
            // "Repair" the values of the solution.
            if (v < 2) {
                // The number of variables is instance.getN()-1.
                v = s.getNumberOfVariables()+1-v;
            }
            str.append(v);
            if (i<(customers-1))
                str.append("->");
        }
        return str.toString();
    }
}
