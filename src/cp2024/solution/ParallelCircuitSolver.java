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
    private final int MAX_THREADS = 4;
    private final ExecutorService pool;

    public ParallelCircuitSolver() {
        this.acceptsComputations = true;
        pool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @Override
    public CircuitValue solve(Circuit c) {
        if (!acceptsComputations) {
            return new BrokenCircuitValue();
        }

        return new ParallelCircuitValue();
    }

    private Future<Boolean> createFutureToComputeCircuitNodeValue(CircuitNode cn) {
        return switch(cn.getType()){
            case LEAF ->
        }
    }

    @Override
    public void stop() {
        /*FIX ME*/
        throw new RuntimeException("Not implemented.");
    }
}
