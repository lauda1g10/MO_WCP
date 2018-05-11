package grafo.cvrpbi.evolutive;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.PermutationSolution;

/**
 * Requires a PermutationSolution but acts on our second part of information: routes.
 *
 * @author J. M. Colmenar
 */
public class MutateRoutes implements MutationOperator<PermutationSolution<Integer>> {

    @Override
    public PermutationSolution<Integer> execute(PermutationSolution<Integer> solution) {

        // TODO: need to perform mutation !!

        return solution;
    }

}
