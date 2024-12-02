package cp2024.tests;

import java.util.concurrent.Callable;

public class Utils {
    public static <T> T timeCallable(Callable<T> task) throws Exception {
        long startTime = System.nanoTime();
        T result = task.call(); // Execute the task
        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Execution time: " + seconds + " seconds");

        return result;
    }

    public static void checkIfTrue(Boolean b) {
        if (b) {
            System.out.println("OK");
        } else {
            System.out.println("WA");
            throw new RuntimeException("WA");
        }
    }
}
