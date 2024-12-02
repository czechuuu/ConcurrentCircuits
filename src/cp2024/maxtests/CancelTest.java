package cp2024.maxtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;



import java.time.Duration;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;
import cp2024.maxtests.TestUtils.Pair;
import cp2024.maxtests.TestUtils.LeafNodeInterruptsCount;


public class CancelTest {
  CircuitSolver pSolver;
  
  private String milToSec(long elapsed) {
    return "No lazy eval, elapsed " + elapsed + " ms.";
  }
  
  @BeforeEach
  void initialize() {
    pSolver = new ParallelCircuitSolver();
  }

  @AfterEach
  void stop() {
    pSolver.stop();
  }

  @Test
  void cancelTest1() {
    LeafNodeInterruptsCount[] leafs = new LeafNodeInterruptsCount[4];
    for (int i = 0; i < leafs.length; ++i) {
      leafs[i] = new LeafNodeInterruptsCount(false, Duration.ofSeconds(10));
    }

    Circuit c = new Circuit(CircuitNode.mk(NodeType.OR, leafs[0], leafs[1], leafs[2], CircuitNode.mk(true, Duration.ofMillis(1)), leafs[3]));

    Pair<Boolean, Long> pair = TestUtils.measureExecutionMilis(pSolver, c);

    assertEquals(true, pair.first);

    assertTrue(pair.second < 100, milToSec(pair.second));
    
    // cancellation may take a bit
    TestUtils.activeWait(100);

    for (var leaf : leafs) {
      assertEquals(1, leaf.getCountInterrupts());
      assertEquals(1, leaf.getCalled());
    }
  }

  @Test
  void cancelTest2() {
    CircuitNode[] leafs = new CircuitNode[1_000];

    for (int i = 0; i < leafs.length; ++i) {
      leafs[i] = new LeafNodeInterruptsCount(true, Duration.ofSeconds(10));
    }

    final int false_time = 1000;
    // make super deep circuit
    // 50 x NOT = 0 x NOT
    leafs[leafs.length / 2] = TestUtils.deepNotLeafNode(50, CircuitNode.mk(false, Duration.ofMillis(false_time)));

    Circuit c = new Circuit(CircuitNode.mk(NodeType.AND, leafs));

    Pair<Boolean, Long> pair = TestUtils.measureExecutionMilis(pSolver, c);

    assertTrue(pair.second < false_time + 500, milToSec(pair.second));
    assertEquals(false, pair.first);

    TestUtils.activeWait(1_000);

    int interruptedCount = 0;
    int called = 0;
    for (int i = 0; i < leafs.length; ++i) {
      if (i == leafs.length / 2) continue;
      called += ((LeafNodeInterruptsCount) leafs[i]).getCalled();
      interruptedCount += ((LeafNodeInterruptsCount) leafs[i]).getCountInterrupts();
    }

    assertTrue(called == interruptedCount, "Called: "+ called + " Interrupted: " + interruptedCount);
    assertEquals(leafs.length - 1, called, "Called: "+ called);
  }

  @Test
  void cancelTest3() {
    int sz = 50;
    CircuitNode[] leafs = new CircuitNode[sz];
    LeafNodeInterruptsCount[] interruptLeafs = new LeafNodeInterruptsCount[sz];


    for (int i = 0; i < leafs.length; ++i) {
      int duration = i == leafs.length / 2 ? 1 : 10;

      interruptLeafs[i] = new LeafNodeInterruptsCount(i != leafs.length / 2, Duration.ofSeconds(duration));
      leafs[i] = TestUtils.deepNotLeafNode(50, interruptLeafs[i]);
    }
    // make super deep circuit
    // 50 x NOT = 0 x NOT
    Circuit c = new Circuit(CircuitNode.mk(NodeType.AND, leafs));

    Pair<Boolean, Long> pair = TestUtils.measureExecutionMilis(pSolver, c);

    assertEquals(false, pair.first);

    TestUtils.activeWait(1_000);

    int interruptedCount = 0;
    int called = 0;
    for (int i = 0; i < leafs.length; ++i) {
      if (i == leafs.length / 2) continue;
      called += interruptLeafs[i].getCalled();
      interruptedCount += interruptLeafs[i].getCountInterrupts();
    }

    assertTrue(called == interruptedCount, "Called: "+ called + " Interrupted: " + interruptedCount);
    assertEquals(leafs.length - 1, called, "Called: "+ called);
  }

  @RepeatedTest(100)
  void cancelIFBranch1() {
    LeafNodeInterruptsCount n1 = new LeafNodeInterruptsCount(false, Duration.ofSeconds(10));
    LeafNodeInterruptsCount n2 = new LeafNodeInterruptsCount(false, Duration.ofSeconds(10));
    LeafNodeInterruptsCount n3 = new LeafNodeInterruptsCount(false, Duration.ofSeconds(10));
    LeafNodeInterruptsCount n4 = new LeafNodeInterruptsCount(false, Duration.ofSeconds(10));

  
    // the first node must be given some sleep time to assert the interruption was called
    // otherwise may not be visited.
    Circuit c1 = new Circuit(CircuitNode.mk(NodeType.IF, CircuitNode.mk(true, Duration.ofMillis(1)),
                                                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                                                        n1));
    Circuit c2 = new Circuit(CircuitNode.mk(NodeType.IF, CircuitNode.mk(false, Duration.ofMillis(1)),
                                                        n2, CircuitNode.mk(true, Duration.ofSeconds(5))));

    Circuit c3 = new Circuit(CircuitNode.mk(NodeType.IF, n3,
                                                        CircuitNode.mk(true, Duration.ofSeconds(1)),
                                                        CircuitNode.mk(true, Duration.ofSeconds(1))));
                
    Circuit c4 = new Circuit(CircuitNode.mk(NodeType.IF, n4,
                                              CircuitNode.mk(false, Duration.ofSeconds(1)),
                                              CircuitNode.mk(false, Duration.ofSeconds(1))));

    assertTrue(TestUtils.getVal(pSolver.solve(c3)));
    assertFalse(TestUtils.getVal(pSolver.solve(c4)));

    pSolver.solve(c1);
    pSolver.solve(c2);
    

    TestUtils.activeWait(100);
    //assertTrue(n1.getCalled() == 1 && n1.getCalled() == n1.getCountInterrupts());
    assertTrue(n1.getCalled() == 0 || n1.getCountInterrupts() == 1);
    assertTrue(n2.getCalled() == 0 || n2.getCountInterrupts() == 1);
    assertTrue(n3.getCalled() == 1 && n3.getCalled() == n3.getCountInterrupts());
    assertTrue(n4.getCalled() == 1 && n4.getCalled() == n4.getCountInterrupts());
  }

}
