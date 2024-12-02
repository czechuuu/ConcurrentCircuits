package cp2024.maxtests;


import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.NodeType;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;
import cp2024.maxtests.CircuitGenerator.BalancedRandomCircuitGenerator;
import cp2024.maxtests.CircuitGenerator.DeepRandomCircuitGenerator;
import cp2024.maxtests.CircuitGenerator.DeterministicNodeGenerator;
import cp2024.maxtests.CircuitGenerator.RandomCircuitGenerator;
import cp2024.maxtests.CircuitGenerator.RandomCircuitGenerator.CircuitWithStats;
import cp2024.maxtests.TestUtils.Pair;

public class PerfomanceTest {
  private final int deepCircuitNodes = 5_000;
  private final int depthBalancedCircuit = 15;
  private final int iterations = 1_000;
  private CircuitSolver seqSolver;
  private CircuitSolver parSolver;

  @BeforeEach
  void initialize() {
    seqSolver = new SequentialSolver();
    parSolver = new ParallelCircuitSolver();
  }

  @AfterEach
  void stop() {
    seqSolver.stop();
    parSolver.stop();
  }

  @Test
  void speedTest1() {
    RandomCircuitGenerator[] generators = {
                                           new DeepRandomCircuitGenerator(deepCircuitNodes, new Random()),
                                           new BalancedRandomCircuitGenerator(12, new Random(), 10)
                                          };
    // Store execution times
    long[] totalSequentialTime = new long[generators.length];
    long[] totalParallelTime = new long[generators.length];
    

    for (int j = 0; j < generators.length; ++j) {
      var generator = generators[j];
      for (int i = 0; i < iterations; ++i) {
        CircuitWithStats cws = generator.generateCircuit();
        Circuit circuit = cws.getCircuit();

        Pair<Boolean, Long> seqPair = TestUtils.measureExecutionNanos(seqSolver, circuit);
        totalSequentialTime[j] += seqPair.second;


        Pair<Boolean, Long> parPair = TestUtils.measureExecutionNanos(parSolver, circuit);
        totalParallelTime[j] += parPair.second;
        
        assertEquals(seqPair.first, parPair.first);
      }
    }

    long[] avgSequentialTime = new long[generators.length];
    long[] avgParallelTime = new long[generators.length];

    long totSeq = 0;
    long totPar = 0;

    for (int i = 0; i < generators.length; ++i) {
      avgSequentialTime[i] = totalSequentialTime[i] / iterations / 1000;
      avgParallelTime[i] = totalParallelTime[i] / iterations / 1_000;
      totSeq += avgSequentialTime[i];
      totPar += avgParallelTime[i];

      System.out.println(generators[i] + ": Average Sequential Solver Time: " + avgSequentialTime[i] + " microseconds");
      System.out.println(generators[i] + ": Average Parallel Solver Time: " + avgParallelTime[i] + " microseconds");
      System.out.println(generators[i] + ": Speedup " + (double) avgSequentialTime[i] / avgParallelTime[i]);
    }
    System.out.println("Total Average Sequential Solver Time: " + totSeq / generators.length + " microseconds");
    System.out.println("Total Average Parallel Solver Time: " + totPar / generators.length + " microseconds");
    System.out.println("Total Speedup: " + (double) totSeq / totPar);
  }

  @Test
  void speedTest2() {
    RandomCircuitGenerator[] generators = {
      new DeepRandomCircuitGenerator(deepCircuitNodes, new Random()),
      new BalancedRandomCircuitGenerator(depthBalancedCircuit, new Random())
    };

    Circuit[] circuits = new Circuit[iterations];
    for (int i = 0; i < iterations; ++i) {
      circuits[i] = generators[i % generators.length].generateCircuit().getCircuit();
    }

    CircuitValue[] resultsSeq = new CircuitValue[iterations];
    CircuitValue[] resultsPar = new CircuitValue[iterations];
    System.out.println("START");
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; ++i) {
      resultsSeq[i] = seqSolver.solve(circuits[i]);
    }
    System.out.println("Seq finished");
    long sequentialTime = System.nanoTime() - startTime;
    try { 
      startTime = System.nanoTime();
      for (int i = 0; i < iterations; ++i) {
        resultsPar[i] = parSolver.solve(circuits[i]);
      }

      for (int i = 0; i < iterations; ++i) {
        resultsPar[i].getValue();
      }

      long parallelTime = System.nanoTime() - startTime;

      for (int i = 0; i < iterations; ++i) {
        assertEquals(resultsSeq[i].getValue(), resultsPar[i].getValue(), "Test" + i);
      }

      System.out.println("Sequential Solver Time: " + sequentialTime / 1000 + " microseconds");
      System.out.println("Parallel Solver Time: " + parallelTime / 1000 + " microseconds");
      System.out.println("Speedup " + (double) sequentialTime / parallelTime);

    } catch (InterruptedException e) {
      assertTrue(false, "Should not happen");
    }
  }

  @Test
  void speedTest3() {

    RandomCircuitGenerator gen = new DeterministicNodeGenerator(new Random(), depthBalancedCircuit,
                                                                NodeType.AND, true);

    long pElapsed = 0;
    long sElapsed = 0;
    int iterations = 1;

    for (int i = 0; i < iterations; ++i) {
      CircuitWithStats cs = gen.generateCircuit();
      Circuit c = cs.getCircuit();

      Pair<Boolean, Long> seqPair = TestUtils.measureExecutionNanos(seqSolver, c);
      sElapsed += seqPair.second;
      
      Pair<Boolean, Long> parPair = TestUtils.measureExecutionNanos(parSolver, c);
      pElapsed += parPair.second;

      assertEquals(seqPair.first, parPair.first);
    }

    // make microsecond 
    sElapsed /= 1_000;
    pElapsed /= 1_000;

    System.out.println("Ave Seq time: "+ sElapsed / iterations + " microseconds");
    System.out.println("Ave Par time: "+ pElapsed / iterations + " microseconds");
    System.out.println("Speedup: " + (double) sElapsed / pElapsed);
  }
}
