package cp2024.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * A thread pool that that creates as many threads as there are tasks given to it.
 * Has a blocking queue that stores the results of the tasks with no identification as to which task has given what result.
 * If any error occurs in any of the threads, all other threads are stopped and all further calls to getResult() will return empty.
 * If the pool is stopped then all threads are stopped and all further calls to getResult() will return empty.
 * Free of interrupted exceptions and interrupt checks, thread using it must check for them and explicitly stop the pool.
 */
public class GreedyThreadPool {
    private final List<Thread> threads;
    private final BlockingQueue<Optional<Boolean>> channel;
    private boolean errorOccurred;

    /**
     * Creates a new GreedyThreadPool.
     */
    public GreedyThreadPool(){
        this.threads = new ArrayList<>();
        this.channel = new LinkedBlockingQueue<>();
        this.errorOccurred = false;
    }

    /**
     * Submits a list of tasks to the pool.
     * @param tasks tasks to be executed
     */
    public void submitList(List<Callable<Optional<Boolean>>> tasks){
        for (var task : tasks){
            while(!errorOccurred){
                this.submit(task);
            }
        }
    }

    /**
     * Submits a single task to the pool and starts executing it in a new thread.
     * If the task returns an error stops the pool.
     * @param task task to be executed
     */
    private void submit(Callable<Optional<Boolean>> task){
        Thread t = new Thread(() -> {
            try {
                Optional<Boolean> res = task.call();
                processChildResult(res);
            } catch (Exception e) {
                handleUnexpectedException();
            }
        });
        threads.add(t);
        t.start();
    }

    /**
     * Stops the pool and all of its threads, waiting for them to finish.
     * All further calls to getResult() will return empty.
     * Idempotent - can be called multiple times without any effect except the first one.
     */
    public void stop(){
        if(errorOccurred){
            return; // guard for multiple calls
        }

        this.errorOccurred = true;
        for (var t : threads){
            t.interrupt();
        }
        waitForThreadsToFinish();
        // TODO clear the memory?
    }

    /**
     * Returns the result of the first task that has finished,
     * or empty if an error occurred.
     * @return the result of the first task that has finished or empty if an error occurred
     */
    public Optional<Boolean> getResult(){
        if(errorOccurred){
            return Optional.empty();
        }

        try {
            return channel.take();
        } catch (InterruptedException e) {
            handleUnexpectedException();
            return Optional.empty();
        }
    }

    /**
     * Checks if the result of the child thread indicates an error (i.e. is empty) and if not puts it in the channel.
     * @param res result of the child thread
     */
    private void processChildResult(Optional<Boolean> res){
        if (res.isEmpty()){
            handleUnexpectedException();
        } else{
            try{
                channel.put(res);
            } catch (InterruptedException e) {
                handleUnexpectedException();
            }
        }
    }

    /**
     * Stops the pool and all of its threads.
     * in current implementation it just calls stop().
     */
    private void handleUnexpectedException(){
        stop();
    }

    /**
     * Waits for all threads to finish.
     * Must be called only when the pool is stopped.
     */
    private void waitForThreadsToFinish(){
        assert errorOccurred:  "waitForThreadsToFinish() called when the pool is not stopped";

        for (var t : threads){
            boolean succeded = false;
            while(!succeded){
                try {
                    t.join();
                    succeded = true;
                } catch (InterruptedException e) {
                    // intentionally empty
                    // just run the loop again until the thread finishes
                }
            }
        }
    }

}
