package cp2024.solution;

import cp2024.circuit.*;

import java.util.*;
import java.util.concurrent.*;

public class ParallelCircuitValue implements CircuitValue {
    private final CircuitNode node;
    private boolean isCancelled;
    //TODO how do interrupts work if im reusing my thread to calculate the condition in the IF etc.

    /**
     * Used to await for the computation of the value, or cancellation of the computation.
     */
    private final CountDownLatch latch;

    /**
     * Uninitialized must not be read until the latch is broken.
     */
    private boolean value;

    /**
     * List of all the tasks for the children of the current node.
     */
    private final List<Future<Optional<Boolean>>> childrenTasks;

    /**
     * The pool on which all the child computations are executed.
     */
    private final ExecutorService pool;

    /**
     * The channel to the parent, where the value of the current node will be sent when computed.
     */
    private final BlockingQueue<Optional<Boolean>> channelToParent;

    /**
     * The channel to the children, from which the values of the children will be read.
     */
    private final BlockingQueue<Optional<Boolean>> channelToChildren;


    public ParallelCircuitValue(CircuitNode node, BlockingQueue<Optional<Boolean>> channelToParent, ExecutorService pool) {
        this.node = node;
        this.isCancelled = false;
        this.latch = new CountDownLatch(1);
        this.childrenTasks = Collections.synchronizedList(new ArrayList<>()); // is this good enough?
        this.pool = pool;
        this.channelToParent = channelToParent;
        this.channelToChildren = new LinkedBlockingQueue<>();
    }


    /**
     * Sets the status of the circuit value to cancelled.
     * All further or yet unfinished (i.e. ones that have hung on the latch awaiting the computation fo the value)
     * of <code>getValue()</code> will throw <code>InterruptedException</code>
     * Cancels all children computations and waits for them to finish.
     * Sends an empty optional to the parent, to signal that it was cancelled, so if the cancellation was unexpected,
     * it propagates upwards.
     * If the computation was already cancelled, does nothing.
     */
    private synchronized void cancel() { // TODO synchronized required?
        if (isCancelled) {
            return;
        }

        isCancelled = true;
        latch.countDown(); // to unlock all threads waiting for the value, that need to get an exception
        propagateCancelToChildren();
        signalCancellationToParent();
        // TODO maybe add an exit(0)?
    }

    /**
     * Propagates the cancel signal to all children of the current node, waiting for them to finish.
     */
    private void propagateCancelToChildren() {
        // cancel all children
        for (Future<?> task : childrenTasks) {
            task.cancel(true);
        }

        // and wait until they are finished
        for (Future<?> task : childrenTasks) {
            boolean finished = (task.isDone() || task.isCancelled()); // !
            while (!finished) {
                try {
                    task.get();
                    finished = true;
                } catch (InterruptedException | ExecutionException e) {
                    // ignore, because we need to wait for all children to finish
                } catch(CancellationException e) {
                    // i dont udnerstand this
                    finished = true;
                }
            }
        }
    }

    /**
     * Puts an empty optional into the channel to the parent, signalling that the computation was cancelled.
     * If the parent didn't expect the cancellation, it will propagate it upwards.
     * If the parent expected the cancellation (i.e. he called for it himself) it should ignore the signal.
     */
    private void signalCancellationToParent() {
        if (channelToParent == null) {
            return;
        }

        boolean sent = false;
        while (!sent) {
            try {
                channelToParent.put(Optional.empty());
                sent = true;
            } catch (InterruptedException e) {
                // ignore, try again - the parent must be able to receive the signal
            }
        }
    }

    /**
     * Tries to put the value into the channel to the parent.
     * If it doesn't succeed then cancels the computation and throws an exception.
     *
     * @param result the value to be sent to the parent
     * @throws InterruptedException if the value couldn't be sent to the parent
     */
    private void sendResultToParent(boolean result) throws InterruptedException {
        if (channelToParent == null) {
            return;
        }

        try {
            channelToParent.put(Optional.of(result));
        } catch (InterruptedException e) {
            cancel();
            throw e;
        }

    }

    /**
     * Tries to set the value of the circuit value, and send it to the parent.
     * If it fails then cancels the computation and throws an exception.
     *
     * @param value the value to be set as the value of the circuit
     * @throws InterruptedException if the value couldn't be sent to the parent
     */
    private void setValue(boolean value) throws InterruptedException {
        this.value = value;
        sendResultToParent(value); // this can fail and throw, but if it does it also cancels
        latch.countDown();
    }

