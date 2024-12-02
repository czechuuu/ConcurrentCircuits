package cp2024.maxtests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;
import static java.lang.Thread.sleep;

public class SimpleAndLazyTests {
    public static void main(String[] args) throws InterruptedException {
        ParallelCircuitSolver solver = new ParallelCircuitSolver();

        // Simple tests without lazy evaluation.
        testSimpleAND(solver);
        testSimpleOR(solver);
        testComplexCircuit(solver);
        testDeepTree(solver);
        testThresholdGT(solver);
        testThresholdLT(solver);
        testMixedThresholds(solver);
        testIFTrue(solver);
        testIFFalse(solver);

        solver.stop();
        System.out.println("All simple tests passed!");

        for(int i = 0;;++i){
            // Lazy evaluation tests.
            solver = new ParallelCircuitSolver();

            testLazyAND(solver);
            testLazyOR(solver);
            testLazyGT_1(solver);
            testLazyGT_2(solver);
            testLazyLT_1(solver);
            testLazyLT_2(solver);
            testLazyIF_1(solver);
            testLazyIF_2(solver);

            System.out.println("Waiting for assertions...");
            sleep(1000);

            solver.stop();
            System.out.println("All lazy evaluation tests passed!: " + i);
        }

    }

    /*

    SIMPLE TESTS

    */

    private static void testSimpleAND(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.AND, CircuitNode.mk(true), CircuitNode.mk(true))
        );
        assert solver.solve(circuit).getValue() : "AND Test failed.";
    }

    private static void testSimpleOR(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.OR, CircuitNode.mk(false), CircuitNode.mk(true))
        );
        assert solver.solve(circuit).getValue() : "OR Test failed.";
    }

    private static void testComplexCircuit(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)),
                        CircuitNode.mk(NodeType.GT, 1,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true))
                )
        );

        assert solver.solve(circuit).getValue() : "Complex Circuit Test failed.";
    }

    private static void testDeepTree(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(NodeType.AND,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false))),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false)),
                        CircuitNode.mk(true)
                )
        );

        assert solver.solve(circuit).getValue() : "Deep Tree Test failed.";
    }

    private static void testThresholdGT(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true))
        );

        assert solver.solve(circuit).getValue() : "Threshold GT Test failed.";
    }

    private static void testThresholdLT(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.LT, 3,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false))
        );

        assert solver.solve(circuit).getValue() : "Threshold LT Test failed.";
    }

    private static void testMixedThresholds(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(NodeType.GT, 2,
                                        CircuitNode.mk(true),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true)),
                                CircuitNode.mk(NodeType.LT, 3,
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(false),
                                        CircuitNode.mk(true))),
                        CircuitNode.mk(NodeType.NOT, CircuitNode.mk(false))
                )
        );

        assert solver.solve(circuit).getValue() : "Mixed Thresholds Test failed.";
    }

    private static void testIFTrue(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(false),
                                CircuitNode.mk(false)
                        )
                )
        );

        assert solver.solve(circuit).getValue() : "IF True Condition Test failed.";
    }

    private static void testIFFalse(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true)),
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false)))
        );

        assert !solver.solve(circuit).getValue() : "IF False Condition Test failed.";
    }

    /*

    LAZY TESTS

    */

    private static void testLazyAND(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true, Duration.ofMillis(1000)),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false, Duration.ofMillis(1000))
                )
        );
        long startTime = System.currentTimeMillis();

        assert !solver.solve(circuit).getValue() : "Lazy AND Test failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Lazy AND was not evaluated lazily.";
    }

    private static void testLazyOR(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(false, Duration.ofMillis(1000)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofMillis(1000))
                )
        );
        long startTime = System.currentTimeMillis();

        assert solver.solve(circuit).getValue() : "Lazy OR Test failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Lazy OR was not evaluated lazily.";
    }

    private static void testLazyGT_1(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true, Duration.ofMillis(1000)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofMillis(1000))
                )
        );

        long startTime = System.currentTimeMillis();
        assert solver.solve(circuit).getValue() : "GT Lazy Evaluation Test 1 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Threshold was not evaluated lazily.";
    }

    private static void testLazyGT_2(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true, Duration.ofMillis(1000)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false)
                )
        );

        long startTime = System.currentTimeMillis();
        assert !solver.solve(circuit).getValue() : "GT Lazy Evaluation Test 2 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Threshold was not evaluated lazily.";
    }

    private static void testLazyLT_1(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true, Duration.ofMillis(1000)),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(false)
                )
        );

        long startTime = System.currentTimeMillis();
        assert solver.solve(circuit).getValue() : "LT Lazy Evaluation Test 2 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Threshold was not evaluated lazily.";
    }

    private static void testLazyLT_2(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofMillis(1000))
                )
        );

        long startTime = System.currentTimeMillis();
        assert !solver.solve(circuit).getValue() : "LT Lazy Evaluation Test 1 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Threshold was not evaluated lazily.";
    }

    private static void testLazyIF_1(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofMillis(1000))
                )
        );

        long startTime = System.currentTimeMillis();
        assert solver.solve(circuit).getValue() : "Lazy IF Test 1 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Lazy IF Test 1 was not evaluated lazily.";
    }

    private static void testLazyIF_2(CircuitSolver solver) throws InterruptedException {
        Circuit circuit = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false, Duration.ofMillis(1000)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true)
                )
        ); 

        long startTime = System.currentTimeMillis();
        assert solver.solve(circuit).getValue() : "Lazy IF Test 2 failed.";
        assert System.currentTimeMillis() - startTime < 1000 : "Lazy IF Test 2 was not evaluated lazily.";
    }
}
