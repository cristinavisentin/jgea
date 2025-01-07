package io.github.ericmedvet.jgea.core.solver.bi.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver;
import io.github.ericmedvet.jgea.core.solver.mapelites.Archive;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jgea.core.util.Misc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class MapElitesBiEvolver<G, S, Q, O> extends AbstractBiEvolver<
    MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>,
    QualityBasedBiProblem<S, O, Q>,
    MEIndividual<G, S, Q>,
    G, S, Q, O> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<MapElites.Descriptor<G, S, Q>> descriptors;

  public MapElitesBiEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>> stopCondition,
      int populationSize,
      Mutation<G> mutation,
      List<MapElites.Descriptor<G, S, Q>> descriptors,
      BinaryOperator<Q> fitnessReducer) {
    super(solutionMapper, genotypeFactory, stopCondition, false, fitnessReducer);
    this.populationSize = populationSize;
    this.mutation = mutation;
    this.descriptors = descriptors;
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> init(
      QualityBasedBiProblem<S, O, Q> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    AtomicLong counter = new AtomicLong(0);
    List<? extends G> genotypes = genotypeFactory.build(populationSize, random);
    List<? extends G> firstHalfGenotypes;
    int half = populationSize / 2;

    if (populationSize % 2 == 0) {
      firstHalfGenotypes = genotypes.subList(0, half);
    } else {
      firstHalfGenotypes = genotypes.subList(0, half + 1);
    }

    List<? extends G> secondHalfGenotypes = genotypes.subList(half, populationSize);

    List<CompletableFuture<List<MEIndividual<G, S, Q>>>> futures = new ArrayList<>();

    for (int i = 0; i < firstHalfGenotypes.size(); i++) {
      int index = i;
      CompletableFuture<List<MEIndividual<G, S, Q>>> future = CompletableFuture.supplyAsync(() -> {
        G firstGenotype = firstHalfGenotypes.get(index);
        G secondGenotype = secondHalfGenotypes.get(index);

        S firstSolution = solutionMapper.apply(firstGenotype);
        S secondSolution = solutionMapper.apply(secondGenotype);

        O outcome = problem.outcomeFunction().apply(firstSolution, secondSolution);
        Q firstQuality = problem.firstQualityFunction().apply(outcome);
        Q secondQuality = problem.secondQualityFunction().apply(outcome);

        MEIndividual<G, S, Q> firstIndividual = MEIndividual.from(
            Individual.of(
                counter.getAndIncrement(),
                firstGenotype,
                firstSolution,
                firstQuality,
                0,
                0,
                List.of()
            ),
            descriptors
        );
        MEIndividual<G, S, Q> secondIndividual = MEIndividual.from(
            Individual.of(
                counter.getAndIncrement(),
                secondGenotype,
                secondSolution,
                secondQuality,
                0,
                0,
                List.of()
            ),
            descriptors
        );
        return List.of(firstIndividual, secondIndividual);
      }, executor);

      futures.add(future);
    }

    Collection<MEIndividual<G, S, Q>> individuals = futures.stream()
        .map(CompletableFuture::join)
        .flatMap(Collection::stream)
        .toList();

    MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> newState =
        MEPopulationState.empty(problem, stopCondition(), descriptors);

    return newState.updatedWithIteration(
        populationSize,
        populationSize,
        newState.archive().updated(individuals, MEIndividual::bins, partialComparator(problem)));
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> state
  ) throws SolverException {
    List<MEIndividual<G, S, Q>> individuals = new ArrayList<>(state.archive().asMap().values().stream().toList());
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    List<ChildGenotype<G>> newChildGenotypes = IntStream.range(0, populationSize)
        .mapToObj(j -> Misc.pickRandomly(individuals, random))
        .map(p -> new ChildGenotype<>(counter.getAndIncrement(), mutation.mutate(p.genotype(), random), List.of(p.id())))
        .toList();

    individuals.addAll(
        newChildGenotypes.stream().map(
            g -> MEIndividual.from(
                Individual.from(g, solutionMapper, s -> null, state.nOfQualityEvaluations()),
                descriptors
            )
        ).toList()
    );

    Collections.shuffle(individuals, random);
    List<MEIndividual<G, S, Q>> firstHalfIndividuals;
    int half = individuals.size() / 2;
    if (individuals.size() % 2 == 0) {
      firstHalfIndividuals = individuals.subList(0, half);
    } else {
      firstHalfIndividuals = individuals.subList(0, half + 1);
    }
    List<MEIndividual<G, S, Q>> secondHalfIndividuals = individuals.subList(half, individuals.size());

    List<CompletableFuture<List<MEIndividual<G, S, Q>>>> futures = new ArrayList<>();
    for (int i = 0; i < firstHalfIndividuals.size(); i++) {
      int index = i;

      CompletableFuture<List<MEIndividual<G, S, Q>>> future = CompletableFuture.supplyAsync(() -> {
        MEIndividual<G, S, Q> firstIndividual = firstHalfIndividuals.get(index);
        MEIndividual<G, S, Q> secondIndividual = secondHalfIndividuals.get(index);

        O outcome = state.problem().outcomeFunction().apply(firstIndividual.solution(), secondIndividual.solution());
        Q newFirstQuality = state.problem().firstQualityFunction().apply(outcome);
        Q newSecondQuality = state.problem().secondQualityFunction().apply(outcome);

        firstIndividual = Objects.isNull(firstIndividual.quality())
            ? firstIndividual.updateQuality(newFirstQuality, state.nOfIterations())
            : firstIndividual.updateQuality(fitnessReducer.apply(firstIndividual.quality(), newFirstQuality), state.nOfIterations());
        secondIndividual = Objects.isNull(secondIndividual.quality())
            ? secondIndividual.updateQuality(newSecondQuality, state.nOfIterations())
            : secondIndividual.updateQuality(fitnessReducer.apply(secondIndividual.quality(), newSecondQuality), state.nOfIterations());

        // If populationSize is odd, the last element of firstHalfGenotypes is the same as the first element of secondHalfGenotypes
        if (individuals.size() % 2 != 0 && index != 0) {
          return List.of(firstIndividual);
        }
        return List.of(firstIndividual, secondIndividual);
      }, executor);

      futures.add(future);

    }

    Collection<MEIndividual<G, S, Q>> newAndOldPopulation = futures.stream()
        .map(CompletableFuture::join)
        .flatMap(Collection::stream)
        .toList();

    Archive<MEIndividual<G, S, Q>> newArchive = new Archive<>(state.archive().binUpperBounds());

    return state.updatedWithIteration(
        populationSize,
        populationSize,
        newArchive.updated(newAndOldPopulation, MEIndividual::bins, partialComparator(state.problem()))
    );
  }
}
