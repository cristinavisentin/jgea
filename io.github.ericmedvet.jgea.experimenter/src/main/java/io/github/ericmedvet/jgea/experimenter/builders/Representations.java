/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2024 Eric Medvet
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
package io.github.ericmedvet.jgea.experimenter.builders;

import io.github.ericmedvet.jgea.core.IndependentFactory;
import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.operator.Mutation;
import io.github.ericmedvet.jgea.core.problem.Problem;
import io.github.ericmedvet.jgea.core.problem.ProblemWithExampleSolution;
import io.github.ericmedvet.jgea.core.representation.sequence.FixedLengthListFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.bit.BitStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringFlipMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.IntStringUniformCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.integer.UniformIntStringFactory;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.GaussianMutation;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.SegmentGeometricCrossover;
import io.github.ericmedvet.jgea.core.representation.sequence.numeric.UniformDoubleFactory;
import io.github.ericmedvet.jgea.core.representation.tree.*;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.experimenter.Representation;
import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.Pair;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

@Discoverable(prefixTemplate = "ea.representation|r")
public class Representations {
  private Representations() {
  }

  public interface ExampleBasedRepresentationBuilder<G, S> extends RepresentationBuilder<G, S, ProblemWithExampleSolution<S>> {  }

  public interface RepresentationBuilder<G, S, P extends Problem<S>> extends BiFunction<P, Function<S,G>, Representation<G>> {}

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> ExampleBasedRepresentationBuilder<BitString, S> bitString(
      @Param(value = "pMutRate", dD = 1d) double pMutRate
  ) {
    return (p, im) -> {
      BitString g = im.apply(p.example());
      return new Representation<>(
          new BitStringFactory(g.size()),
          new BitStringFlipMutation(pMutRate / (double) g.size()),
          Crossover.from(new BitStringUniformCrossover()
              .andThen(new BitStringFlipMutation(pMutRate / (double) g.size())))
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> ExampleBasedRepresentationBuilder<List<Double>, S> doubleString(
      @Param(value = "initialMinV", dD = -1d) double initialMinV,
      @Param(value = "initialMaxV", dD = 1d) double initialMaxV,
      @Param(value = "sigmaMut", dD = 0.35d) double sigmaMut
  ) {
    return (p, im) -> new Representation<>(
        new FixedLengthListFactory<>(
            im.apply(p.example()).size(),
            new UniformDoubleFactory(initialMinV, initialMaxV)
        ),
        new GaussianMutation(sigmaMut),
        Crossover.from(new SegmentGeometricCrossover().andThen(new GaussianMutation(sigmaMut)))
    );
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> ExampleBasedRepresentationBuilder<IntString, S> intString(
      @Param(value = "pMutRate", dD = 1d) double pMutRate
  ) {
    return (p, im) -> {
      IntString g = im.apply(p.example());
      return new Representation<>(
          new UniformIntStringFactory(
              g.lowerBound(),
              g.upperBound(),
              g.size()
          ),
          new IntStringFlipMutation(pMutRate / (double) g.size()),
          Crossover.from(new IntStringUniformCrossover()
              .andThen(new IntStringFlipMutation(pMutRate / (double) g.size())))
      );
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> ExampleBasedRepresentationBuilder<List<Tree<Element>>, S> multiSRTree(
      @Param(
          value = "constants",
          dDs = {0.1, 1, 10})
      List<Double> constants,
      @Param(
          value = "operators",
          dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"})
      List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH
  ) {
    return (p, im) -> {
      List<Tree<Element>> g = im.apply(p.example());
      List<Element.Variable> variables = g.stream()
          .map(t -> t.visitDepth().stream()
              .filter(e -> e instanceof Element.Variable)
              .map(e -> ((Element.Variable) e).name())
              .toList())
          .flatMap(List::stream)
          .distinct()
          .map(Element.Variable::new)
          .toList();
      List<Element.Constant> constantElements =
          constants.stream().map(Element.Constant::new).toList();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables), IndependentFactory.picker(constantElements));
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      IndependentFactory<List<Tree<Element>>> treeListFactory = new FixedLengthListFactory<>(
          g.size(),
          new TreeIndependentFactory<>(minTreeH, maxTreeH, x -> 2, nonTerminalFactory, terminalFactory, 0.5)
      );
      // single tree factory
      TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
      // subtree between same position trees
      SubtreeCrossover<Element> subtreeCrossover = new SubtreeCrossover<>(maxTreeH);
      Crossover<List<Tree<Element>>> pairWiseSubtreeCrossover =
          (list1, list2, rnd) -> IntStream.range(0, list1.size())
              .mapToObj(i -> subtreeCrossover.recombine(list1.get(i), list2.get(i), rnd))
              .toList();
      // swap trees
      Crossover<List<Tree<Element>>> uniformCrossover = (list1, list2, rnd) -> IntStream.range(0, list1.size())
          .mapToObj(i -> rnd.nextDouble() < 0.5 ? list1.get(i) : list2.get(i))
          .toList();
      // subtree mutation
      SubtreeMutation<Element> subtreeMutation = new SubtreeMutation<>(maxTreeH, treeBuilder);
      Mutation<List<Tree<Element>>> allSubtreeMutations = (list, rnd) ->
          list.stream().map(t -> subtreeMutation.mutate(t, rnd)).toList();
      return new Representation<>(
          treeListFactory, List.of(allSubtreeMutations), List.of(pairWiseSubtreeCrossover, uniformCrossover));
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <G1, G2, S, P extends Problem<S>> RepresentationBuilder<Pair<G1, G2>, S, P> pair(
      @Param("first") RepresentationBuilder<G1, S, P> r1, @Param("second") RepresentationBuilder<G2, S, P> r2) {
    return (p, im) -> {
      return Representation.pair(r1.apply(p, s -> im.apply(s).first()), r2.apply(p, s -> im.apply(s).second()));
    };
  }

  @SuppressWarnings("unused")
  @Cacheable
  public static <S> ExampleBasedRepresentationBuilder<Tree<Element>, S> srTree(
      @Param(
          value = "constants",
          dDs = {0.1, 1, 10})
      List<Double> constants,
      @Param(
          value = "operators",
          dSs = {"addition", "subtraction", "multiplication", "prot_division", "prot_log"})
      List<Element.Operator> operators,
      @Param(value = "minTreeH", dI = 4) int minTreeH,
      @Param(value = "maxTreeH", dI = 10) int maxTreeH
  ) {
    return (p,im) -> {
      Tree<Element> g = im.apply(p.example());
      List<Element.Variable> variables = g.visitDepth().stream()
          .filter(e -> e instanceof Element.Variable)
          .map(e -> ((Element.Variable) e).name())
          .distinct()
          .map(Element.Variable::new)
          .toList();
      List<Element.Constant> constantElements =
          constants.stream().map(Element.Constant::new).toList();
      IndependentFactory<Element> terminalFactory = IndependentFactory.oneOf(
          IndependentFactory.picker(variables), IndependentFactory.picker(constantElements));
      IndependentFactory<Element> nonTerminalFactory = IndependentFactory.picker(operators);
      // single tree factory
      TreeBuilder<Element> treeBuilder = new GrowTreeBuilder<>(x -> 2, nonTerminalFactory, terminalFactory);
      return new Representation<>(
          new RampedHalfAndHalf<>(minTreeH, maxTreeH, x -> 2, nonTerminalFactory, terminalFactory),
          new SubtreeMutation<>(maxTreeH, treeBuilder),
          new SubtreeCrossover<>(maxTreeH)
      );
    };
  }
}
