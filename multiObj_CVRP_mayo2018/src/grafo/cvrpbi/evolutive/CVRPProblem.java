package grafo.cvrpbi.evolutive;

import grafo.cvrpbi.structure.CVRPInstance;
import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

public class CVRPProblem extends AbstractIntegerPermutationProblem {

//  Permutation with attributes for the routes !!!

    protected final JavaRandomGenerator random = new JavaRandomGenerator();

    public CVRPProblem(CVRPInstance instance) {

        // Value 0 will correspond to N (node values belong to interval [1,N] )
        setNumberOfVariables(instance.getNodes()-1);
        setNumberOfObjectives(4);
        setName("MultiobjectiveGeneticCVRP");

        this.instance = instance;
    }

    protected CVRPInstance instance;

    @Override
    public int getPermutationLength() {
        return 0;
    }

    @Override
    public void evaluate(PermutationSolution<Integer> integerPermutationSolution) {

    }
}
