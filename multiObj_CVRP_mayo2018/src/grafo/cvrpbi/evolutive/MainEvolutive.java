package grafo.cvrpbi.evolutive;

import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.structure.CVRPInstance;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.impl.crossover.PMXCrossover;
import org.uma.jmetal.operator.impl.mutation.PermutationSwapMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static grafo.cvrpbi.evolutive.UtilityMethods.*;

/**
 * Main class to run evolutive algorithms.
 *
 * @author J. M. Colmenar
 */
public class MainEvolutive {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("\nERROR: No properties file was provided.\n");
            System.out.println("--> java -jar MultiCVRP.jar nsgaii.properties\n");
            System.exit(0);
        }

        Properties props = loadProperties(args[0]);

        // Random Seed:
        int randomSeed = 1234;
        if (props.getProperty("RandomSeed") != null) {
            randomSeed = Integer.valueOf(props.getProperty("RandomSeed"));
        }

        RandomManager.setSeed(randomSeed);

        // Parameters:
        int population = Integer.valueOf(props.getProperty("Population"));
        int generations = Integer.valueOf(props.getProperty("Generations"));
        double cxProb = Double.valueOf(props.getProperty("CrossoverProb"));
        double mutProb = Double.valueOf(props.getProperty("MutationProb"));
        int runs = Integer.valueOf(props.getProperty("Runs"));

        String dir = props.getProperty("InstancesDirectory");
        String outDir = props.getProperty("ResultsDirectory");

        int testedVehiclesIterations = 1;
        try {
            testedVehiclesIterations = Integer.parseInt(props.getProperty("TestedVehiclesIterations"));
        } catch (NullPointerException e) {
            System.out.println("\n --> No TestedVehiclesIterations property is defined. Value set to 1.\n");
        }


        List<String> instances = obtainFiles(dir);

        String propertiesAsString = executionDataAsString(props);
        System.out.println(propertiesAsString);

        // Genetic operators:
        PMXCrossover crossover = new PMXCrossover(cxProb);
        MutateRoutes mutation = new MutateRoutes(mutProb);
        BinaryTournamentSelection<PermutationSolution<Integer>> selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>()) ;
        DominanceComparator<PermutationSolution<Integer>> dominanceComparator = new DominanceComparator();
        SequentialSolutionListEvaluator<PermutationSolution<Integer>> evaluator = new SequentialSolutionListEvaluator();

        double alpha = 0.5;
        try {
            alpha = Double.valueOf(props.getProperty("Alpha"));
        } catch (NullPointerException e) {
            // If no alpha, 0.5 is used
            System.out.println("\n --> No Alpha property is defined. Value set to 0.5.\n");
        }

        for (String inst : instances) {
/*
            if (outDir != null) {
                try {
                    Files.write(Paths.get(outDir + File.separator + inst + ".txt"),
                            propertiesAsString.getBytes());
                } catch (Exception ex) {
                    System.err.println("Solutions file error: " + ex.getLocalizedMessage());
                }
            }
*/
            long start = System.currentTimeMillis();

            // Read instance
            System.out.println("\nReading instance: " + inst);
            WCPInstance instance = new WCPInstance(dir + File.separator + inst);

            // Use this element to control the number of vehicles:
//            WCPInstance.currentVehicles;


            // Create the problem
            CVRPProblem problem = new CVRPProblem(instance,alpha);

            // Solutions from all executions
            ArrayList<PermutationSolution<Integer>> bestSolutions = new ArrayList<>();

            double[] results = new double[runs];

            for (int j = 0; j < testedVehiclesIterations; j++) {

                if (j>0) WCPInstance.currentVehicles++;

                for (int i = 0; i < runs; i++) {

                    System.out.println("\n### Run " + (i + 1) + " - " + inst + " - "+ WCPInstance.currentVehicles +" vehicles - (" + ((System.currentTimeMillis() - start) / 1000) + " secs. running)");
                    // Second create the algorithm
                    NSGAII<PermutationSolution<Integer>> algorithm = new NSGAII<>(problem,generations,
                            population,crossover,mutation,selection,dominanceComparator,evaluator);

                    algorithm.run();

                    // Collect solutions
                    bestSolutions.addAll(algorithm.getResult());

                    // Print results of this run:
                    for (PermutationSolution<Integer> s : algorithm.getResult()) {
                        System.out.print(problem.convert(s));
                    }

                }

            }

            long end = (System.currentTimeMillis() - start) / 1000;

            System.out.println("\nExec time (secs) for instance "+instance.getName()+" : "+end+"\n");

            // Obtain non-dominated solutions from all executions:
            List<PermutationSolution<Integer>> ndSols = SolutionListUtils.getNondominatedSolutions(bestSolutions);

            if (outDir != null) {
//                writeStatsToFile(bestSolutions, instance.getName(), outDir);
//                writeStatsToFile(results, instance.getName(), outDir);
                writeFrontToFile(ndSols, inst, outDir);
            }

        }

    }


}
