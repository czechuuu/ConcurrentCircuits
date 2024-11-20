package cp2024.solution;

import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.LeafNode;
import cp2024.circuit.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// TODO if we get hit with an exception we should interrupt all further computations
// can we just shut down the pool to achieve this?

// the solver should ahve a thread pool for executing the computeValue calls
// operations on the same object from different threads seem sus

// what im writing seems somewhat brute force
// for AND OR LT AND GT wed rather have a homemadfuture-eque structure that we can await on for any result
// this way we dont get stuck on some computation that will never finish


public class ParallelCircuitValue implements CircuitValue {
    private final CircuitNode node;
    private final CircuitNode[] cachedNodeArgs;
    private Optional<Boolean> cachedValue;
    boolean isCancelled;
    private final ExecutorService pool;
    private static final int MAX_THREADS = 10;

    public ParallelCircuitValue(CircuitNode node) throws InterruptedException {
        this.node = node;
        this.cachedNodeArgs = node.getArgs(); // can throw interrupted exception
        this.cachedValue = Optional.empty();
        this.isCancelled = false;
        int nThreads = Math.min(cachedNodeArgs.length, MAX_THREADS);
        this.pool = Executors.newFixedThreadPool(nThreads);
    }

    /**
     * Stops the pool interrupting all running computations, and causes getValue to throw an InterruptedException
     */
    public void cancelForcefully() {
        isCancelled = true;
        pool.shutdownNow(); // sends interrupt to all running threads in the pool and doesn't start any new ones
    }

    /**
     * Stops the pool interrupting all running computations - gracefully but with force
     */
    private void endGracefully() {
        pool.shutdown();
    }

    @Override
    public boolean getValue() throws InterruptedException {
        // TODO wait until cached value is computed
        if (cachedValue.isPresent())
            return cachedValue.get();


    }

    // todo dont
    // can be changed to not have the try and return the broken circuit value
    // in the splver well catch it
    public void computeValue() {
        // we try to compute the result but if at any point we get interrupted we cancel all and will throw when prompted to get the value
        try {
            if (node.getType() == NodeType.LEAF)
                cacheValue(((LeafNode) node).getValue()); // can throw interrupted exception :)


            // TODO can be written more compactly
            List<ParallelCircuitValue> pvals = new ArrayList<>();
            for (CircuitNode cn : cachedNodeArgs) {
                ParallelCircuitValue pcv = new ParallelCircuitValue(cn);
                pvals.add(pcv);
            }

            List<Future<Boolean>> futureChildrenValues = new ArrayList<>();
            for (ParallelCircuitValue pcv : pvals) {
                Future<Boolean> future = pool.submit(pcv::getValue);
                futureChildrenValues.add(future);
            }


            checkForInterruption();


            throw new InterruptedException();

        } catch (InterruptedException _) {
            cancelForcefully();
        }

    }

    private boolean processValuesOfChildren(List<Future<Boolean>> futureChildrenValues) throws InterruptedException {
        return switch (node.getType()) {
            case AND -> processAND(futureChildrenValues);
        }
    }

    private boolean processAND(List<Future<Boolean>> futureChildrenValues) throws InterruptedException {

    }

    /**
     * This method processes children returning `result` if any of the children returns `value`, otherwise it returns `!value`
     *
     * @param futureChildrenValues list of futures representing children values
     * @param value                value to be checked for
     * @param result               value to be returned if any of the children returns `value`
     * @return `result` if any of the children returns `value`, otherwise `!value`
     * @throws InterruptedException if was cancelled, interrupted, or any of the children throws an exception
     */
    private boolean processSingleValueImpliesResult(List<Future<Boolean>> futureChildrenValues, boolean value, boolean result) throws InterruptedException {
        boolean ret = !result;
        for (Future<Boolean> future : futureChildrenValues) {
            try {
                if (future.get() == value) {
                    endGracefully(); // we forcefully end other computations
                    ret = result;
                }
            } catch (ExecutionException _) {
                throw new InterruptedException(); // if any of the children throws an exception, we throw an exception
            }
        }
        endGracefully(); // pool shutdown idempotent so we can call it multiple times
        return ret;
    }

    private void checkForInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
            cancelForcefully();
            throw new InterruptedException();
        }
    }

    /**
     * Caches the value and returns it
     *
     * @param value the value to be cached
     * @return the given value unchanged
     */
    private boolean cacheValue(boolean value) {
        cachedValue = Optional.of(value);
        return value;
    }
}
