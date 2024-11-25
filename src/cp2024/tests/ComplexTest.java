package cp2024.tests;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

public class ComplexTest {
    public static void main(String[] args) throws InterruptedException {
        CircuitSolver solver = new ParallelCircuitSolver();

        // OR with nested GT and LT expressions
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
        CircuitValue test1 = solver.solve(c1);
        System.out.println("Test 1 (OR with GT and LT) Expected: true: " + test1.getValue());  // Expected: true

        // AND with nested OR and NOT statements
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
        CircuitValue test2 = solver.solve(c2);
        System.out.println("Test 2 (AND with OR and NOT) Expected: true: " + test2.getValue());  // Expected: false (due to NOT true)

        // Nested IF with a complex condition and delay
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
        CircuitValue test3 = solver.solve(c3);
        System.out.println("Test 3 (Nested IF with GT and LT) Expected: true: " + test3.getValue());  // Expected: true

        // Complex threshold with nested AND and IF
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
        CircuitValue test4 = solver.solve(c4);
        System.out.println("Test 4 (GT with nested AND and IF) Expected: true: " + test4.getValue());  // Expected: false

        // Deeply nested OR with AND, GT, and IF to test concurrency limits
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
        CircuitValue test5 = solver.solve(c5);
        System.out.println("Test 5 (Deeply nested OR with AND, GT, IF) Expected: true : " + test5.getValue());  // Expected: true

        // Final check after solver stop
        solver.stop();
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(3))
                )
        );
        try {
            CircuitValue test6 = solver.solve(c6);
            System.out.println("Test 6 (Solver stopped): " + test6.getValue());
        } catch (InterruptedException e) {
            System.out.println("Test 6 (Solver stopped): Computation interrupted.");
        }

        System.out.println("Complex test completed.");
    }
}
