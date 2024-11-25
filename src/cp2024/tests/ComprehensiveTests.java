package cp2024.tests;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

public class ComprehensiveTests {
    public static void main(String[] args) throws InterruptedException {
        CircuitSolver solver = new ParallelCircuitSolver();

        // 1. Test for GT operation with a correct threshold
        Circuit c1 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),  // First argument (true)
                        CircuitNode.mk(true),  // Second argument (true)
                        CircuitNode.mk(false), // Third argument (false)
                        CircuitNode.mk(true)   // Fourth argument (true)
                )
        );
        System.out.println("Expected GT result: true"); // Expected: More than 2 true values
        CircuitValue result1 = solver.solve(c1);
        System.out.println("GT result: " + result1.getValue());

        // 2. Test for LT operation with a correct threshold
        Circuit c2 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(false), // First argument (false)
                        CircuitNode.mk(true),  // Second argument (true)
                        CircuitNode.mk(false)  // Third argument (false)
                )
        );
        System.out.println("Expected LT result: true"); // Expected: Less than 2 true values
        CircuitValue result2 = solver.solve(c2);
        System.out.println("LT result: " + result2.getValue());

        // 3. Test for NOT operation negating a GT result
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.NOT,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),  // First argument (true)
                                CircuitNode.mk(true),  // Second argument (true)
                                CircuitNode.mk(false), // Third argument (false)
                                CircuitNode.mk(true)   // Fourth argument (true)
                        )
                )
        );
        System.out.println("Expected NOT (GT result): false"); // Expected: NOT of true -> false
        CircuitValue result3 = solver.solve(c3);
        System.out.println("NOT (GT result): " + result3.getValue());

        // 4. Test for IF operation based on GT condition
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),  // First argument (true)
                                CircuitNode.mk(false), // Second argument (false)
                                CircuitNode.mk(true),  // Third argument (true)
                                CircuitNode.mk(true)   // Fourth argument (true)
                        ),
                        CircuitNode.mk(true),  // If true, return true
                        CircuitNode.mk(false)  // If false, return false
                )
        );
        System.out.println("Expected IF result (GT condition): true"); // Expected: GT should return true, so IF should return true
        CircuitValue result4 = solver.solve(c4);
        System.out.println("IF result (GT condition): " + result4.getValue());

        // 5. Test for IF operation based on LT condition
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(false), // First argument (false)
                                CircuitNode.mk(true),  // Second argument (true)
                                CircuitNode.mk(false)  // Third argument (false)
                        ),
                        CircuitNode.mk(true),   // If true, return true
                        CircuitNode.mk(false)   // If false, return false
                )
        );
        System.out.println("Expected IF result (LT condition): true"); // Expected: LT should return true, so IF should return true
        CircuitValue result5 = solver.solve(c5);
        System.out.println("IF result (LT condition): " + result5.getValue());
        System.out.println("SKIBIDI!");

        // 6. Test for delayed NOT operation
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.NOT,
                        CircuitNode.mk(true, Duration.ofSeconds(3))
                )
        );
        System.out.println("Expected delayed NOT result: false"); // Expected: NOT of true should be false, but delayed
        CircuitValue result6 = solver.solve(c6);
        System.out.println("Delayed NOT result: " + result6.getValue());

        // 7. Test for delayed IF operation with a true condition
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true, Duration.ofSeconds(3)),  // Delayed true condition
                        CircuitNode.mk(true),                          // Expected return if true
                        CircuitNode.mk(false)                          // Expected return if false
                )
        );
        System.out.println("Expected delayed IF result (true condition): true"); // Expected: IF should return true after delay
        CircuitValue result7 = solver.solve(c7);
        System.out.println("Delayed IF result (true condition): " + result7.getValue());

        // 8. Test for a combination of GT and LT within IF operation
        Circuit c8 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(NodeType.GT, 1,
                                CircuitNode.mk(true),  // First argument (true)
                                CircuitNode.mk(true),  // Second argument (true)
                                CircuitNode.mk(true)   // Third argument (true)
                        ),
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(false), // First argument (false)
                                CircuitNode.mk(true),  // Second argument (true)
                                CircuitNode.mk(false)  // Third argument (false)
                        ),
                        CircuitNode.mk(false) // If false condition, return false
                )
        );
        System.out.println("Expected combined GT/LT IF result: true"); // Expected: GT returns true, LT returns true, so IF condition should be true
        CircuitValue result8 = solver.solve(c8);
        System.out.println("Combined GT/LT IF result: " + result8.getValue());

        // 9. Test for an edge case with empty circuit and false leaf
        Circuit c9 = new Circuit(
                CircuitNode.mk(NodeType.LT, 1,
                        CircuitNode.mk(false) // Single false argument
                )
        );
        System.out.println("Expected edge case result: true"); // Expected: Less than 1 true should return true
        CircuitValue result9 = solver.solve(c9);
        System.out.println("Edge case result: " + result9.getValue());

        // 10. Test for a deeply nested condition
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
        System.out.println("Expected deeply nested IF result: true"); // Expected: Inner IF evaluates to true, outer IF should return true
        CircuitValue result10 = solver.solve(c10);
        System.out.println("Deeply nested IF result: " + result10.getValue());
        solver.stop();
    }
}
