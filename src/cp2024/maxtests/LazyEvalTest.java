package cp2024.maxtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;

public class LazyEvalTest {
  CircuitSolver pSolver;
  
  private String milToSec(long elapsed) {
    return "No lazy eval, elapsed " + elapsed/1000 + " s.";
  }

  @BeforeEach
  void initialize() {
    pSolver = new ParallelCircuitSolver();
  }

  @Test
  void lazyEval1() {
    Circuit c = new Circuit(CircuitNode.mk(NodeType.OR, CircuitNode.mk(false, Duration.ofSeconds(10)),
                                                         CircuitNode.mk(false, Duration.ofSeconds(10)),
                                                         CircuitNode.mk(true),
                                                         CircuitNode.mk(false, Duration.ofSeconds(10))));
    long start = System.currentTimeMillis();
    Boolean val = TestUtils.getVal(pSolver.solve(c));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(true, val);

    assertTrue(elapsed < 100, milToSec(elapsed));
  }

  @Test
  void lazyEval2() {
    Circuit c = new Circuit(CircuitNode.mk(NodeType.OR, CircuitNode.mk(false, Duration.ofSeconds(10)),
                            CircuitNode.mk(false, Duration.ofSeconds(10)),
                            CircuitNode.mk(NodeType.NOT, CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true, Duration.ofSeconds(1)))),
                            CircuitNode.mk(false, Duration.ofSeconds(10))));

    long start = System.currentTimeMillis();
    Boolean val = TestUtils.getVal(pSolver.solve(c));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(true, val);

    assertTrue(elapsed < 2_000, milToSec(elapsed));
  }

  @Test
  void lazyEval3() {
    CircuitNode NOT_NOT_TRUE = CircuitNode.mk(NodeType.NOT,
                                              CircuitNode.mk(NodeType.NOT,
                                                              CircuitNode.mk(true, Duration.ofSeconds(1))));


    Circuit c = new Circuit(CircuitNode.mk(NodeType.OR,  
    CircuitNode.mk(false, Duration.ofSeconds(10)), CircuitNode.mk(false, Duration.ofSeconds(10)),
                                                         CircuitNode.mk(false, Duration.ofSeconds(10)),
                                                         CircuitNode.mk(NodeType.AND, 
                                                                        CircuitNode.mk(true, Duration.ofSeconds(1)), 
                                                                        NOT_NOT_TRUE
                                                                       ),
                                                         CircuitNode.mk(false, Duration.ofSeconds(10)))
    );
    long start = System.currentTimeMillis();
    Boolean val = TestUtils.getVal(pSolver.solve(c));
    long elapsed = System.currentTimeMillis() - start;
    assertEquals(true, val);

    assertTrue(elapsed < 1_100, milToSec(elapsed));
  }

  @Test
  void lazyEval4() {
    CircuitNode NOT_NOT_FALSE = CircuitNode.mk(NodeType.NOT,
                                              CircuitNode.mk(NodeType.NOT,
                                              CircuitNode.mk(false, Duration.ofSeconds(1))));
    CircuitNode[] arr = new CircuitNode[5];
    for (int j = 0; j < arr.length - 1; ++j) {
      arr[j] = CircuitNode.mk(true, Duration.ofSeconds(10));
    }

    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < arr.length - 1; ++j) {
        arr[j] = CircuitNode.mk(NodeType.NOT, arr[j]);
      }
    }

    arr[arr.length - 1] = NOT_NOT_FALSE;

    Circuit c = new Circuit(CircuitNode.mk(NodeType.AND, arr));

    long start = System.currentTimeMillis();
    Boolean val = TestUtils.getVal(pSolver.solve(c));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(false, val);
    assertTrue(elapsed < 1_100, milToSec(elapsed));
  }

  @Test
  void lazyEval5() {
    int availableProcessors = Runtime.getRuntime().availableProcessors();

    CircuitNode[] nodes = new CircuitNode[availableProcessors * 3];

    for (int i = 0; i < nodes.length - 1; ++i) {
      nodes[i] = CircuitNode.mk(true, Duration.ofSeconds(10));
    }
    nodes[nodes.length - 1] = CircuitNode.mk(false, Duration.ofSeconds(1));

    Circuit c = new Circuit(CircuitNode.mk(NodeType.AND, nodes));

    long start = System.currentTimeMillis();
    boolean val = TestUtils.getVal(pSolver.solve(c));
    long elapsed = System.currentTimeMillis() - start;

    assertEquals(false, val);
    assertTrue(elapsed < 1_100, milToSec(elapsed));
  }
}

