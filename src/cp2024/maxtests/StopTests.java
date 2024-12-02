package cp2024.maxtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;
import cp2024.maxtests.TestUtils.LeafNodeInterruptsCount;

public class StopTests {
  private CircuitSolver pSolver;

  @BeforeEach
  void initialize() {
    pSolver = new ParallelCircuitSolver();
  }

  private void testClosedSolver() {
    try {
      pSolver.solve(new Circuit(CircuitNode.mk(true, Duration.ofSeconds(10)))).getValue();
      assertTrue(false, "SOLVER NOT STOPPED");
    } catch (InterruptedException e) {}
  }
  @Test
  void test1() {
    Circuit[] circuits = new Circuit[200];
    CircuitValue[] results = new CircuitValue[circuits.length];

    int sleepTime = 10;

    for (int i = 0; i < circuits.length; ++i) {
      circuits[i] = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(sleepTime)));
      results[i] = pSolver.solve(circuits[i]);
    }
    TestUtils.activeWait(sleepTime * 1_000 + 200);
    pSolver.stop();
    testClosedSolver();

    for (int i = 0; i < circuits.length; ++i) {
      try {
        boolean result = results[i].getValue();
        assertTrue(result, "Wrong Result");
      } catch (InterruptedException e) {
        assertTrue(false, "ALL COMPUTATION MUST HAVE FINISHED!!! SOLVE IS NOT PARALLEL");
      }
    }
    testClosedSolver();

  }

  @Test
  void test2() {
    int sleepTime = 10;
    LeafNodeInterruptsCount[] leafs = new LeafNodeInterruptsCount[100];
    for (int i = 0; i < leafs.length; ++i) {
      leafs[i] = new LeafNodeInterruptsCount(true, Duration.ofSeconds(sleepTime));
    }
    Circuit circuit = new Circuit(CircuitNode.mk(NodeType.AND, leafs));
    CircuitValue val = pSolver.solve(circuit);

    // wait to make sure that all threads reached leafs
    TestUtils.activeWait(500);
    
    pSolver.stop();
    long start = System.currentTimeMillis();
    try {
      val.getValue();
      assertTrue(false, "NOT STOPPED");
    } catch (InterruptedException e) {}
    long elapsed = System.currentTimeMillis() - start;
    
    assertTrue(elapsed < 100, "Elapsed: "+ elapsed);
    testClosedSolver();
    try {
      start = System.currentTimeMillis();
      val.getValue();
    } catch (InterruptedException e) {
      elapsed = System.currentTimeMillis() - start;
      assertTrue(elapsed < 10, "MUST HAVE STOPPED IMMIDIATELY");
    }

    for (LeafNodeInterruptsCount leaf : leafs) {
      assertTrue(leaf.getCountInterrupts() == 1);
    }
  }

  @Test
  void test3() {
    Circuit[] circuits = new Circuit[60];
    int start = 2;
    
    for (int i = start; i < circuits.length + start; ++i) {
      LeafNodeInterruptsCount[] leafs = new LeafNodeInterruptsCount[i];
      int seconds = i - start < circuits.length / 2 ? 1 : 10;
      for (int j = 0; j < leafs.length; ++j) {
        leafs[j] = new LeafNodeInterruptsCount(true, Duration.ofSeconds(seconds));
      }
      circuits[i - start] = new Circuit(CircuitNode.mk(NodeType.AND, leafs));
    }
    System.out.println("here");
    CircuitValue[] vals = new CircuitValue[circuits.length];

    for (int i = 0; i < circuits.length; ++i) {
      vals[i] = pSolver.solve(circuits[i]);
    }

    TestUtils.activeWait(1_500);

    pSolver.stop();
    for (int i = 0; i < vals.length / 2; ++i) {
      try {
          assertTrue(vals[i].getValue());
      } catch (InterruptedException e) {
        assertTrue(false, "Not computed "+ i);
      }
    }
    for (int i = vals.length / 2; i < vals.length; ++i) {
      try {
        vals[i].getValue();
        assertTrue(false);
      } catch (InterruptedException e) {}
    }
  }

  @Test
  void test4() {
    LeafNodeInterruptsCount leaf = new LeafNodeInterruptsCount(false, Duration.ofSeconds(100));
    Circuit c = new Circuit(leaf);

    var val = pSolver.solve(c);
    new Thread(() -> pSolver.stop()).start();
    long start = System.currentTimeMillis();

    try {
      val.getValue();
      assertTrue(false);
    } catch (InterruptedException e) {}
    long elapsed = System.currentTimeMillis() - start;

    assertTrue(elapsed < 1.5, "Elapsed " + elapsed);
  }
}

