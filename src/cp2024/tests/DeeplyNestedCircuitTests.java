package cp2024.solution;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

public class DeeplyNestedCircuitTests {
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
        System.out.println("Test 1 (Nested AND/OR/GT/LT with delays): " + test1.getValue());  // Expected: true

        // Test 2: Deeply nested IF and AND with conditions and delays
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
        CircuitValue test2 = solver.solve(c2);
        System.out.println("Test 2 (IF with nested AND and LT): " + test2.getValue());  // Expected: true

        // Test 3: Nested AND with multiple OR conditions, delays, and GT check
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
        CircuitValue test3 = solver.solve(c3);
        System.out.println("Test 3 (AND with nested OR/GT/LT): " + test3.getValue());  // Expected: false

        // Test 4: Complex IF and OR with deeply nested conditions
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
        CircuitValue test4 = solver.solve(c4);
        System.out.println("Test 4 (Complex IF and OR): " + test4.getValue());  // Expected: true

        // Test 5: Nested IF and OR with delays and multiple conditions
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
        CircuitValue test5 = solver.solve(c5);
        System.out.println("Test 5 (Nested IF/OR with delays): " + test5.getValue());  // Expected: false

        // Test 6: Complex LT and GT with nested conditions, delays, and NOTs
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
        CircuitValue test6 = solver.solve(c6);
        System.out.println("Test 6 (AND with nested LT, GT, and NOT): " + test6.getValue());  // Expected: false

        // Test 7: Deeply nested LT, GT, AND, OR, and NOT to test concurrency limits
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
        CircuitValue test7 = solver.solve(c7);
        System.out.println("Test 7 (Deeply nested AND, OR, LT, GT, NOT): " + test7.getValue());  // Expected: false

        // Test 8: Nested combinations of OR and AND with long delays and IF conditions
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
        CircuitValue test8 = solver.solve(c8);
        System.out.println("Test 8 (OR with AND, IF, NOT and delays): " + test8.getValue());  // Expected: true

        // Final check after solver stop
        solver.stop();
        Circuit c9 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(3))
                )
        );
        try {
            CircuitValue test9 = solver.solve(c9);
            System.out.println("Test 9 (Solver stopped): " + test9.getValue());
        } catch (InterruptedException e) {
            System.out.println("Test 9 (Solver stopped): Computation interrupted.");
        }

        System.out.println("All nested tests completed.");
    }
}
