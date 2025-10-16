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
package io.github.ericmedvet.jgea.experimenter.drawer;

import io.github.ericmedvet.jgea.core.representation.programsynthesis.Program;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ProgramExecutionException;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.*;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Base;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Composed;
import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.TypeException;
import io.github.ericmedvet.jgea.core.representation.tree.numeric.Element;
import io.github.ericmedvet.jgea.problem.programsynthesis.Problems;
import io.github.ericmedvet.jgea.problem.programsynthesis.ProgramSynthesisProblem;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import java.io.File;
import java.util.List;
import java.util.Set;

public class VideoMaker {

  private static void triLongestStringbiggerNetwork() throws NoSuchMethodException, NetworkStructureException, TypeException, ProgramExecutionException {
    Network n = new Network(
        List.of(
            Gate.input(Base.STRING),
            Gate.input(Base.STRING),
            Gate.input(Base.STRING),
            Gates.sSplitter(),
            Gates.sSplitter(),
            Gates.sSplitter(),
            Gates.length(),
            Gates.length(),
            Gates.length(),
            Gates.iBefore(),
            Gates.select(),
            Gates.sSplitter(),
            Gates.length(),
            Gates.iBefore(),
            Gates.select(),
            Gate.output(Base.STRING),
            Gates.noop(),
            Gates.noop(),
            Gates.noop(),
            Gates.noop(),
            Gates.noop()
        ),
        Set.of(
            Wire.of(0, 0, 16, 0),
            Wire.of(16, 0, 3, 0),

            Wire.of(0, 0, 10, 1),
            Wire.of(1, 0, 4, 0),
            Wire.of(1, 0, 10, 0),
            Wire.of(3, 0, 6, 0),
            Wire.of(4, 0, 7, 0),
            Wire.of(6, 0, 9, 0),
            Wire.of(7, 0, 18, 0),
            Wire.of(18, 0, 9, 1),

            Wire.of(9, 0, 10, 2),
            Wire.of(10, 0, 17, 0),
            Wire.of(17, 0, 11, 0),

            Wire.of(10, 0, 14, 1),
            Wire.of(2, 0, 5, 0),
            Wire.of(5, 0, 19, 0),
            Wire.of(19, 0, 8, 0),

            Wire.of(8, 0, 13, 1),
            Wire.of(2, 0, 14, 0),
            Wire.of(11, 0, 12, 0),
            Wire.of(12, 0, 13, 0),
            Wire.of(13, 0, 14, 2),

            Wire.of(14, 0, 20, 0),
            Wire.of(20, 0, 15, 0)
        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    drawer.show(n);
    Runner runner = new Runner(100000, 100000, 100000, 10000, false);

    Runner.TTPNInstrumentedOutcome outcome = runner.run(
        n,
        List.of(
            "somethingverylong",
            "dog",
            "ocean"
        )
    );
    System.out.println("Wire contents: " + outcome.wireContents());
    System.out.println("Outputs: " + outcome.outputs());

    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-triLongestStringbiggerNetwork.mp4"), outcome);
    System.out.println(outcome);
    System.out.println(outcome.wireContents());
  }

  private static void triLongestString() throws NoSuchMethodException, NetworkStructureException, TypeException, ProgramExecutionException {
    Network n = new Network(
        List.of(
            Gate.input(Base.STRING),
            Gate.input(Base.STRING),
            Gate.input(Base.STRING),
            Gates.sSplitter(),
            Gates.sSplitter(),
            Gates.sSplitter(),
            Gates.length(),
            Gates.length(),
            Gates.length(),
            Gates.iBefore(),
            Gates.select(),
            Gates.sSplitter(),
            Gates.length(),
            Gates.iBefore(),
            Gates.select(),
            Gate.output(Base.STRING)
        ),
        Set.of(
            Wire.of(0, 0, 3, 0),
            Wire.of(0, 0, 10, 1),
            Wire.of(1, 0, 4, 0),
            Wire.of(1, 0, 10, 0),
            Wire.of(3, 0, 6, 0),
            Wire.of(4, 0, 7, 0),
            Wire.of(6, 0, 9, 0),
            Wire.of(7, 0, 9, 1),
            Wire.of(9, 0, 10, 2),
            Wire.of(10, 0, 11, 0),
            Wire.of(10, 0, 14, 1),
            Wire.of(2, 0, 5, 0),
            Wire.of(5, 0, 8, 0),
            Wire.of(8, 0, 13, 1),
            Wire.of(2, 0, 14, 0),
            Wire.of(11, 0, 12, 0),
            Wire.of(12, 0, 13, 0),
            Wire.of(13, 0, 14, 2),
            Wire.of(14, 0, 15, 0)
        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    drawer.show(n);
    Runner runner = new Runner(100000, 100000, 100000, 10000, false);

    Runner.TTPNInstrumentedOutcome outcome = runner.run(
        n,
        List.of(
            "somethingverylong",
            "dog",
            "ocean"
        )
    );
    System.out.println("Wire contents: " + outcome.wireContents());
    System.out.println("Outputs: " + outcome.outputs());

    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-triLongestStringGoodNetwork.mp4"), outcome);
    System.out.println(outcome);
    System.out.println(outcome.wireContents());
  }


  private static void vProduct() throws NoSuchMethodException, NetworkStructureException, TypeException, ProgramExecutionException {
    Network n = new Network(
        List.of(
            Gate.input(Composed.sequence(Base.REAL)),
            Gate.input(Composed.sequence(Base.REAL)),
            Gates.splitter(),
            Gates.splitter(),
            Gates.rPMathOperator(Element.Operator.MULTIPLICATION),
            Gates.rSPSum(),
            Gate.output(Base.REAL)
        ),
        Set.of(
            Wire.of(0, 0, 2, 0),
            Wire.of(1, 0, 3, 0),
            Wire.of(2, 0, 4, 0),
            Wire.of(3, 0, 4, 1),
            Wire.of(4, 0, 5, 0),
            Wire.of(5, 0, 5, 1),
            Wire.of(5, 0, 6, 0)


        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    drawer.show(n);
    Runner runner = new Runner(1000, 1000, 1000, 100, false);
    Runner.TTPNInstrumentedOutcome outcome = runner.run(
        n,
        List.of(
            List.of(1.0, 2.0, 3.0),
            List.of(4.0, 5.0, 6.0)
        )
    );
    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-vProduct.mp4"), outcome);
    System.out.println(outcome);
  }

  private static void sLengther() throws NoSuchMethodException, NetworkStructureException, TypeException, ProgramExecutionException {
    Network n = new Network(
        List.of(
            Gate.input(Composed.sequence(Base.STRING)),
            Gates.splitter(),
            Gates.sSplitter(),
            Gates.length(),
            Gates.pairer(),
            Gates.sPSequencer(),
            Gate.output(Composed.sequence(Composed.tuple(List.of(Base.STRING, Base.INT))))
        ),
        Set.of(
            Wire.of(0, 0, 1, 0),
            Wire.of(1, 0, 2, 0),
            Wire.of(1, 0, 4, 0),
            Wire.of(2, 0, 3, 0),
            Wire.of(3, 0, 4, 1),
            Wire.of(4, 0, 5, 0),
            Wire.of(5, 0, 5, 1),
            Wire.of(5, 0, 6, 0)

        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    drawer.show(n);
    Runner runner = new Runner(1000, 1000, 1000, 100, false);
    Runner.TTPNInstrumentedOutcome outcome = runner.run(
        n,
        List.of(
            List.of("somethingverylong")
        )
    );
    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-sLengther.mp4"), outcome);
    System.out.println(outcome);
  }


  private static void iArraySum() throws NoSuchMethodException, NetworkStructureException, TypeException, ProgramExecutionException {
    Program target = Program.from(Problems.class.getMethod("iArraySum", List.class));
    Network n = new Network(
        List.of(
            Gate.input(Composed.sequence(Base.INT)),
            Gates.splitter(),
            Gates.iToR(),
            Gates.rSPSum(),
            Gates.rToI(),
            Gate.output(Base.INT),
            Gates.iConst(5)
        ),
        Set.of(
            Wire.of(0, 0, 1, 0),
            Wire.of(1, 0, 2, 0),
            Wire.of(2, 0, 3, 0),
            Wire.of(3, 0, 4, 0),
            Wire.of(3, 0, 3, 1),
            Wire.of(4, 0, 5, 0),
            Wire.of(1, 0, 6, 0)
        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    drawer.show(n);
    Runner runner = new Runner(1000, 1000, 1000, 100, false);
    Runner.TTPNInstrumentedOutcome outcome = runner.run(n, List.of(List.of(1, 2, 3, 4, 5, 6, 7, 8)));
    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-iArrayMax.mp4"), outcome);
    System.out.println(outcome);
  }

  private static void iBiMax() throws NetworkStructureException, TypeException {
    NamedBuilder<?> nb = NamedBuilder.fromDiscovery();
    ProgramSynthesisProblem psb = (ProgramSynthesisProblem) nb.build("ea.p.ps.synthetic(name = \"iBiMax\")");
    // good solution
    Network goodNetwork = new Network(
        List.of(
            Gate.input(Base.INT),
            Gate.input(Base.INT),
            Gate.output(Base.INT),
            Gates.iBefore(),
            Gates.select()
        ),
        Set.of(
            Wire.of(0, 0, 3, 0),
            Wire.of(1, 0, 3, 1),
            Wire.of(0, 0, 4, 1),
            Wire.of(1, 0, 4, 0),
            Wire.of(3, 0, 4, 2),
            Wire.of(4, 0, 2, 0)
        )
    );
    TTPNDrawer drawer = new TTPNDrawer(TTPNDrawer.Configuration.DEFAULT);
    //drawer.show(goodNetwork);
    Runner runner = new Runner(100, 100, 100, 100, false);
    // check
    psb.caseProvider()
        .stream()
        .forEach(
            e -> System.out.printf(
                "in=%s\tactualOut=%s\tpredOut=%s%n",
                e.input(),
                e.output().outputs(),
                runner.run(goodNetwork, e.input()).outputs()
            )
        );
    Runner.TTPNInstrumentedOutcome outcome = runner.run(goodNetwork, List.of(3, 4));
    System.out.println(outcome);
    TTPNOutcomeVideoBuilder videoBuilder = new TTPNOutcomeVideoBuilder(TTPNOutcomeVideoBuilder.Configuration.DEFAULT);
    videoBuilder.save(new File("../ttpn-iBiMax.mp4"), outcome);
  }

  public static void main(
      String[] args
  ) throws NetworkStructureException, ProgramExecutionException, NoSuchMethodException, TypeException {
    //iArraySum();
    //triLongestString();
    //iBiMax();
    vProduct();
    //sLengther();
    //triLongestStringbiggerNetwork();
  }
}
