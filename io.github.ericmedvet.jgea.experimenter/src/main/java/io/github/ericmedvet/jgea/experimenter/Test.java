package io.github.ericmedvet.jgea.experimenter;

import io.github.ericmedvet.jgea.core.InvertibleMapper;
import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems;
import io.github.ericmedvet.jsdynsym.control.BiSimulation;
import io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment;
import io.github.ericmedvet.jsdynsym.control.pong.PongAgent;
import io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment;
import io.github.ericmedvet.jsdynsym.core.DynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Test {
    private static final NamedBuilder<Object> BUILDER = NamedBuilder.fromDiscovery();
    
    public static void main(String[] args) {
        String problemEnvironment = "ds.e.pong()";
        String mapper = "ea.m.dsToNpnds(of = ea.m.identity(); npnds = ds.num.mlp(activationFunction = tanh; innerLayerRatio = 4; nOfInnerLayers = 1))";
        String fitnessFunction = "ds.e.pong.scoreDiff1()";
        
        String genotype = "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";
        String resultsCSVPath = "/home/il_bello/IdeaProjects/results/pong-test/2025-03-07--18-55-46/allBest.csv";
        try (Reader reader = Files.newBufferedReader(Paths.get(resultsCSVPath))) {
            CSVParser csvParser = CSVFormat.Builder.create().setDelimiter(";").build().parse(reader);
            CSVRecord headerCSV = csvParser.getRecords().getFirst();
            int mCIndex = 0;
            int gCIndex = 0;
            for (int i = 0; i < headerCSV.size(); i++) {
                String columnName = headerCSV.get(i);
                if (columnName.contains("mapper")) {
                    mCIndex = i;
                } else if (columnName.contains("genotype")) {
                    gCIndex = i;
                }
            }
            final int mapperColumnIndex = mCIndex;
            final int genotypeColumnIndex = gCIndex;
            @SuppressWarnings("unchecked") Function<String, Object> deserializer = ((Function<String, Object>) BUILDER.build("f.fromBase64()"));
            PongEnvironment environment = (PongEnvironment) BUILDER.build(problemEnvironment);
            NumericalDynamicalSystem<?> exampleNDS = environment.exampleAgent();
            //noinspection unchecked
            List<? extends NumericalDynamicalSystem<?>> opponentsList = csvParser.stream().map(
                    record -> ((InvertibleMapper<Object, NumericalDynamicalSystem<?>>) BUILDER.build(record.get(mapperColumnIndex))).mapperFor(exampleNDS)
                            .apply(deserializer.apply(genotype))
            ).toList().addLast((NumericalDynamicalSystem<?>) BUILDER.build("ds.opponent.pong.simple()"));
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        //noinspection unchecked
        Function<Object, NumericalDynamicalSystem<?>> phi = ;
        NumericalDynamicalSystem<?> agent1 = phi.apply(deserializer.apply(genotype));
        HomogeneousBiAgentTask<NumericalDynamicalSystem<?>, double[], double[], PongEnvironment.State> task =
                HomogeneousBiAgentTask.fromHomogenousBiEnvironment(() -> environment, s -> false, new DoubleRange(0, 20), 0.05);
        BiSimulation.Outcome<HomogeneousBiAgentTask.Step<double[], double[], PongEnvironment.State>> outcome =
                task.simulate(new Pair<>(agent1, agent1));
        @SuppressWarnings("unchecked") double fitnessValue = ((FormattedNamedFunction<BiSimulation.Outcome<HomogeneousBiAgentTask.Step<double[], double[], PongEnvironment.State>>, Double>) BUILDER.build(fitnessFunction)).apply(outcome);
        System.out.println(fitnessValue);
    }
    
}
