package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;

public class NotTest {
    private static final CircuitSolver solver = new ParallelCircuitSolver();

    // test a simple not operation
    public static void testNot() throws InterruptedException {
        Circuit notTrue = new Circuit(
                CircuitNode.mk(NodeType.NOT,
                        CircuitNode.mk(true)
                )
        );
        CircuitValue testNotTrue = solver.solve(notTrue);
        System.out.println("Test NOT true: " + testNotTrue.getValue() + " expected false");  // Expected: false


    }

    public static void testGT() throws InterruptedException {
        Circuit c1 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(false),  // First argument (true)
                        CircuitNode.mk(true),  // Second argument (true)
                        CircuitNode.mk(false), // Third argument (false)
                        CircuitNode.mk(true)   // Fourth argument (true)
                )
        );
        System.out.println("Expected GT result: true"); // Expected: More than 2 true values
        CircuitValue result1 = solver.solve(c1);
        System.out.println("GT result: " + result1.getValue());
    }

    public static void testNOTGT() throws InterruptedException {
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
    }

    public static void testIFGt() throws InterruptedException {
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
        System.out.println("Expected IF result (LT condition): true"); // Expected: GT should return true, so IF should return true
        CircuitValue result4 = solver.solve(c5);
        System.out.println("IF result (LT condition): " + result4.getValue());
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("skibidi");
        testIFGt();
    }
}