    /**
     * Attempts to read the value of the circuit value.
     * Can throw if the computation was cancelled
     *
     * @return the computed value of the circuit
     * @throws InterruptedException if the computation was cancelled, or the thread was interrupted while waiting for the value
     */
    @Override
    public boolean getValue() throws InterruptedException {
        // this awaiting can throw!
        latch.await(); // wait until the computation is finished or cancelled

        if (isCancelled) {
            throw new InterruptedException();
        }
        return value;
    }

    // TODO make sure theres no leftover bugs
    public void computeValue() {
        try {
            if (node.getType() == NodeType.LEAF) {
                computeValueOfLeafNode();
            } else if (node.getType() == NodeType.NOT) {
                computeValueOfNotNode();
            } else {
                if (node.getType() == NodeType.IF) {
                    computeValueOfIfNode();
                } else {
                    computeValueOfMultipleChildNode();
                }
            }
        } catch (InterruptedException e) {
            cancel();
        }
    }

    /**
     * Tries to get the value of the leaf and set it as the value of the circuit.
     * If it fails then cancels the computation and throws an exception.
     *
     * @throws InterruptedException if getting the value of the leaf or sending it to the parent fails
     */
    private void computeValueOfLeafNode() throws InterruptedException {
        try {
            LeafNode leafNode = (LeafNode) node;
            boolean valueOfTheLeaf = leafNode.getValue(); // can fail getting the value of the leaf
            setValue(valueOfTheLeaf); // can fail sending the value to the parent
        } catch (InterruptedException e) {
            cancel();
            throw e;
        }
    }

    /**
     * Tries to get the value of the child of the NOT node and set it as the value of the circuit.
     *
     * @throws InterruptedException if the computation or setting the value fails
     */
    private void computeValueOfNotNode() throws InterruptedException {
        try {
            CircuitNode child = node.getArgs()[0];
            // ParallelCircuitValue valueOfChild = new ParallelCircuitValue(child, channelToChildren, pool);
            childrenTasks.add(pool.submit(poolTaskForGivenChild(child)));
            boolean valueOfTheChild = channelToChildren.take().orElseThrow(InterruptedException::new);
            setValue(!valueOfTheChild);
        } catch (InterruptedException e) {
            cancel();
            throw e;
        }
    }

    /**
     * Creates a lambda that computes the value of the given child and returns it as an optional.
     * The lambda should be submitted to the pool.
     *
     * @param child the child whose value is to be computed (concurrently)
     * @return the lambda to be run on the pool
     */
    private Callable<Optional<Boolean>> poolTaskForGivenChild(CircuitNode child) {
        ParallelCircuitValue valueOfChild = new ParallelCircuitValue(child, channelToChildren, pool);
        return () -> {
            try {
                valueOfChild.computeValue();
                return Optional.of(valueOfChild.getValue());
            } catch (InterruptedException e) {
                return Optional.empty();
            } catch (Exception e){
                return Optional.empty();
            }
        };
    }

    /**
     * Tries to compute the value of the IF node.
     *
     * @throws InterruptedException if the computation fails
     */
    private void computeValueOfIfNode() throws InterruptedException {
        try {
            CircuitNode[] args = node.getArgs(); // can throw
            final int conditionIndexInArgs = 0;
            final int ifTrueIndexInArgs = 1;
            final int ifFalseIndexInArgs = 2;

            for (int i = ifTrueIndexInArgs; i <= ifFalseIndexInArgs; i++) {
                CircuitNode child = args[i];
                Callable<Optional<Boolean>> task = poolTaskForGivenChild(child);
                Future<Optional<Boolean>> future = pool.submit(task);
                childrenTasks.add(future);
            }

            CircuitNode condition = args[conditionIndexInArgs];
//            ParallelCircuitValue valueOfCondition = new ParallelCircuitValue(condition, channelToChildren, pool);
//            // if we're interrupted while calculating the condition, we'll cancel the condition subtree
//            // then when we try to read the value of the condition, we'll get an InterruptedException
//            // and cancel the entire IF tree
//            valueOfCondition.computeValue();
            Future<Optional<Boolean>> conditionFuture = pool.submit(poolTaskForGivenChild(condition));
            boolean conditionValue = conditionFuture.get().orElseThrow(InterruptedException::new); // can throw

            checkForInterruption(); // if the computation of the condition was long, the result might not be needed anymore
            int resultIndex = conditionValue ? 0 : 1;
            // try to get the result of the child, or if it failed, cancel and throw
            boolean result = childrenTasks.get(resultIndex).get().orElseThrow(InterruptedException::new);
            setValue(result);
            propagateCancelToChildren(); // we don't need the child to compute a value that won't be read
        } catch (InterruptedException e) {
            cancel();
            throw e;
        } catch (ExecutionException e) {
            cancel();
            throw new IllegalStateException("The child task should not throw.", e);
        }
    }

