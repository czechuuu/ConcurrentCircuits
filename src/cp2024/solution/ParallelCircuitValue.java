package cp2024.solution;

import cp2024.circuit.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelCircuitValue implements CircuitValue {
    private static final Logger logger = Logger.getLogger(ParallelCircuitValue.class.getName());
    static{
        logger.setLevel(Level.OFF);
    }

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
        this.threadsWaitingForValue = Collections.synchronizedList(new ArrayList<>());
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

        for (Thread t : threadsWaitingForValue) {
            logger.info(() -> "Interrupting thread " + t.getName());
            t.interrupt();
        }

    }

    @Override
    public boolean getValue() throws InterruptedException {
        if (isCancelled) {
            logger.warning(() -> "Attempt to get value from cancelled node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            throw new InterruptedException();
        }


        threadsWaitingForValue.add(Thread.currentThread());

        logger.info(() -> "Getting value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        if (cachedValue.isEmpty()) {
            logger.info(() -> "Going to wait for value of: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        }

        boolean value = takeCachedValue();

        threadsWaitingForValue.remove(Thread.currentThread());


        logger.info(() -> "Returning computed value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        return value;
    }

    public Optional<Boolean> exceptionSafeGetValue() {
        try {
            logger.info(() -> "Safely getting value for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            return Optional.of(getValue());
        } catch (InterruptedException e) {
            logger.warning(() -> "Exception during safe value retrieval for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            e.printStackTrace();
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
        logger.info(() -> "Processing leaf node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        try {
            LeafNode leaf = (LeafNode) node;
            boolean value = leaf.getValue();
            putCachedValue(value);
            logger.info(() -> "Processed leaf value: " + value + " in thread " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            logger.warning(() -> "Interrupted while processing leaf node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            throw e;
        }
    }

    private void processValuesOfChildren() throws InterruptedException {
        logger.info(() -> "Processing children of node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        try {
            for (CircuitNode child : cachedNodeArgs) {
                checkForInterruption(); // can throw
                logger.info(() -> "Submitting task for child: " + noderepr(child) + " in thread " + Thread.currentThread().getName());

                ParallelCircuitValue valueOfChild = new ParallelCircuitValue(child);
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
                default -> {
                    logger.severe(() -> "Unexpected node type: " + node.getType() + " in thread " + Thread.currentThread().getName());
                    throw new IllegalStateException("Unexpected value: " + node.getType());
                }
            };

            putCachedValue(nodeValue);
            logger.info(() -> "Processed children for node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            logger.warning(() -> "Interrupted while processing children of node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            Thread.currentThread().interrupt(); // Preserve interrupt status
            throw e;
        }
    }

    private boolean processAND() throws InterruptedException {
        logger.info(() -> "Processing AND node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        boolean result = processSingleValueImpliesResult(false, false);
        logger.info(() -> "Result of AND node: " + result + " in thread " + Thread.currentThread().getName());
        return result;
    }

    private boolean processOR() throws InterruptedException {
        logger.info(() -> "Processing OR node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        boolean result = processSingleValueImpliesResult(true, true);
        logger.info(() -> "Result of OR node: " + result + " in thread " + Thread.currentThread().getName());
        return result;
    }

    private boolean processGT() throws InterruptedException {
        logger.info(() -> "Processing GT node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        boolean result = processRelationNumberOfValue(x, true, Comparator.naturalOrder());
        logger.info(() -> "Result of GT node: " + result + " in thread " + Thread.currentThread().getName());
        return result;
    }

    private boolean processLT() throws InterruptedException {
        logger.info(() -> "Processing LT node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        boolean result = processRelationNumberOfValue(cachedNodeArgs.length - x, false, Comparator.naturalOrder());
        logger.info(() -> "Result of LT node: " + result + " in thread " + Thread.currentThread().getName());
        return result;
    }

    private void processNOTNode() throws InterruptedException {
        logger.info(() -> "Processing NOT node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        try {
            ParallelCircuitValue valueOfChild = new ParallelCircuitValue(cachedNodeArgs[0]);
            valueOfChild.computeValue();
            boolean result = !valueOfChild.exceptionSafeGetValue().orElseThrow(InterruptedException::new);
            putCachedValue(result);
            logger.info(() -> "Result of NOT node: " + result + " in thread " + Thread.currentThread().getName());
        } catch (InterruptedException e) {
            logger.warning(() -> "Interrupted while processing NOT node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            throw e;
        }
    }

    private void processIFNode() throws InterruptedException {
        logger.info(() -> "Processing IF node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
        final int TRUE_CHILD = 0;
        final int FALSE_CHILD = 1;
        List<Future<Optional<Boolean>>> futures = new ArrayList<>();
        this.traditionalPool = Executors.newFixedThreadPool(2);

        try {
            for (int i = 1; i <= 2; i++) {
                ParallelCircuitValue valueOfChild = new ParallelCircuitValue(cachedNodeArgs[i]);
                int finalI = i;
                logger.info(() -> "Submitting IF node branch " + finalI + ": " + noderepr(cachedNodeArgs[finalI]) + " in thread " + Thread.currentThread().getName());
                Callable<Optional<Boolean>> task = () -> {
                    valueOfChild.computeValue();
                    return valueOfChild.exceptionSafeGetValue();
                };
                futures.add(traditionalPool.submit(task));
            }

            ParallelCircuitValue conditionValue = new ParallelCircuitValue(cachedNodeArgs[0]);
            conditionValue.computeValue();
            boolean condition = conditionValue.getValue();
            logger.info(() -> "Condition value of IF node: " + condition + " in thread " + Thread.currentThread().getName());

            putCachedValue(futures.get(condition ? TRUE_CHILD : FALSE_CHILD).get().orElseThrow(InterruptedException::new));
            logger.info(() -> "IF node result: " + condition + " in thread " + Thread.currentThread().getName());
        } catch (Exception e) {
            logger.warning(() -> "Exception in processing IF node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            throw new InterruptedException();
        } finally {
            traditionalPool.shutdown();
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
        logger.info(() -> "Starting processSingleValueImpliesResult for node: " + noderepr(node) +
                ", looking for value: " + value + " in thread " + Thread.currentThread().getName());

        int receivedChildValues = 0;
        boolean foundValue = false;

        while (receivedChildValues < cachedNodeArgs.length && !foundValue) {
            checkForInterruption();
            boolean valueOfChild = pool.getResult().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            foundValue = valueOfChild == value;

            boolean finalFoundValue = foundValue;
            int finalReceivedChildValues = receivedChildValues;
            logger.info(() -> "Processed child " + finalReceivedChildValues + ", value: " + valueOfChild +
                    ", foundValue: " + finalFoundValue + " in thread " + Thread.currentThread().getName());
        }

        boolean finalResult = foundValue ? result : !result;
        logger.info(() -> "Completed processSingleValueImpliesResult for node: " + noderepr(node) +
                ", result: " + finalResult + " in thread " + Thread.currentThread().getName());
        return finalResult;
    }

    /**
     * Processes the values of the children looking for <code>value</code> and returns <code>true</code> iff found ">" <code>number</code>.
     *
     * @param number the highest number of occurrences of <code>value</code> that is still considered a failure
     * @param value  the value to be found
     * @param order  the order of the comparison - if it's natural order then the result is true if the number of occurrences is greater than <code>number</code>
     * @return <code>true</code> if the number of occurrences of <code>value</code> is "greater" than <code>number</code>, <code>false</code> otherwise
     * @throws InterruptedException if was cancelled or interrupted
     */
    private boolean processRelationNumberOfValue(int number, boolean value, Comparator<Integer> order) throws InterruptedException {
        logger.info(() -> "Starting processRelationNumberOfValue for node: " + noderepr(node) +
                ", looking for occurrences of value: " + value + ", threshold: " + number +
                " in thread " + Thread.currentThread().getName());

        int receivedChildValues = 0;
        int foundValues = 0;

        while (receivedChildValues < cachedNodeArgs.length && order.compare(foundValues, number) <= 0) {
            checkForInterruption();
            boolean valueOfChild = pool.getResult().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            if (valueOfChild == value) {
                foundValues++;
            }
            int finalReceivedChildValues = receivedChildValues;
            int finalFoundValues = foundValues;
            logger.info(() -> "Processed child " + finalReceivedChildValues + ", value: " + valueOfChild +
                    ", found occurrences: " + finalFoundValues + " in thread " + Thread.currentThread().getName());
        }

        boolean finalResult = order.compare(foundValues, number) > 0;
        logger.info(() -> "Completed processRelationNumberOfValue for node: " + noderepr(node) +
                ", final result: " + finalResult + " in thread " + Thread.currentThread().getName());
        return finalResult;
    }

    private void checkForInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
            logger.warning(() -> "Thread interrupted during computation of node: " + noderepr(node) + " in thread " + Thread.currentThread().getName());
            Thread.currentThread().interrupt(); // Preserve interrupt status
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
        try {
            // Check if the node is a Leaf and print its value
            if (node.getType() == NodeType.LEAF) {
                return "(" + ((LeafNode) node).getValue() + ")";
            }

            StringBuilder result = new StringBuilder();
            result.append(node.getType().toString());

            // Get child nodes (if any) and process them recursively
            CircuitNode[] children = node.getArgs();
            if (children.length > 0) {
                result.append(" [");
                for (int i = 0; i < children.length; i++) {
                    result.append(noderepr(children[i]));  // Recursively represent each child
                    if (i < children.length - 1) {
                        result.append(", ");  // Add comma between children
                    }
                }
                result.append("]");
            }

            return result.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupt status
            return "node repr error";
        }
    }

}
