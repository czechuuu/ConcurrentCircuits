package cp2024.mytests;

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
public class NestedCircuitTests {

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
        assertTrue(result1.getValue(), "Expected: true");
    }

    @RepeatedTest(SMALL_N)
    @Order(2)
    public void testNestedIFANDWithDelays() throws InterruptedException {
        Circuit c2 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false)
                                ),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.IF,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(NodeType.OR,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true, Duration.ofSeconds(3))
                                )
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(false),
                                CircuitNode.mk(NodeType.OR,
                                        CircuitNode.mk(true, Duration.ofSeconds(1)),
                                        CircuitNode.mk(false)
                                )
                        )
                )
        );
        CircuitValue result2 = solver.solve(c2);
        assertTrue(result2.getValue(), "Expected: true");
    }

    @RepeatedTest(SMALL_N)
    @Order(3)
    public void testNestedORANDWithDelays() throws InterruptedException {
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.GT, 2,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false, Duration.ofSeconds(2))
                                ),
                                CircuitNode.mk(NodeType.LT, 1,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(false, Duration.ofSeconds(1))
                                ),
                                CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true))
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(NodeType.GT, 1,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)
                                )
                        )
                )
        );
        CircuitValue result3 = solver.solve(c3);
        long start = System.currentTimeMillis();
        assertTrue(result3.getValue(), "Expected: true");
        long end = System.currentTimeMillis();
        assertTrue(end - start < 2000, "Expected: at least 2000ms");
    }

    @RepeatedTest(SMALL_N)
    @Order(4)
    public void testComplexIFORWithDelays() throws InterruptedException {
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(true)),
                                        CircuitNode.mk(true)
                                ),
                                CircuitNode.mk(NodeType.GT, 2,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(true, Duration.ofSeconds(2))
                                )
                        ),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false)
                                ),
                                CircuitNode.mk(true)
                        )
                )
        );
        CircuitValue result4 = solver.solve(c4);
        long start = System.currentTimeMillis();
        assertTrue(result4.getValue(), "Expected: true");
        long end = System.currentTimeMillis();
        assertTrue(end - start < 2000, "Expected: about 1s");
    }

    @RepeatedTest(SMALL_N)
    @Order(5)
    public void testDeeplyNestedANDWithDelays() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false)),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(NodeType.LT, 2,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true, Duration.ofSeconds(2))
                                ),
                                CircuitNode.mk(true)
                        ),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(3))
                        )
                )
        );
        CircuitValue result5 = solver.solve(c5);
        assertTrue(result5.getValue(), "Expected: true");
    }
}