package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class DeeplyNestedCircuitTests {

    private static final int N = 100;
    private static final int SMALL_N = 5; // Define the constant N
    private static CircuitSolver solver;

    @BeforeAll
    public static void setUpAll() {
        solver = new ParallelCircuitSolver();
    }

    @AfterAll
    public static void tearDownAll() {
        solver.stop();
    }

    @RepeatedTest(SMALL_N)
    @Order(1)
    public void testNestedANDORWithDelays() throws InterruptedException {
        Circuit c1 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(NodeType.OR,
                                                CircuitNode.mk(false),
                                                CircuitNode.mk(NodeType.GT, 1,
                                                        CircuitNode.mk(false),
                                                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                                                        CircuitNode.mk(true)
                                                )
                                        ),
                                        CircuitNode.mk(NodeType.LT, 2,
                                                CircuitNode.mk(false),
                                                CircuitNode.mk(true)
                                        )
                                ),
                                CircuitNode.mk(false, Duration.ofSeconds(3))
                        ),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false)),
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false, Duration.ofSeconds(1))
                        )
                )
        );
        CircuitValue result1 = solver.solve(c1);
        long start = System.currentTimeMillis();
        assertTrue(result1.getValue(), "Expected: true");
        long end = System.currentTimeMillis();
        assertTrue(end - start < 3000, "Expected: less than 3 seconds");
    }

    @RepeatedTest(N)
    @Order(2)
    public void testNestedIFANDWithDelays() throws InterruptedException {
        Circuit c2 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.LT, 3,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false))
                )
        );
        CircuitValue result2 = solver.solve(c2);
        assertTrue(result2.getValue(), "Expected: true");
    }

    @RepeatedTest(N)
    @Order(3)
    public void testNestedANDORGTWithDelays() throws InterruptedException {
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.GT, 1,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false, Duration.ofSeconds(1)),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result3 = solver.solve(c3);
        assertFalse(result3.getValue(), "Expected: false");
    }

    @RepeatedTest(SMALL_N)
    @Order(4)
    public void testComplexIFORWithDelays() throws InterruptedException {
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(false, Duration.ofSeconds(2)),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false, Duration.ofSeconds(1))
                                ),
                                CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false))
                        ),
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result4 = solver.solve(c4);
        assertTrue(result4.getValue(), "Expected: true");
    }

    // ! SEMI IMPORTANT
    // SHOULD TAKE AROUND 1 second
    @RepeatedTest(SMALL_N)
    @Order(5)
    public void testNestedIFORWithDelays() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 4,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false, Duration.ofSeconds(2)),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true))
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false, Duration.ofSeconds(1))
                        )
                )
        );
        CircuitValue result5 = solver.solve(c5);
        long start = System.currentTimeMillis();
        assertFalse(result5.getValue(), "Expected: false");
        long end = System.currentTimeMillis();
        assertTrue(end - start >= 1000, "Expected: at least 1 second");
        assertTrue(end - start < 2000, "Expected: less than 2 seconds");
    }

    @RepeatedTest(N)
    @Order(6)
    public void testComplexLTGTWithDelays() throws InterruptedException {
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.LT, 5,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false)
                        ),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true))
                )
        );
        CircuitValue result6 = solver.solve(c6);
        assertFalse(result6.getValue(), "Expected: false");
    }

    @RepeatedTest(N)
    @Order(7)
    public void testDeeplyNestedANDORLTGTNOT() throws InterruptedException {
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.LT, 3,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true, Duration.ofSeconds(1)),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(NodeType.GT, 2,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false, Duration.ofSeconds(2))
                                )
                        ),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true))
                )
        );
        CircuitValue result7 = solver.solve(c7);
        long start = System.currentTimeMillis();
        assertFalse(result7.getValue(), "Expected: false");
        long end = System.currentTimeMillis();
        assertTrue(end - start < 1000, "Expected: less than 1 second");
    }

    @RepeatedTest(N)
    @Order(8)
    public void testNestedORANDIFWithDelays() throws InterruptedException {
        Circuit c8 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.IF,
                                        CircuitNode.mk(NodeType.LT, 4,
                                                CircuitNode.mk(true),
                                                CircuitNode.mk(false, Duration.ofSeconds(1)),
                                                CircuitNode.mk(true)
                                        ),
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false)
                                ),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false, Duration.ofSeconds(2)))
                )
        );
        CircuitValue result8 = solver.solve(c8);
        long start = System.currentTimeMillis();
        assertTrue(result8.getValue(), "Expected: true");
        long end = System.currentTimeMillis();
        assertTrue(end - start < 1000, "Expected: less than a second");
    }

}