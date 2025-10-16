/*-
 * ========================LICENSE_START=================================
 * jgea-problem
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
package io.github.ericmedvet.jgea.problem.programsynthesis;

import io.github.ericmedvet.jgea.core.representation.programsynthesis.type.Typed;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Problems {
  private Problems() {
  }

  @Typed("s")
  public static String biLongestString(@Typed("s") String s1, @Typed("s") String s2) {
    return s1.length() >= s2.length() ? s1 : s2;
  }

  @Typed("i")
  public static Integer rIntSum(@Typed("r") Double v1, @Typed("r") Double v2) {
    return (int) (v1 + v2);
  }

  @Typed("i")
  public static Integer iArraySum(@Typed("[i]") List<Integer> is) {
    return is.stream().reduce(Integer::sum).orElse(0);
  }

  @Typed("i")
  public static Integer iBiMax(@Typed("i") Integer v1, @Typed("i") Integer v2) {
    return Math.max(v1, v2);
  }

  @Typed("i")
  public static Integer iTriMax(@Typed("i") Integer v1, @Typed("i") Integer v2, @Typed("i") Integer v3) {
    return Math.max(v1, Math.max(v2, v3));
  }

  @Typed("[r]")
  public static List<Double> vScProduct(@Typed("[r]") List<Double> vs, @Typed("r") Double r) {
    return vs.stream().map(v -> v * r).toList();
  }

  @Typed("[<S,I>]")
  public static List<List<Object>> sLengther(@Typed("[s]") List<String> strings) {
    return strings.stream().map(s -> List.<Object>of(s, s.length())).toList();
  }

  @Typed("s")
  public static String triLongestString(@Typed("s") String s1, @Typed("s") String s2, @Typed("s") String s3) {
    return Stream.of(s1, s2, s3).max(Comparator.comparingInt(String::length)).orElseThrow();
  }

  @Typed("r")
  public static Double vProduct(@Typed("[r]") List<Double> v1, @Typed("[r]") List<Double> v2) {
    if (v1.size() != v2.size()) {
      throw new IllegalArgumentException("Input sizes are different: %d and %d".formatted(v1.size(), v2.size()));
    }
    return IntStream.range(0, v1.size()).mapToDouble(i -> v1.get(i) * v2.get(i)).sum();
  }

  @Typed("i")
  public static Integer remainder(@Typed("i") Integer v1, @Typed("i") Integer v2) {
    return (v1 % v2);
  }

}
