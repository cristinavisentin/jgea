package io.github.ericmedvet.jgea.core.solver.bi;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.POCPopulationState;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractBiEvolver<
    T extends POCPopulationState<I, G, S, Q, P>,
    P extends QualityBasedBiProblem<S, O, Q>,
    I extends Individual<G, S, Q>,
    G,
    S,
    Q, O> extends AbstractPopulationBasedIterativeSolver<T, P, I, G, S, Q> {

  protected final BinaryOperator<Q> fitnessReducer;

  public AbstractBiEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super T> stopCondition,
      boolean remap, BinaryOperator<Q> fitnessReducer) {
    super(solutionMapper, genotypeFactory, stopCondition, remap);
    this.fitnessReducer = fitnessReducer;
  }

  /*
  public static <
      T extends POCPopulationState<I, G, S, Q, P>,
      P extends QualityBasedBiProblem<S, O, Q>,
      I extends Individual<G, S, Q>,
      G,
      S,
      Q,
      O>
  Collection<Pair<Future<I>, Future<I>> biMap(
      Collection<Pair<Individual<G, S, Q>, Individual<G, S, Q>>> opponentsCollections,
      TriFunction<ChildGenotype<G>, ChildGenotype<G>, T, I> mapper,
      T state,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    try {
      return executor.invokeAll(IntStream.range(0, opponentsCollections.first().size())
          .mapToObj(i -> (Callable<I>) () -> mapper.apply(
              opponentsCollections.first().stream().toList().get(i),
              opponentsCollections.second().stream().toList().get(i),
              state)
          )
          .toList());
    } catch (InterruptedException e) {
      throw new SolverException(e);
    }
  }
  */
}
