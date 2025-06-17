package io.github.ericmedvet.jgea.core.representation.sequence.integer;

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class SequentialIntStringFactory extends UniformUniqueIntStringFactory {

  public SequentialIntStringFactory(int lowerBound, int upperBound, int size) {
    super(lowerBound, upperBound, size);
  }

  @Override
  public IntString build(RandomGenerator random) {
    return new IntString(
        IntStream.range(lowerBound, upperBound).boxed().toList().subList(0, Math.min(upperBound-lowerBound,size)),
        lowerBound,
        upperBound
    );
  }
}
