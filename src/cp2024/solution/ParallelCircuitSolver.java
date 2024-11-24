package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.Circuit;
import cp2024.demo.BrokenCircuitValue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelCircuitSolver implements CircuitSolver {
    private boolean acceptsComputations;
    private final ExecutorService pool;

    public ParallelCircuitSolver() {
        this.acceptsComputations = true;
        pool = Executors.newWorkStealingPool();
    }

    @Override
    public CircuitValue solve(Circuit c) {
        if (!acceptsComputations) {
            return new BrokenCircuitValue();
        }

        ParallelCircuitValue result = new ParallelCircuitValue(c.getRoot());
        pool.submit(result::computeValue);
        return result;
    }


    @Override
    public void stop() {
        acceptsComputations = false;
        pool.shutdownNow();
    }
}
