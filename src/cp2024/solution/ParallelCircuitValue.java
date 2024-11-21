package cp2024.solution;

import cp2024.circuit.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

// we can throw an interrupted exception in the constructor
// we should catch it and return the broken circuit value (in the solver)

// whenever we get interrupted we should call cancel -> maybe idempotent since can be interrupted multiple times



public class ParallelCircuitValue implements CircuitValue {
    private final CircuitNode node;
    private final CircuitNode[] cachedNodeArgs;
    private BlockingQueue<Boolean> cachedValue;
    boolean isCancelled;
    private final GreedyThreadPool pool;
    private final List<Thread> threadsWaitingForValue;

    public ParallelCircuitValue(CircuitNode node) throws InterruptedException {
        this.node = node;
        this.cachedNodeArgs = node.getArgs(); // can throw interrupted exception
        this.cachedValue = new LinkedBlockingQueue<>();
        this.isCancelled = false;
        this.pool = new GreedyThreadPool();
        this.threadsWaitingForValue = new ArrayList<>();
    }

    private void cancel() {
        assert !isCancelled : "Can't cancel twice"; // TODO make it idempotent instead?

        isCancelled = true;
        pool.stop();
        interruptThreadsWaitingForValue(); // not children threads so we need not wait for them to finish
    }

    private void interruptThreadsWaitingForValue() {
        for (Thread t : threadsWaitingForValue) {
            t.interrupt();
        }
    }

    @Override
    public boolean getValue() throws InterruptedException {
        if (isCancelled)
            throw new InterruptedException();

        threadsWaitingForValue.add(Thread.currentThread());
        boolean value = takeCachedValue(); // can throw interrupted exception caused by cancel
        threadsWaitingForValue.remove(Thread.currentThread());
        return value;
    }

    /**
     * Wrapper for getValue that instead of throwing an exception returns an empty optional
     * @return the value of the circuit node or an empty optional if the computation failed
     */
    public Optional<Boolean> exceptionSafeGetValue(){
        try {
            return Optional.of(getValue());
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    // todo dont
    // can be changed to not have the try and return the broken circuit value
    // in the solver well catch it
    public void computeValue() {
        if (node.getType() == NodeType.LEAF) {
            try {
                processValueOfLeafNode();
            } catch (InterruptedException e) {
                cancel();
            }
        } else {
            try {
                processValuesOfChildren();
            } catch (InterruptedException e) {
                cancel();
            }
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
        // TODO does this opt make senses
        if(x < cachedNodeArgs.length - x){
            return processRelationNumberOfValue(x, true, Comparator.naturalOrder());
        } else{
            return processRelationNumberOfValue(cachedNodeArgs.length - x, false, Comparator.reverseOrder());
        }
    }


    /**
     * Processes the values of the children looking for <code>value</code> and returns <code>result</code> iff found.
     * @param value the value to be found
     * @param result the result to be returned if the value is found
     * @return <code>result</code> if <code>value</code> is found, <code>!result</code> otherwise
     * @throws InterruptedException if was cancelled or interrupted
     */
    private boolean processSingleValueImpliesResult(boolean value, boolean result) throws InterruptedException {
        int receivedChildValues = 0;
        boolean foundValue = false;
        while(receivedChildValues < cachedNodeArgs.length && !foundValue){
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
     * @param number the highest number of occurrences of <code>value</code> that is still considered a failure
     * @param value the value to be found
     * @param order the order of the comparison - if it's natural order then the result is true if the number of occurrences is greater than <code>number</code>
     * @return <code>true</code> if the number of occurrences of <code>value</code> is "greater" than <code>number</code>, <code>false</code> otherwise
     * @throws InterruptedException if was cancelled or interrupted
     */
    private boolean processRelationNumberOfValue(int number, boolean value, Comparator<Integer> order) throws InterruptedException {
        int receivedChildValues = 0;
        int foundValues = 0;
        while(receivedChildValues < cachedNodeArgs.length && order.compare(foundValues, number) <= 0){
            checkForInterruption();
            boolean valueOfChild = pool.getResult().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            foundValues += valueOfChild == value ? 1 : 0;
        }

        return order.compare(foundValues, number) > 0;
    }

    private void checkForInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
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
        // BlockingQueue does not provide a blocking method for peeking at an element
        boolean value = cachedValue.take();
        cachedValue.put(value);
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
}
