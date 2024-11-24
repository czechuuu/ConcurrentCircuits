package cp2024.solution;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;

import java.time.Duration;

public class NestedCircuitTests {
    public static void main(String[] args) throws InterruptedException {
        CircuitSolver solver = new ParallelCircuitSolver();

        // Deeply nested AND and OR with various conditions and delays
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
        CircuitValue test1 = solver.solve(c1);
        System.out.println("Test 1 (Deeply nested AND, OR with GT, LT, and NOT) Expected: true : " + test1.getValue());

        // Deeply nested IF with delays to test short-circuiting and concurrency
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
        CircuitValue test2 = solver.solve(c2);
        System.out.println("Test 2 (Deeply nested IF with AND, OR, and delays) Expected: true : " + test2.getValue());

        // Nested OR within AND with a mix of GT and LT thresholds and delays
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
        CircuitValue test3 = solver.solve(c3);
        System.out.println("Test 3 (Nested OR in AND with GT and LT thresholds) Expected: true : " + test3.getValue());

        // Complex IF-OR combination to check short-circuiting behavior with delayed nodes
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
        CircuitValue test4 = solver.solve(c4);
        System.out.println("Test 4 (Complex IF-OR with GT, LT, NOT) Expected: true : " + test4.getValue());

        // Deeply nested AND with NOT and delays to check the cancellation mechanism
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
        CircuitValue test5 = solver.solve(c5);
        System.out.println("Test 5 (Deeply nested AND with NOT and delays) Expected: true : " + test5.getValue());

        System.out.println("All nested tests completed.");
        solver.stop();
    }
}
