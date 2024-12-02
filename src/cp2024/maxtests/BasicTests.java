package cp2024.maxtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;


public class BasicTests {
  static CircuitSolver pSolver;

  @BeforeAll
  static void initialize() {
    pSolver = new ParallelCircuitSolver(); 
  }

  @AfterAll
  static void end() {
    pSolver.stop();
  }

  @Test
  void trivialTest() {
      assertTrue(TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(true)))));
      assertFalse(TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(false)))));
  }

  @Test
  void checkAND() {
    boolean[][] inputs = {{true, true, true}, {true, false, true}, {false, true, true}, {false, true, false}, {false, false, false}};
    boolean[] outpus = {true, false, false, false, false};

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.AND, TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }

  @Test
  void checkOR() {
    boolean[][] inputs = {{true, true, true}, {true, false, true}, {false, true, true}, {false, true, false}, {false, false, false}};
    boolean[] outpus = {true, true, true, true, false};

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.OR, TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }
  
  @Test
  void checkIF() {
    boolean[][] inputs = {{true, true, false}, {false, true, false}};
    boolean[] outpus = {true, false};

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.IF, TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }

  @Test
  void checkNOT() {
    boolean[][] inputs = {{true}, {false}};
    boolean[] outpus = {false, true};

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.NOT, TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }

  @Test
  void checkGT() {
    boolean[][] inputs = {{true, true, true}, {true}, {false, true, false}, {true, false, true}};
    boolean[] outpus = {false, false, false, true};
    int thresholds[] = {3, 2, 1, 1}; 

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.GT, thresholds[i], TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }

  @Test
  void checkLT() {
    boolean[][] inputs = {{true, true, true}, {false}, {false, false, false}, {false, false, false}};
    boolean[] outpus = {true, false, true, true};
    int thresholds[] = {4, 0, 1, 2}; 

    for (int i = 0; i < inputs.length; ++i) {
      boolean val = TestUtils.getVal(pSolver.solve(new Circuit(CircuitNode.mk(NodeType.LT, thresholds[i], TestUtils.args(inputs[i])))));
      assertEquals(outpus[i], val);
    }
  }
}