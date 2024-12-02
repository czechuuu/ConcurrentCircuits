package cp2024.mytests;

import cp2024.circuit.*;
import cp2024.demo.BrokenCircuitValue;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

public class PerformanceTestV3 {
    private static final double SLEEPY_CHANCE = 0.8; // Chance for a sleepy leaf node (performance heavy)
    private static final double TRUE_LEAF_CHANCE = 0.5; // Chance that a leaf node has value "true"
    private static final int MAX_DEPTH = 8; // Maximum depth of the circuit (performance heavy)
    private static final int MAX_DEPTH_VARIATION = 3; // Maximum variation in depth at any step
    private static final int MAX_SLEEP_MS = 100; // (performance heavy)
    private static final int MAX_ARGS = 8; // For AND, OR, GT, LT (performance heavy)
    private static final int NODE_COUNT_LIMIT = 100000; // Soft caps the number of nodes in the circuit

    // Whether the sequential solver can be stopped to save time; useful for testing times but not for correctness
    private static final boolean SEQUENTIAL_STOPPABLE = true;

    // In seconds, after how long the sequential solver should be stopped
    private static final int SEQUENTIAL_TIMEOUT_S = 10;

    private static int count; // Number of nodes in the circuit (valid after building the circuit)
    record Pair(CircuitValue circuitValue, long time) {} // Record for storing the result and time taken

    public static void main(String[] args) throws InterruptedException {
        System.out.print("Generating...");
        Circuit c = buildRandomCircuit();

        System.out.println(" Generated " + count + " nodes.");
        if (count >= NODE_COUNT_LIMIT) {
            System.out.println("Generator has hit the node soft cap (" + NODE_COUNT_LIMIT + ").");
        }

        System.out.print("SequentialSolver starts...");
        if (SEQUENTIAL_STOPPABLE) {
            System.out.println(" (timeout is enabled)");
        } else {
            System.out.println(" (timeout is disabled)");
        }

        Pair sequentialResultPair = runSequential(c);

        if (sequentialResultPair.time == -1) {
            System.out.println("SequentialSolver took longer than " + SEQUENTIAL_TIMEOUT_S + "s and was stopped.");
        } else {
            System.out.println("Time taken by SequentialSolver: " + sequentialResultPair.time + "ms");
        }

        CircuitSolver parallelSolver = new ParallelCircuitSolver();

        System.out.println("ParallelSolver starts...");
        Instant startParallel = Instant.now();
        CircuitValue parallelResult = parallelSolver.solve(c);
        parallelResult.getValue();
        Instant endParallel = Instant.now();

        parallelSolver.stop();

        long parallelDuration = Duration.between(startParallel, endParallel).toMillis();

        System.out.println("Time taken by ParallelSolver: " + parallelDuration + "ms");

        // Compare the results
        if (sequentialResultPair.time == -1) {
            System.out.println("SequentialSolver was stopped, cannot compare results.");
        } else if (sequentialResultPair.circuitValue.getValue() == parallelResult.getValue()) {
            System.out.println("Both solvers returned the same result.");
        } else {
            System.out.println("The solvers returned different results.");
        }
    }

    private static Circuit buildRandomCircuit() {
        count = 0; // after building the circuit, the count will be the number of nodes
        return new Circuit(buildNodeTree(MAX_DEPTH));
    }

    // Recursively build a tree
    private static CircuitNode buildNodeTree(int depth) {
        count++;

        // Generate a leaf node
        if (depth == 0 || count >= NODE_COUNT_LIMIT) {
            Duration sleep = Duration.ofMillis(ThreadLocalRandom.current().nextInt(0, MAX_SLEEP_MS + 1));
            boolean leafValue = withChance(TRUE_LEAF_CHANCE);

            if (withChance(SLEEPY_CHANCE)) {
                //sleepy Node
                return CircuitNode.mk(leafValue, sleep);
            }
            //no sleep
            return CircuitNode.mk(leafValue);
        }

        int depthChange = ThreadLocalRandom.current().nextInt(1, MAX_DEPTH_VARIATION + 1);
        depth = Math.max(0, depth - depthChange);
        int maxArgs = ThreadLocalRandom.current().nextInt(2, MAX_ARGS + 1);
        int threshold = ThreadLocalRandom.current().nextInt(1, maxArgs + 1);

        return switch (ThreadLocalRandom.current().nextInt(0, 6)) {
            case 0 -> CircuitNode.mk(NodeType.GT, threshold, getArgs(2, maxArgs, depth));
            case 1 -> CircuitNode.mk(NodeType.LT, threshold, getArgs(2, maxArgs, depth));
            case 2 -> CircuitNode.mk(NodeType.AND, getArgs(2, maxArgs, depth));
            case 3 -> CircuitNode.mk(NodeType.OR, getArgs(2, maxArgs, depth));
            case 4 -> CircuitNode.mk(NodeType.NOT, getArgs(1, 1, depth));
            case 5 -> CircuitNode.mk(NodeType.IF, getArgs(3, 3, depth));
            default -> throw new RuntimeException("Something went terribly wrong.");
        };
    }

    private static boolean withChance(double chance) {
        assert 0 <= chance && chance <= 1;
        return Math.random() < chance;
    }

    private static CircuitNode[] getArgs(int minArgs, int maxArgs, int depth) { // minArgs and maxArgs are inclusive
        int numArgs = ThreadLocalRandom.current().nextInt(minArgs, maxArgs + 1);
        CircuitNode[] args = new CircuitNode[numArgs];
        for (int i = 0; i < numArgs; i++) {
            args[i] = buildNodeTree(depth);
        }
        return args;
    }

    private static Pair runSequential(Circuit c) {
        CircuitSolver sequentialSolver = new SequentialSolver();
        ExecutorService singleThread = Executors.newSingleThreadExecutor(); // Will run sequential in another thread

        // Run sequential in another thread and get result and runtime
        Future<Pair> result = singleThread.submit(() -> {
            Instant startSequential = Instant.now();
            CircuitValue circuitValue = sequentialSolver.solve(c);
            Instant endSequential = Instant.now();
            long sequentialDuration = Duration.between(startSequential, endSequential).toMillis();
            return new Pair(circuitValue, sequentialDuration);
        });

        // Stop the sequential solver if it takes too long (to save time)
        if (SEQUENTIAL_STOPPABLE) {
            // Busy wait for the result, checking if time exceeds the timeout
            Instant startSequential = Instant.now();
            while (!result.isDone()) {
                Instant endSequential = Instant.now();
                long sequentialDuration = Duration.between(startSequential, endSequential).toSeconds();
                if (sequentialDuration > SEQUENTIAL_TIMEOUT_S) {
                    result.cancel(true);
                    break;
                }
            }
        }

        // get the result if it wasn't cancelled
        Pair sequentialResultPair;
        if (!result.isCancelled()) {
            try {
                sequentialResultPair = result.get();
            } catch (Exception e) {
                throw new RuntimeException("Main Interrupted.", e);
            }
        } else {
            sequentialResultPair = new Pair(new BrokenCircuitValue(), -1);
        }

        sequentialSolver.stop();
        singleThread.shutdownNow();

        return sequentialResultPair;
    }
}