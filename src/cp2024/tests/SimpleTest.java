package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleTest {

    private static final int N = 100; // Define the constant N
    private CircuitSolver solver;

    @BeforeEach
    public void setUp() {
        solver = new ParallelCircuitSolver();
    }

    @AfterEach
    public void tearDown() {
        solver.stop();
    }

    @RepeatedTest(N)
    @Order(1)
    public void testTrueValueEvaluation() throws InterruptedException {
        Circuit c1 = new Circuit(CircuitNode.mk(true));
        CircuitValue test1 = solver.solve(c1);
        assertTrue(test1.getValue(), "Expected: true");
    }

    @Test
    @Order(2)
    public void testFalseValueEvaluationWithDelay() throws InterruptedException {
        Circuit c2 = new Circuit(CircuitNode.mk(false, Duration.ofSeconds(1)));
        CircuitValue test2 = solver.solve(c2);
        assertFalse(test2.getValue(), "Expected: false");
    }

    @RepeatedTest(N)
    @Order(3)
    public void testANDShortCircuiting() throws InterruptedException {
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue test3 = solver.solve(c3);
        assertFalse(test3.getValue(), "Expected: false");
    }

    @RepeatedTest(N)
    @Order(4)
    public void testORShortCircuiting() throws InterruptedException {
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(false),
                        CircuitNode.mk(false, Duration.ofSeconds(2)),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue test4 = solver.solve(c4);
        assertTrue(test4.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    @Order(5)
    public void testGTThresholdWithDelay() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue test5 = solver.solve(c5);
        assertTrue(test5.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    @Order(6)
    public void testLTThreshold() throws InterruptedException {
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true, Duration.ofSeconds(1))
                )
        );
        CircuitValue test6 = solver.solve(c6);
        assertFalse(test6.getValue(), "Expected: false");
    }

    @RepeatedTest(N)
    @Order(7)
    public void testIFConditionWithDelay() throws InterruptedException {
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue test7 = solver.solve(c7);
        assertFalse(test7.getValue(), "Expected: false");
    }

    @RepeatedTest(N)
    @Order(8)
    public void testIFConditionWithOtherBranchDelay() throws InterruptedException {
        Circuit c8 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(2))
                )
        );
        CircuitValue test8 = solver.solve(c8);
        assertTrue(test8.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    @Order(Integer.MAX_VALUE)
    public void testReAccessAfterStop() throws InterruptedException {
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue test7 = solver.solve(c7);
        assertFalse(test7.getValue(), "Expected: false");
        solver.stop();
        assertDoesNotThrow(test7::getValue);
        assertFalse(test7.getValue(), "Expected: false");
    }
}