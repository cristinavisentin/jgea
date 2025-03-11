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
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

public class XOExperiments {

  public static void main(
      String[] args
  ) throws NetworkStructureException, ProgramExecutionException, NoSuchMethodException, TypeException {

    Network rIntSumgoodNetwork = new Network(
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

    Network rIntSumbiggerNetwork = new Network(
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

    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    Runner runner = new Runner(100, 1000, 1000, 100, false);

    System.out.println(
        rIntSumpsb.qualityFunction().apply(runner.asInstrumentedProgram((rIntSumgoodNetwork))).get("fail_rate")
    );
    System.out.println(
        rIntSumpsb.qualityFunction().apply(runner.asInstrumentedProgram((rIntSumbiggerNetwork))).get("fail_rate")
    );


    //drawer.show(rIntSumgoodNetwork);
    //drawer.show(rIntSumbiggerNetwork);

    Crossover<Network> xo = new NetworkCrossover(30, 0.5, 20, true);
    RandomGenerator rnd = new Random(3);
    Network xod = xo.recombine(rIntSumgoodNetwork, rIntSumbiggerNetwork, rnd);
    drawer.show(xod);

    System.out.println(rIntSumpsb.qualityFunction().apply(runner.asInstrumentedProgram((xod))).get("fail_rate"));

    //    rIntSumpsb.caseProvider()
    //        .stream()
    //        .forEach(
    //            e -> {
    //              InstrumentedProgram.InstrumentedOutcome outcome = runner.run(xod, e.input());
    //              System.out.printf(
    //                  "in=%s\tactualOut=%s\tpredOut=%s exc=%s%n",
    //                  e.input(),
    //                  e.output().outputs(),
    //                  outcome.outputs(),
    //                  outcome.exception()
    //              );
    //            }
    //        );


  }
}
