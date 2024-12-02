package cp2024.mytests;
import cp2024.circuit.*;

import cp2024.solution.ParallelCircuitSolver;
import cp2024.demo.SequentialSolver;

import java.time.Duration;
import java.time.Instant;

public class SolverPerformanceTest {

    public static void main(String[] args) throws InterruptedException {
        // Define the size of the circuit for testing
        int numNodes = 10; // Large number to simulate a complex circuit (can be adjusted)

        // Construct a circuit using AND, OR, and GT nodes
        Circuit c = generateComplexCircuit(numNodes);
        printCircuitTree(c.getRoot(), 0);

        // Sequential solver test
        CircuitSolver sequentialSolver = new SequentialSolver();
        Instant startSequential = Instant.now();
        CircuitValue sequentialResult = sequentialSolver.solve(c);
        Instant endSequential = Instant.now();
        long sequentialDuration = Duration.between(startSequential, endSequential).toMillis();

        // Parallel solver test
        CircuitSolver parallelSolver = new ParallelCircuitSolver();
        Instant startParallel = Instant.now();
        CircuitValue parallelResult = parallelSolver.solve(c);
        boolean parallelResultValue = parallelResult.getValue();
        Instant endParallel = Instant.now();
        long parallelDuration = Duration.between(startParallel, endParallel).toMillis();

        // Print the results
        System.out.println("Sequential solver result: " + sequentialResult.getValue());
        System.out.println("Parallel solver result: " + parallelResultValue);
        System.out.println("Time taken by SequentialSolver: " + sequentialDuration + " ms");
        System.out.println("Time taken by ParallelSolver: " + parallelDuration + " ms");

        // Compare the results
        if (sequentialResult.getValue() == parallelResult.getValue()) {
            System.out.println("Both solvers returned the same result.");
        } else {
            System.out.println("The solvers returned different results.");
        }
        parallelSolver.stop();
    }

    // Generate a complex circuit to test the solvers' performance
    private static Circuit generateComplexCircuit(int numNodes) {
        // Build a chain of logical operations for testing, for example:
        // AND(OR(AND(...))) to simulate a large number of operations

        CircuitNode root = buildNodeChain(numNodes);

        return new Circuit(root);
    }

    // Recursively build a chain of AND and OR nodes
    private static CircuitNode buildNodeChain(int depth) {
        if (depth <= 0) {
            // Leaf node with random value (true or false)
            return CircuitNode.mk(Math.random() > 0.5);
        }

        // Randomly choose AND or OR for the current level
        CircuitNode left = buildNodeChain(depth - 1);
        CircuitNode right = buildNodeChain(depth - 1);

        // Randomly choose AND or OR
        if (Math.random() > 0.5) {
            return CircuitNode.mk(NodeType.AND, left, right);
        } else {
            return CircuitNode.mk(NodeType.OR, left, right);
        }
    }
    // Recursive method to print the circuit tree structure
    private static void printCircuitTree(CircuitNode node, int indent) throws InterruptedException {
        if (node == null) {
            return;
        }

        // Indentation for tree levels
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        // Print node details
        if (node.getType() == NodeType.LEAF) {
            System.out.println("Leaf: " + ((LeafNode) node).getValue());
        } else {
            System.out.println(node.getType());
        }

        // Print child nodes recursively
        for (CircuitNode child : node.getArgs()) {
            printCircuitTree(child, indent + 1);
        }
    }
}