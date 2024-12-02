package cp2024.mytests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ComprehensiveTests {

    private static final int N = 20; // Define the constant N

    private static CircuitSolver solver;

    @BeforeAll
    public static void setUp() {
        solver = new ParallelCircuitSolver();
    }

    @AfterAll
    public static void tearDown() {
        solver.stop();
    }

    @RepeatedTest(N)
    public void testGT() throws InterruptedException {
        Circuit c1 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue result1 = solver.solve(c1);
        assertTrue(result1.getValue(), "Expected GT result: true");
    }

    @RepeatedTest(N)
    public void testLT() throws InterruptedException {
        Circuit c2 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result2 = solver.solve(c2);
        assertTrue(result2.getValue(), "Expected LT result: true");
    }

    @RepeatedTest(N)
    public void testNotGT() throws InterruptedException {
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.NOT,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result3 = solver.solve(c3);
        assertFalse(result3.getValue(), "Expected NOT (GT result): false");
    }

    @RepeatedTest(N)
    public void testIfGT() throws InterruptedException {
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result4 = solver.solve(c4);
        assertTrue(result4.getValue(), "Expected IF result (GT condition): true");
    }

    @RepeatedTest(N)
    public void testIfLT() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(false),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result5 = solver.solve(c5);
        assertTrue(result5.getValue(), "Expected IF result (LT condition): true");
    }

    @Test
    public void testDelayedNot() throws InterruptedException {
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.NOT,
                        CircuitNode.mk(true, Duration.ofSeconds(3))
                )
        );
        CircuitValue result6 = solver.solve(c6);
        assertFalse(result6.getValue(), "Expected delayed NOT result: false");
    }

    @Test
    public void testDelayedIf() throws InterruptedException {
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result7 = solver.solve(c7);
        assertTrue(result7.getValue(), "Expected delayed IF result (true condition): true");
    }

    @RepeatedTest(N)
    public void testCombinedGT_LT_IF() throws InterruptedException {
        Circuit c8 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 1,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(false),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result8 = solver.solve(c8);
        assertTrue(result8.getValue(), "Expected combined GT/LT IF result: true");
    }

    @RepeatedTest(N)
    public void testEdgeCase() throws InterruptedException {
        Circuit c9 = new Circuit(
                CircuitNode.mk(NodeType.LT, 1,
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result9 = solver.solve(c9);
        assertTrue(result9.getValue(), "Expected edge case result: true");
    }

    @RepeatedTest(N)
    public void testDeeplyNestedIF() throws InterruptedException {
        Circuit c10 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.IF,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.LT, 2,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false)
                                ),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue result10 = solver.solve(c10);
        assertTrue(result10.getValue(), "Expected deeply nested IF result: true");
    }
}