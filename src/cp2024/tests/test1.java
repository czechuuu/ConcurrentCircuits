package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;
import static cp2024.tests.Utils.*;

public class test1 {


    public static void main(String[] args) throws Exception {
        CircuitSolver solver = new ParallelCircuitSolver();


        Circuit c1 = new Circuit(
            CircuitNode.mk(NodeType.GT, 3,
                CircuitNode.mk(true),
                CircuitNode.mk(true),
                CircuitNode.mk(true),
                CircuitNode.mk(true, Duration.ofSeconds(1)),
                CircuitNode.mk(false, Duration.ofSeconds(3))
            )
        );

        Circuit c2 = new Circuit(
            CircuitNode.mk(NodeType.GT, 3,
                CircuitNode.mk(true),
                CircuitNode.mk(true),
                CircuitNode.mk(true),
                CircuitNode.mk(true, Duration.ofSeconds(2)),
                CircuitNode.mk(false, Duration.ofSeconds(3))
            )
        );

        Circuit c3 = new Circuit(
                CircuitNode.mk(NodeType.LT, 2,
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(false, Duration.ofSeconds(10))
                )
        );

        Circuit c4 = new Circuit(
                CircuitNode.mk(NodeType.IF,
                        CircuitNode.mk(false, Duration.ofSeconds(4)),
                        CircuitNode.mk(true, Duration.ofSeconds(10)),
                        CircuitNode.mk(false, Duration.ofSeconds(1))
                )
        );

        Circuit c5 = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(false),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(100)),
                        CircuitNode.mk(false),
                        CircuitNode.mk(true, Duration.ofSeconds(5)),
                        CircuitNode.mk(false, Duration.ofSeconds(100))
                )
        );

        Circuit c6 = new Circuit(
                CircuitNode.mk(NodeType.AND,
                        CircuitNode.mk(true),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(100)),
                        CircuitNode.mk(true),
                        CircuitNode.mk(false, Duration.ofSeconds(6)),
                        CircuitNode.mk(true, Duration.ofSeconds(100))
                )
        );



        var cv1 = solver.solve(c1); // 1sec
        var cv2 = solver.solve(c2); // 2sec
        var cv3 = solver.solve(c3); // 3sec
        var cv4 = solver.solve(c4); // 4sec
        var cv5 = solver.solve(c5); // 5sec
        var cv6 = solver.solve(c6); // 6sec


        // Awaits seqeuntially so execution time of every getValue should be around 1sec.

        checkIfTrue( timeCallable(cv1::getValue) );
        checkIfTrue( timeCallable(cv2::getValue) );
        checkIfTrue( !timeCallable(cv3::getValue) );
        checkIfTrue( !timeCallable(cv4::getValue) );
        checkIfTrue( timeCallable(cv5::getValue) );
        checkIfTrue( !timeCallable(cv6::getValue) );



        solver.stop();
    }
}