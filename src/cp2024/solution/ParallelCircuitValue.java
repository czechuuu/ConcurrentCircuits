package cp2024.solution;

import cp2024.circuit.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ParallelCircuitValue implements CircuitValue {
    private static final Logger logger = Logger.getLogger(ParallelCircuitValue.class.getName());

    private final CircuitNode node;
    private CircuitNode[] cachedNodeArgs;
    private final BlockingQueue<Boolean> cachedValue;
    private volatile boolean isCancelled;
    private final GreedyThreadPool pool;
    private ExecutorService traditionalPool;
    private final List<Thread> threadsWaitingForValue;

    public ParallelCircuitValue(CircuitNode node) {
        this.node = node;
        this.cachedValue = new LinkedBlockingQueue<>();
        this.isCancelled = false;
        this.pool = new GreedyThreadPool();
        this.threadsWaitingForValue = new ArrayList<>();
        logger.info(() -> "Initialized ParallelCircuitValue for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
    }

    private void cacheChildren() throws InterruptedException {
        logger.info(() -> "Caching children of node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        this.cachedNodeArgs = node.getArgs();
    }

    private void cancel() {
        if (isCancelled) {
            logger.warning(() -> "Attempt to cancel already cancelled node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            return;
        }
        isCancelled = true;
        logger.info(() -> "Cancelling computation for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        endCalculations();
        interruptThreadsWaitingForValue();
    }

    private void endCalculations() {
        logger.info(() -> "Ending calculations for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        pool.stop();
        if (traditionalPool != null) {
            traditionalPool.shutdownNow();
            boolean terminated = false;
            while (!terminated) {
                try {
                    terminated = traditionalPool.awaitTermination(1, TimeUnit.SECONDS);
                    logger.info(() -> "Waiting for traditionalPool to terminate in thread " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    logger.warning(() -> "Thread interrupted while waiting for traditionalPool shutdown in thread " + Thread.currentThread().getName());
                    traditionalPool.shutdownNow();
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                }
            }
        }
    }

    private void interruptThreadsWaitingForValue() {
        logger.info(() -> "Interrupting threads waiting for value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        synchronized (threadsWaitingForValue) {
            for (Thread t : threadsWaitingForValue) {
                logger.info(() -> "Interrupting thread " + t.getName());
                t.interrupt();
            }
        }
    }

    @Override
    public boolean getValue() throws InterruptedException {
        if (isCancelled) {
            logger.warning(() -> "Attempt to get value from cancelled node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            throw new InterruptedException();
        }

        synchronized (threadsWaitingForValue) {
            threadsWaitingForValue.add(Thread.currentThread());
        }
        logger.info(() -> "Getting value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());

        boolean value = takeCachedValue();
        synchronized (threadsWaitingForValue) {
            threadsWaitingForValue.remove(Thread.currentThread());
        }

        logger.info(() -> "Returning computed value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        return value;
    }

    public Optional<Boolean> exceptionSafeGetValue() {
        try {
            logger.info(() -> "Safely getting value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            return Optional.of(getValue());
        } catch (InterruptedException e) {
            logger.warning(() -> "Exception during safe value retrieval for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            return Optional.empty();
        }
    }

    public void computeValue() {
        try {
            logger.info(() -> "Starting computation for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            if (node.getType() == NodeType.LEAF) {
                processValueOfLeafNode();
            } else if (node.getType() == NodeType.NOT) {
                cacheChildren();
                processNOTNode();
            } else {
                cacheChildren();
                if (node.getType() == NodeType.IF) {
                    processIFNode();
                } else {
                    processValuesOfChildren();
                }
                endCalculations();
            }
            logger.info(() -> "Finished computation for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            logger.warning(() -> "Computation interrupted for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            cancel();
        }
    }

    private void processValueOfLeafNode() throws InterruptedException {
        LeafNode leaf = (LeafNode) node;
        putCachedValue(leaf.getValue());
    }

    private void processValuesOfChildren() throws InterruptedException {
        for (CircuitNode child : cachedNodeArgs) {
            checkForInterruption(); // can throw

            ParallelCircuitValue valueOfChild = new ParallelCircuitValue(child); // can throw
            Callable<Optional<Boolean>> task = () -> {
                valueOfChild.computeValue();
                return valueOfChild.exceptionSafeGetValue();
            };
            pool.submit(task);
        }

        boolean nodeValue = switch (node.getType()) {
            case AND -> processAND();
            case OR -> processOR();
            case GT -> processGT();
            case LT -> processLT();
            default -> throw new IllegalStateException("Unexpected value: " + node.getType());
        };

        putCachedValue(nodeValue);
    }

    private boolean processAND() throws InterruptedException {
        // if any child is false then the result is false
        return processSingleValueImpliesResult(false, false);
    }

    private boolean processOR() throws InterruptedException {
        // if any child is true then the result is true
        return processSingleValueImpliesResult(true, true);
    }

    private boolean processGT() throws InterruptedException {
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        return processRelationNumberOfValue(x, true, Comparator.naturalOrder());
    }

    // TODO threshold logic
    private boolean processLT() throws InterruptedException {
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        return processRelationNumberOfValue(cachedNodeArgs.length - x, false, Comparator.naturalOrder());
    }

    private void processNOTNode() throws InterruptedException {
        ParallelCircuitValue valueOfChild = new ParallelCircuitValue(cachedNodeArgs[0]);
        valueOfChild.computeValue();
        putCachedValue(!valueOfChild.exceptionSafeGetValue().orElseThrow(InterruptedException::new));
    }

    private void processIFNode() throws InterruptedException {
        final int TRUE_CHILD = 0;
        final int FALSE_CHILD = 1;
        List<Future<Optional<Boolean>>> futures = new ArrayList<>();
        this.traditionalPool = Executors.newFixedThreadPool(2);

        for (int i = 1; i <= 2; i++) {
            ParallelCircuitValue valueOfChild = new ParallelCircuitValue(cachedNodeArgs[i]);
            Callable<Optional<Boolean>> task = () -> {
                valueOfChild.computeValue();
                return valueOfChild.exceptionSafeGetValue();
            };
            futures.add(traditionalPool.submit(task));
        }

        ParallelCircuitValue conditionValue = new ParallelCircuitValue(cachedNodeArgs[0]);
        conditionValue.computeValue();
        checkForInterruption();
        boolean condition = conditionValue.getValue();
        try {
            putCachedValue(futures.get(condition ? TRUE_CHILD : FALSE_CHILD).get().orElseThrow(InterruptedException::new));
        } catch (Exception _) {
            throw new InterruptedException();
        }
    }


    /**
     * Processes the values of the children looking for <code>value</code> and returns <code>result</code> iff found.
     *
     * @param value  the value to be found
     * @param result the result to be returned if the value is found
     * @return <code>result</code> if <code>value</code> is found, <code>!result</code> otherwise
     * @throws InterruptedException if was cancelled or interrupted
     */
    private boolean processSingleValueImpliesResult(boolean value, boolean result) throws InterruptedException {
        int receivedChildValues = 0;
        boolean foundValue = false;
        while (receivedChildValues < cachedNodeArgs.length && !foundValue) {
            checkForInterruption();
            boolean valueOfChild = pool.getResult().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            foundValue = valueOfChild == value;
        }

        // can be simplified to return foundValue == result, but it's less readable
        //noinspection SimplifiableConditionalExpression
        return foundValue ? result : !result;

    }

    /**
     * Processes the values of the children looking for <code>value</code> and returns <code>true</code> iff found ">" <code>number</code>
     *
     * @param number the highest number of occurrences of <code>value</code> that is still considered a failure
     * @param value  the value to be found
     * @param order  the order of the comparison - if it's natural order then the result is true if the number of occurrences is greater than <code>number</code>
     * @return <code>true</code> if the number of occurrences of <code>value</code> is "greater" than <code>number</code>, <code>false</code> otherwise
     * @throws InterruptedException if was cancelled or interrupted
     */
    private boolean processRelationNumberOfValue(int number, boolean value, Comparator<Integer> order) throws InterruptedException {
        int receivedChildValues = 0;
        int foundValues = 0;
        while (receivedChildValues < cachedNodeArgs.length && order.compare(foundValues, number) <= 0) {
            checkForInterruption();
            boolean valueOfChild = pool.getResult().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            foundValues += valueOfChild == value ? 1 : 0;
        }

        return order.compare(foundValues, number) > 0;
    }

    private void checkForInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
            logger.warning(() -> "Thread interrupted during computation of node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            cancel();
            throw new InterruptedException();
        }
    }

    /**
     * If cached value is present just returns it,
     * otherwise waits for the cached value to be computed and returns it.
     *
     * @return the cached value
     * @throws InterruptedException if was cancelled or interrupted
     */
    private Boolean takeCachedValue() throws InterruptedException {
        logger.info(() -> "Waiting to take cached value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        boolean value = cachedValue.take();
        cachedValue.put(value);
        logger.info(() -> "Retrieved cached value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        return value;
    }

    /**
     * Puts the given value into the cached value
     *
     * @param value to be set as the cached value of the circuit node;
     * @throws InterruptedException if was cancelled or interrupted
     */
    private void putCachedValue(boolean value) throws InterruptedException {
        assert cachedValue.isEmpty();
        cachedValue.put(value);
    }

    // TODO for debugging
    private String noderepr(CircuitNode node) {
        try{
            return node.getType().toString() + "; arity: " + node.getArgs().length;
        } catch (Exception e){
            return "node repr error";
        }
    }
}
