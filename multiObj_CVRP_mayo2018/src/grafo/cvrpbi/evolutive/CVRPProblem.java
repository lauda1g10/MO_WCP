package grafo.cvrpbi.evolutive;

import grafo.cvrpbi.constructive.C1;
import grafo.cvrpbi.constructive.C1_LR;
import grafo.cvrpbi.constructive.C1_TIME;
import grafo.cvrpbi.structure.WCPInstance;
import grafo.cvrpbi.structure.WCPRoute;
import grafo.cvrpbi.structure.WCPSolution;
import grafo.optilib.metaheuristics.Constructive;
import grafo.optilib.tools.RandomManager;
import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerPermutationSolution;
import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

import java.util.ArrayList;

public class CVRPProblem<T extends Constructive<WCPInstance, WCPSolution>> extends AbstractIntegerPermutationProblem {

    public WCPSolution testSol;

    protected final JavaRandomGenerator random = new JavaRandomGenerator();
    public static final String ROUTES = "Routes";

    protected WCPInstance instance;
    protected Constructive<WCPInstance, WCPSolution> constructiveObj0;
    protected Constructive<WCPInstance, WCPSolution> constructiveObj1;
    protected Constructive<WCPInstance, WCPSolution> constructiveObj2;

    public CVRPProblem(WCPInstance instance,double alpha) {

        // An instance with N nodes indicates that N-1 are the customers.
        // Therefore, we will deal with elements from 1 to N-1.
        // Value 0 will correspond to N-1 (node values belong to interval [1,N-1] )
        setNumberOfVariables(instance.getNodes()-1);
        setNumberOfObjectives(4);
        setName("MultiobjectiveGeneticCVRP");

        this.instance = instance;

        // Create the array of constructive methods:
        this.constructiveObj0 = new C1(alpha);
        this.constructiveObj1 = new C1_LR(alpha);
        this.constructiveObj2 = new C1_TIME(alpha);

    }


    @Override
    public PermutationSolution<Integer> createSolution() {

        Constructive<WCPInstance, WCPSolution> constructive;
        double rnd = random.nextDouble();

        if (rnd >= 0.666) {
            constructive = constructiveObj2;
        } else {
            if (rnd >= 0.333) {
                constructive = constructiveObj1;
            } else {
                constructive = constructiveObj0;
            }
        }

        // Create a solution with the constructive algorithm and translate it to a jMetal solution.
        WCPSolution sol = constructive.constructSolution(instance);
        testSol = sol;

        PermutationSolution<Integer> jmSol = convert(sol);

        return jmSol;
    }

    @Override
    public int getPermutationLength() {
        return instance.getNodes()-1;
    }

    @Override
    public void evaluate(PermutationSolution<Integer> integerPermutationSolution) {
        // Convert to WCPSolution and evaluate there:
        WCPSolution sol = convert(integerPermutationSolution);

        // Set objective values:
        integerPermutationSolution.setObjective(0,sol.getTotalDist());
        integerPermutationSolution.setObjective(1,sol.getDistanceLongestRoute());
        integerPermutationSolution.setObjective(2,sol.getDifTime());
        integerPermutationSolution.setObjective(3,sol.getNumRoutes());
    }


    /**
     * Converts from WCPSolution to jMetal Solution.
     * @param sol
     */
    public PermutationSolution<Integer> convert(WCPSolution sol) {
        DefaultIntegerPermutationSolution jmSol = new DefaultIntegerPermutationSolution(this);
        /*
           Don't need the permutation here.
           This default integer permutation generates a permutation with numbers in [0,N-1). We need a permutation
           in the range of [1,N-1]. Then, we will always consider the 0 as
           instance.getNodes()-1.

            This consideration will be taken in the evaluation of the solutions.
        */
        int idxPermutation = 0;
        ArrayList<Integer> routes = new ArrayList<>(sol.getNumRoutes());

        for (int i = 0; i < sol.getNumRoutes(); i++) {
            WCPRoute route = sol.getRoute(i);
            // Each route begins and ends with the depot (0). We avoid it with the subroute.
            for (int c : route.getSubRoute(1,route.size()-2)) {
                // Incorporate customers to the permutation (except N-1, which is 0)
                jmSol.setVariableValue(idxPermutation,c == (instance.getNodes()-1) ? 0 : c);
                idxPermutation++;
            }
            // Add vehicle to solution (remove extreme values corresponding to depot).
            routes.add(route.size()-2);
        }

        // Add routes to solution
        jmSol.setAttribute(ROUTES,routes);

        return jmSol;
    }


    /**
     * Converts from jMetal Solution to WCPSolution
     * @param jmSol
     */
    public WCPSolution convert(PermutationSolution<Integer> jmSol) {
        WCPSolution sol = new WCPSolution(instance);

        // Recover routes:
        ArrayList<Integer> routes = (ArrayList<Integer>)jmSol.getAttribute(ROUTES);

        int idxPermutation = 0;
        int idxRoute = 0;

        // Iterates along the routes.
        for (Integer r : routes) {
            int routeSize = r;
            while (routeSize > 0) {
                int v = jmSol.getVariableValue(idxPermutation);
                sol.addNode(v == 0 ? instance.getNodes()-1 : v,idxRoute);
                routeSize--;
                idxPermutation++;
            }
            idxRoute++;
        }

        return sol;
    }


    public static void main(String[] args) {
        RandomManager.setSeed(1234);

        WCPInstance inst = new WCPInstance("instancias/CMT1_vrp.txt");

        CVRPProblem problem = new CVRPProblem(inst, 0.5);

        PermutationSolution jmSol = problem.createSolution();
        WCPSolution testSol2 = problem.convert(jmSol);

        System.out.println(problem.testSol);

        System.out.println(testSol2);
    }

}
