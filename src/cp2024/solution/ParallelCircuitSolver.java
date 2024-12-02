package cp2024.solution;


import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.Circuit;
import cp2024.demo.BrokenCircuitValue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelCircuitSolver implements CircuitSolver {
    private boolean acceptsComputations;
    private final ExecutorService pool;

    public ParallelCircuitSolver() {
        this.acceptsComputations = true;
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public synchronized CircuitValue solve(Circuit c) {
        if (!acceptsComputations) {
            return new BrokenCircuitValue();
        }

        ParallelCircuitValue result = new ParallelCircuitValue(c.getRoot(), null, pool);
        pool.submit(result::computeValue);
        return result;
    }

    @Override
    public synchronized void stop() {
        acceptsComputations = false;
        pool.shutdownNow();
    }
}
