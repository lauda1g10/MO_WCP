package grafo.cvrpbi.evolutive;

import grafo.optilib.tools.RandomManager;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.PermutationSolution;

import java.util.ArrayList;
import java.util.Random;

/**
 * Requires a PermutationSolution but acts on our second part of information: routes.
 *
 * @author J. M. Colmenar
 */
public class MutateRoutes implements MutationOperator<PermutationSolution<Integer>> {

    protected double mutationProbability;
    protected Random rnd = RandomManager.getRandom();

    public MutateRoutes(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    @Override
    public PermutationSolution<Integer> execute(PermutationSolution<Integer> solution) {

        // Probability shoud be applied to each variable individually. This is not the case.
        if (rnd.nextDouble() <= mutationProbability) {
            /*
              Takes two routes by random: one is reduced and the other is augmented.
              - The one to be reduced has to be larger than 1.
              - Capacity is not taken into account.
             */

            ArrayList<Integer> routes = (ArrayList<Integer>) solution.getAttribute(CVRPProblem.ROUTES);

            int idR;
            int idA;
            do {
                idR = rnd.nextInt(routes.size());
            } while (routes.get(idR) <= 1);

            do {
                idA = rnd.nextInt(routes.size());
            } while (idA == idR);

            // From one route to the other one:
            routes.set(idA, routes.get(idA) + 1);
            routes.set(idR, routes.get(idR) - 1);
        }

        return solution;
    }

}
