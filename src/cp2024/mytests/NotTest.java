package cp2024.mytests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

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
                CircuitNode.mk(NodeType.LT, 2,
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

    public static void testIF() throws InterruptedException {
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false
                        ),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),   // If true, return true
                        CircuitNode.mk(false)   // If false, return false
                )
        );
        System.out.println("Expected IF result (LT condition): false"); // Expected: GT should return true, so IF should return true
        CircuitValue result4 = solver.solve(c5);
        System.out.println("IF result (LT condition): " + result4.getValue());
    }

    public static void longUnneededGT() throws InterruptedException {
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(10))
                )
        );
        CircuitValue test6 = solver.solve(c6);
        System.out.println("Test 6 (GT threshold) expected false, got: " + test6.getValue());  // Expected: true
    }

    public static void combinedTest() throws InterruptedException {
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
    }

    public static void main(String[] args) throws InterruptedException {
        while(true){
            combinedTest();
            //longUnneededGT();
        }
    }
}
