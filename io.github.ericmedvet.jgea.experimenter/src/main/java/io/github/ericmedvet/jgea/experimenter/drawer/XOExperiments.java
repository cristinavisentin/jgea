/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
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
package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jgea.core.operator.Crossover;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ProgramExecutionException;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.*;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Base;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.TypeException;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.problem.programsynthesis.ProgramSynthesisProblem;
import io.github.ericmedvet.jnb.core.NamedBuilder;

import java.util.*;
import java.util.random.RandomGenerator;

public class XOExperiments {

  public static void main(
      String[] args
  ) throws NetworkStructureException, ProgramExecutionException, NoSuchMethodException, TypeException {

    Network rIntSumGoodNetwork = new Network(
        List.of(
            Gate.input(Base.REAL),
            Gate.input(Base.REAL),
            Gates.rPMathOperator(Element.Operator.ADDITION),
            Gates.rToI(),
            Gate.output(Base.INT)
        ),
        Set.of(
            Wire.of(0, 0, 2, 0),
            Wire.of(1, 0, 2, 1),
            Wire.of(2, 0, 3, 0),
            Wire.of(3, 0, 4, 0)

        )
    );

    Network rIntSumBiggerNetwork = new Network(
        List.of(
            Gate.input(Base.REAL),
            Gate.input(Base.REAL),
            Gates.rSPMult(),
            Gates.noop(),
            Gates.rPMathOperator(Element.Operator.ADDITION),
            Gates.rToI(),
            Gates.iConst(5),
            Gates.repeater(),
            Gates.noop(),
            Gates.sPSequencer(),
            Gates.splitter(),
            Gate.output(Base.INT)
        ),
        Set.of(
            Wire.of(0, 0, 2, 0),
            Wire.of(2, 0, 2, 1),
            Wire.of(1, 0, 3, 0),
            Wire.of(3, 0, 4, 1),
            Wire.of(2, 0, 4, 0),
            Wire.of(4, 0, 5, 0),
            Wire.of(5, 0, 7, 0),
            Wire.of(3, 0, 6, 0),
            Wire.of(6, 0, 7, 1),
            Wire.of(7, 0, 8, 0),
            Wire.of(8, 0, 9, 0),
            Wire.of(9, 0, 9, 1),
            Wire.of(9, 0, 10, 0),
            Wire.of(10, 0, 11, 0)

        )
    );

    NamedBuilder<?> nb = NamedBuilder.fromDiscovery();
    ProgramSynthesisProblem rIntSumpsb = (ProgramSynthesisProblem) nb.build(
        "ea.p.ps.synthetic(name = \"rIntSum\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
    );
//    ProgramSynthesisProblem biLongestStringpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"biLongestString\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem iArraySumpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"iArraySum\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem iBiMaxpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"iBiMax\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem iTriMaxpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"iTriMax\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem vScProductpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"vScProduct\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem sLengtherpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"sLengther\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem triLongestStringpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"triLongestString\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem vProductpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"vProduct\"; metrics = [smooth_fail_rate ; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );
//    ProgramSynthesisProblem remainderpsb = (ProgramSynthesisProblem) nb.build(
//        "ea.p.ps.synthetic(name = \"remainder\"; metrics = [fail_rate; avg_raw_dissimilarity; exception_error_rate; profile_avg_steps; profile_avg_tot_size])"
//    );

    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    Runner runner = new Runner(100, 1000, 1000, 100, false);

    //drawer.show(rIntSumgoodNetwork);
    //drawer.show(rIntSumbiggerNetwork);

    Set<Network> xodNetworks = new HashSet<>();
    xodNetworks.add(rIntSumGoodNetwork);
    xodNetworks.add(rIntSumBiggerNetwork);

    int times = 2;

    Crossover<Network> xo = new NetworkCrossover(30, 0.5, 20, true);
    RandomGenerator rnd = new Random(3);

    Network parent1 = rIntSumGoodNetwork;
    Network parent2 = rIntSumBiggerNetwork;

    double totalFailRate = 0;
    double totalAvgRawDissimilarity = 0;
    double totalProfileAvgSteps = 0;

    int neutralCount = 0;


    for (int i=0; i<times; i++) {
      Network xod = xo.recombine(parent1, parent2, rnd);
      xodNetworks.add(xod);

      neutralCount += (xod.equals(rIntSumGoodNetwork) || xod.equals(rIntSumBiggerNetwork)) ? 1 : 0;

      //drawer.show(xod);

      List<Network> networkList = new ArrayList<>(xodNetworks);

      parent1 = networkList.get(rnd.nextInt(networkList.size()));
      parent2 = networkList.get(rnd.nextInt(networkList.size()));

      while (parent1.equals(parent2)) {
        parent2 = networkList.get(rnd.nextInt(networkList.size()));
      }

      rIntSumpsb.caseProvider().all().forEach(c -> System.out.println(c.input()) );
      Map<String, Double> qualityMetrics = rIntSumpsb.qualityFunction().apply(runner.asInstrumentedProgram(xod));

      double failRate = qualityMetrics.get("fail_rate");
      double avgRawDissimilarity = qualityMetrics.get("avg_raw_dissimilarity");
      double profileAvgSteps = qualityMetrics.get("profile_avg_steps");

      totalFailRate += failRate;
      totalAvgRawDissimilarity += avgRawDissimilarity;
      totalProfileAvgSteps += profileAvgSteps;

    }
    double uniqueness = xodNetworks.size();
    double neutrality = neutralCount;

    System.out.printf(
        "Uniqueness: %.4f | Neutrality: %.4f | Fail Rate: %.4f | Avg Raw Dissimilarity: %.4f | Steps: %.4f%n",
        (uniqueness - 2) / times,
        neutrality / times,
        totalFailRate / times,
        totalAvgRawDissimilarity / times,
        totalProfileAvgSteps / times
    );
  }
}

