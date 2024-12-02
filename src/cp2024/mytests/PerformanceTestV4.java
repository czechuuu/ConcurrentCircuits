package cp2024.demo;

import cp2024.circuit.*;
import cp2024.solution.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class PerformanceTestV4 {
    private static final double SLEEPY_CHANCE = 0.8; // Chance for a sleepy leaf node (performance heavy)
    private static final double TRUE_LEAF_CHANCE = 0.5; // Chance that a leaf node has value "true"
    private static final int MAX_DEPTH = 8; // Maximum depth of the circuit (performance heavy)
    private static final int MAX_DEPTH_VARIATION = 3; // Maximum variation in depth at any step
    private static final int MAX_SLEEP_MS = 100; // (performance heavy)
    private static final int MAX_ARGS = 16; // For AND, OR, GT, LT (performance heavy)
    private static final int NODE_COUNT_LIMIT = 100000; // Soft caps the number of nodes in the circuit

    private static int count; // Number of nodes in the circuit (valid after building the circuit)
    record Pair(CircuitValue circuitValue, long time) {} // Record for storing the result and time taken

    public static void main(String[] args) throws InterruptedException {

        int tests = 1000;
        
        List<Callable<Boolean>> sequentialTasks = new ArrayList<>();
        List<Boolean> parallelResults = new ArrayList<>();

        long parallelSumDuration = 0;

        // performance test
        for (int i = 0; i < tests; i++) {
            
            Circuit c = buildRandomCircuit();

            sequentialTasks.add(runSequential(c));

            CircuitSolver parallelSolver = new ParallelCircuitSolver();

            Instant startParallel = Instant.now();
            CircuitValue parallelResult = parallelSolver.solve(c);
            parallelResults.add(parallelResult.getValue());
            Instant endParallel = Instant.now();
            long parallelDuration = Duration.between(startParallel, endParallel).toMillis();

            System.out.println("Test " + (i+1) + " time taken by parallel solver:" + parallelDuration + " ms");
            parallelSolver.stop();
            parallelSumDuration += parallelDuration;
        }
        System.out.println("\nAverage time taken by parallel solver: " + parallelSumDuration/tests + " ms");
        System.out.println("Waiting for sequential results - be patient, it may take up to a few minutes!\n");
        
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Boolean>> sequentialResults = new ArrayList<>();
        for (Callable<Boolean> task : sequentialTasks) {
            sequentialResults.add(executor.submit(task));
        }

        // correctness test
        for (int i = 0; i < tests; i++) {
            try {
                Boolean sequentialResult = sequentialResults.get(i).get();
                Boolean parallelResult = parallelResults.get(i);
                if (sequentialResult != parallelResult) {
                    System.out.println("Test " + (i+1) + " failed");
                }
                else {
                    System.out.println("Test " + (i+1) + " passed");
                }
            } catch (ExecutionException e) {
                System.out.println("Test " + (i+1) + " failed");
            }
        }

        executor.shutdown();

        System.out.println("\nAverage time taken by parallel solver: " + parallelSumDuration/tests + " ms");
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

    private static Callable<Boolean> runSequential(Circuit c) {
        CircuitSolver sequentialSolver = new SequentialSolver();

        // Run sequential in another thread and get result and runtime
        Callable<Boolean> result = (() -> {
            CircuitValue circuitValue = sequentialSolver.solve(c);
            return circuitValue.getValue();
        });

        return result;
    }
}