    /**
     * Checks if the current thread was interrupted, and if so, cancels the computation and throws an exception.
     * Clears the interrupt status of the thread.
     *
     * @throws InterruptedException if the thread was interrupted
     */
    private void checkForInterruption() throws InterruptedException {
        if (Thread.interrupted()) {
            cancel();
            throw new InterruptedException();
        }
    }


    /**
     * Computes the value of the circuit node with multiple children.
     *
     * @throws InterruptedException if the computation was cancelled or interrupted
     */
    private void computeValueOfMultipleChildNode() throws InterruptedException {
        try {
            // submit all children computation tasks
            CircuitNode[] children = node.getArgs();
            int numberOfChildren = children.length;
            for (CircuitNode child : children) {
                checkForInterruption();
                Callable<Optional<Boolean>> task = poolTaskForGivenChild(child);
                Future<Optional<Boolean>> future = pool.submit(task);
                childrenTasks.add(future);
            }

            switch (node.getType()) {
                case AND -> processAND(numberOfChildren);
                case OR -> processOR(numberOfChildren);
                case GT -> processGT(numberOfChildren);
                case LT -> processLT(numberOfChildren);
                default -> throw new IllegalStateException("Unexpected value: " + node.getType());

            }
            propagateCancelToChildren();
        } catch (InterruptedException e) {
            cancel();
            throw e;
        }
    }

    private void processAND(int N) throws InterruptedException {
        computeValueWhereSingleChildValueImpliesTheResult(false, false, N);

    }

    private void processOR(int N) throws InterruptedException {
        computeValueWhereSingleChildValueImpliesTheResult(true, true, N);
    }

    private void processGT(int N) throws InterruptedException {
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        computeValueWhereGreaterThanAmountOf(x, true, N);
    }

    private void processLT(int N) throws InterruptedException {
        ThresholdNode tnode = (ThresholdNode) node;
        int x = tnode.getThreshold();
        computeValueWhereGreaterThanAmountOf(x, false, N);
    }


    /**
     * Processes the values of the children looking for <code>value</code> and set the value of the circuit
     * to <code>result</code> if found at least one <code>value</code>.
     *
     * @param value  the value to be found
     * @param result the result to be set if the value is found
     * @throws InterruptedException if was cancelled or interrupted
     */
    private void computeValueWhereSingleChildValueImpliesTheResult(boolean value, boolean result, int N) throws InterruptedException {
        int receivedChildValues = 0;
        boolean foundValue = false;

        while (receivedChildValues < N && !foundValue) {
            checkForInterruption();
            boolean valueOfChild = channelToChildren.take().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            foundValue = valueOfChild == value;
        }

        // can be simplified but I find this more readable
        //noinspection SimplifiableConditionalExpression
        setValue(foundValue ? result : !result);
    }

    /**
     * Processes the values of the children looking for <code>true</code> and returns <code>true</code> iff found ">" <code>number</code>.
     *
     * @param number the highest number of occurrences of <code>value</code> that is still considered a failure
     * @throws InterruptedException if was cancelled or interrupted
     */
    private void computeValueWhereGreaterThanAmountOf(int number, boolean greaterThan, int N) throws InterruptedException {
        int receivedChildValues = 0;
        int foundValues = 0;

        while (receivedChildValues < N && (greaterThan ? foundValues <= number : foundValues < number)) {
            checkForInterruption();
            boolean valueOfChild = channelToChildren.take().orElseThrow(InterruptedException::new);
            receivedChildValues++;
            if (valueOfChild) {
                foundValues++;
            }
        }

        // If we're here then either
        // 1. we've found more than number of values so if we wanted to find more than number of values, we return true
        //    otherwise we return false
        // 2. we've found less than or equal to number of values but have looked through all children
        //    so we return whether the found number fits the order we've wanted
        setValue(greaterThan ? foundValues > number : foundValues < number);
    }


}
