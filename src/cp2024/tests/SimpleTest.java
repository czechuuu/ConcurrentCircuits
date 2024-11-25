package cp2024.tests    ;

import cp2024.circuit.*;
import cp2024.demo.SequentialSolver;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleTest {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> fails = new ArrayList<>();
        CircuitSolver solver = new ParallelCircuitSolver();

        // Simple true value evaluation
        Circuit c1 = new Circuit(CircuitNode.mk(true));
        CircuitValue test1 = solver.solve(c1);
        System.out.println("Test 1 (True) e true got:" + test1.getValue());  // Expected: true
        if(!test1.getValue()) fails.add(1);

        // Simple false value evaluation with delay
        Circuit c2 = new Circuit(CircuitNode.mk(false, Duration.ofSeconds(1)));
        CircuitValue test2 = solver.solve(c2);
        System.out.println("Test 2 (False with delay) e false got: " + test2.getValue());  // Expected: false
        if(test2.getValue()) fails.add(2);

        // AND short-circuiting (should stop on first false)
        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                        CircuitNode.mk(false )
                )
        );
        CircuitValue test3 = solver.solve(c3);
        System.out.println("Test 3 (AND short-circuit) expected false, got: " + test3.getValue());  // Expected: false (without delay)
        if(test3.getValue()) fails.add(3);

        // OR short-circuiting (should stop on first true)
        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(false),
                        CircuitNode.mk(false, Duration.ofSeconds(2)),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue test4 = solver.solve(c4);
        System.out.println("Test 4 (OR short-circuit) expected true got: " + test4.getValue());  // Expected: true (without delay)
        if(!test4.getValue()) fails.add(4);

        // Greater-than threshold test with delay (2 true values needed)
        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.GT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true)
                )
        );
        CircuitValue test5 = solver.solve(c5);
        System.out.println("Test 5 (GT threshold) expected true, got: " + test5.getValue());  // Expected: true
        if(!test5.getValue()) fails.add(5);

        // Less-than threshold test (2 true values allowed)
        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(true),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true),
                        CircuitNode.mk(true, Duration.ofSeconds(1))
                )
        );
        CircuitValue test6 = solver.solve(c6);
        System.out.println("Test 6 (LT threshold) expected false, got: " + test6.getValue());  // Expected: false
        if(test6.getValue()) fails.add(6);

        // IF condition with one branch having a delay
        Circuit c7 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(2)),
                        CircuitNode.mk(false)
                )
        );
        CircuitValue test7 = solver.solve(c7);
        System.out.println("Test 7 (IF condition) expected false, got: " + test7.getValue());  // Expected: false (without delay)
        if(test7.getValue()) fails.add(7);

        // IF condition with the other branch having a delay
        Circuit c8 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(2))
                )
        );
        CircuitValue test8 = solver.solve(c8);
        System.out.println("Test 8 (IF condition with delay) expected true: " + test8.getValue());  // Expected: true (without delay)
        if(!test8.getValue()) fails.add(8);

        // Stopping the solver while solving a delayed node
        solver.stop();
        Circuit c9 = new Circuit(CircuitNode.mk(true, Duration.ofSeconds(3)));
        try {
            System.out.println("Test 9 (Solver stopped): " + solver.solve(c9).getValue());
        } catch (InterruptedException e) {
            System.out.println("Test 9 (Solver stopped): Computation interrupted.");
        }

        // Checking the result of a previously computed value after stop
        System.out.println("Re-access Test 7 value after stop: " + test7.getValue());  // Expected: false (no exception)

        System.out.println("End of Extended Test");
        System.out.println("Number of failed tests: " + fails.size() + ": " + Arrays.toString(fails.toArray()));
    }
}
