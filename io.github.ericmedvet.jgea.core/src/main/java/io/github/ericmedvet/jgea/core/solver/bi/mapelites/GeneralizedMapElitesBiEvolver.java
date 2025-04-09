/*-
 * ========================LICENSE_START=================================
 * jgea-core
 * %%
 * Copyright (C) 2018 - 2025 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.core.solver.bi.mapelites;

import io.github.ericmedvet.jgea.core.Factory;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.order.PartialComparator;
import io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem;
import io.github.ericmedvet.jgea.core.solver.Individual;
import io.github.ericmedvet.jgea.core.solver.SolverException;
import io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver;
import io.github.ericmedvet.jgea.core.solver.mapelites.Archive;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual;
import io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState;
import io.github.ericmedvet.jgea.core.solver.mapelites.MapElites;
import io.github.ericmedvet.jgea.core.util.Misc;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jnb.datastructure.TriFunction;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneralizedMapElitesBiEvolver<G, S, Q, O> extends AbstractBiEvolver<MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>, QualityBasedBiProblem<S, O, Q>, MEIndividual<G, S, Q>, G, S, Q, O> {

  protected final int populationSize;
  private final Mutation<G> mutation;
  private final List<MapElites.Descriptor<G, S, Q>> descriptors;
  private final boolean emptyArchive;
  //TODO opponentsSelector deve ricevere una collection di MEIndividual con le coordinate per risolvere il problema degli scontri con la nuova popolazione (anche in init)
  //TODO ragionare sul conteggio delle fitness evaluation
  private final TriFunction<MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>, MEIndividual<G, S, Q>, RandomGenerator, List<MEIndividual<G, S, Q>>> opponentsSelector;
  private final Function<List<Q>, Q> fitnessAggregator;
  
  public GeneralizedMapElitesBiEvolver(
      Function<? super G, ? extends S> solutionMapper,
      Factory<? extends G> genotypeFactory,
      Predicate<? super MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>> stopCondition,
      Mutation<G> mutation,
      int populationSize,
      List<MapElites.Descriptor<G, S, Q>> descriptors,
      BinaryOperator<Q> fitnessReducer,
      boolean emptyArchive,
      List<PartialComparator<? super MEIndividual<G, S, Q>>> additionalIndividualComparators,
      TriFunction<MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>>, MEIndividual<G, S, Q>, RandomGenerator, List<MEIndividual<G, S, Q>>> opponentsSelector,
      Function<List<Q>, Q> fitnessAggregator
  ) {
    super(solutionMapper, genotypeFactory, stopCondition, false, fitnessReducer, additionalIndividualComparators);
    this.populationSize = populationSize;
    this.mutation = mutation;
    this.descriptors = descriptors;
    this.emptyArchive = emptyArchive;
    this.opponentsSelector = opponentsSelector;
    this.fitnessAggregator = fitnessAggregator;
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> init(
      QualityBasedBiProblem<S, O, Q> problem,
      RandomGenerator random,
      ExecutorService executor
  ) throws SolverException {
    // create new genotypes and split in two list of opponents
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
    // build futures
    List<Future<List<MEIndividual<G, S, Q>>>> futures = new ArrayList<>();
    for (int i = 0; i < firstHalfGenotypes.size(); i++) {
      int index = i;
      Future<List<MEIndividual<G, S, Q>>> future = executor.submit(() -> {
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
      });
      futures.add(future);
    }
    // extract future results
    Collection<MEIndividual<G, S, Q>> individuals = futures.stream()
        .map(listFuture -> {
          try {
            return listFuture.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        })
        .flatMap(Collection::stream)
        .toList();
    MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> newState = MEPopulationState.empty(
        problem,
        stopCondition(),
        descriptors
    );
    return newState.updatedWithIteration(
        populationSize,
        futures.size(),
        newState.archive().updated(individuals, MEIndividual::bins, partialComparator(problem))
    );
  }

  @Override
  public MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> update(
      RandomGenerator random,
      ExecutorService executor,
      MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> state
  ) throws SolverException {
    List<MEIndividual<G, S, Q>> individuals = new ArrayList<>(state.archive().asMap().values());
    AtomicLong counter = new AtomicLong(state.nOfBirths());
    List<ChildGenotype<G>> newChildGenotypes = IntStream.range(0, populationSize)
        .mapToObj(j -> Misc.pickRandomly(individuals, random))
        .map(
            p -> new ChildGenotype<>(
                counter.getAndIncrement(),
                mutation.mutate(p.genotype(), random),
                List.of(p.id())
            )
        )
        .toList();
    individuals.addAll(
        newChildGenotypes.stream()
            .map(
                g -> MEIndividual.from(
                    Individual.from(g, solutionMapper, s -> null, state.nOfQualityEvaluations()),
                    descriptors
                )
            )
            .toList()
    );
    List<Future<Pair<MEIndividual<G, S, Q>, Integer>>> futures = new ArrayList<>();
    for (MEIndividual<G, S, Q> individual : individuals) {
      futures.add(executor.submit(() -> evaluateIndividual(state, individual, state.nOfIterations(), random)));
    }
    Collection<Pair<MEIndividual<G, S, Q>, Integer>> newPopulationPair = futures.stream()
        .map(future -> {
          try {
            return future.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        })
        .toList();
    PartialComparator<? super Individual<?, ?, ?>> updaterComparator = (
        newI,
        existingI
    ) -> newI == existingI ? PartialComparator.PartialComparatorOutcome.BEFORE : PartialComparator.PartialComparatorOutcome.AFTER;
    Archive<MEIndividual<G, S, Q>> archive;
    if (emptyArchive) {
      archive = new Archive<>(state.archive().binUpperBounds());
    } else {
      archive = state.archive().updated(newPopulationPair.stream().map(Pair::first).toList(), MEIndividual::bins, updaterComparator);
    }
    archive = archive.updated(newPopulationPair.stream().map(Pair::first).toList(), MEIndividual::bins, partialComparator(state.problem()));
    return state.updatedWithIteration(populationSize, newPopulationPair.stream().map(Pair::second).reduce(Integer::sum).orElse(0), archive);
  }

  private Pair<MEIndividual<G, S, Q>, Integer> evaluateIndividual(
      MEPopulationState<G, S, Q, QualityBasedBiProblem<S, O, Q>> state,
      MEIndividual<G, S, Q> individual,
      long iteration,
      RandomGenerator random
  ) {
    List<MEIndividual<G, S, Q>> opponents = opponentsSelector.apply(state, individual, random);
    if (opponents.isEmpty()) {
      throw new IllegalArgumentException("Unexpected empty list of opponents");
    } else {
      List<Q> qualityList = new ArrayList<>();
      for (MEIndividual<G, S, Q> opponent : opponents) {
        O outcome = state.problem().outcomeFunction().apply(individual.solution(), opponent.solution());
        Q computedQuality = state.problem().firstQualityFunction().apply(outcome);
        qualityList.add(computedQuality);
      }
      return new Pair<>(individual.updateQuality(fitnessAggregator.apply(qualityList), iteration), opponents.size());
    }
  }
}
