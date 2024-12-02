package cp2024.mytests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComplexTests {

    private static final int N = 100; // Define the constant N
    private static final int SMALL_N = 5;
    private static CircuitSolver solver;

    @BeforeAll
    public static void setUpAll() {
        solver = new ParallelCircuitSolver();
    }

    @AfterAll
    public static void tearDownAll() {
        solver.stop();
    }

    @RepeatedTest(N)
    public void testORWithNestedGTAndLT() throws InterruptedException {
        Circuit c1 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(false),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(3))
                        ),
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(false),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result1 = solver.solve(c1);
        assertTrue(result1.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    public void testANDWithNestedORAndNOT() throws InterruptedException {
        Circuit c2 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(false),
                                CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true, Duration.ofSeconds(1))),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue result2 = solver.solve(c2);
        assertTrue(result2.getValue(), "Expected: true");
    }

    @RepeatedTest(SMALL_N)
    public void testNestedIFWithComplexConditionAndDelay() throws InterruptedException {
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 1,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.LT, 2,
                                        CircuitNode.mk(false, Duration.ofSeconds(2)),
                                        CircuitNode.mk(true)
                                )
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result3 = solver.solve(c3);
        assertTrue(result3.getValue(), "Expected: true");
    }

    @RepeatedTest(SMALL_N)
    public void testComplexThresholdWithNestedANDAndIF() throws InterruptedException {
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(NodeType.IF,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                )
                        ),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue result4 = solver.solve(c4);
        assertTrue(result4.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    public void testDeeplyNestedORWithANDGTAndIF() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.IF,
                                        CircuitNode.mk(NodeType.LT, 2,
                                                CircuitNode.mk(true),
                                                CircuitNode.mk(false),
                                                CircuitNode.mk(true, Duration.ofSeconds(3))
                                        ),
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false)
                                ),
                                CircuitNode.mk(NodeType.GT, 1,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                )
                        ),
                        CircuitNode.mk(NodeType.IF,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.LT, 2,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result5 = solver.solve(c5);
        assertTrue(result5.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    @Order(Integer.MAX_VALUE)
    public void testSolverStop() {
        solver.stop();
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(3))
                )
        );
        try {
            CircuitValue result6 = solver.solve(c6);
            result6.getValue();
            fail("Expected computation to be interrupted");
        } catch (InterruptedException e) {
            // Expected exception
        }
    }
}