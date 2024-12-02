package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

import static cp2024.tests.Utils.checkIfTrue;
import static cp2024.tests.Utils.timeCallable;

public class test4 {

    public static void main(String[] args) throws Exception {
        CircuitSolver solver = new ParallelCircuitSolver();

        Circuit c = new Circuit(
                CircuitNode.mk(NodeType.GT, 1,
                        CircuitNode.mk(NodeType.GT, 1,
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(false, Duration.ofSeconds(100))
                        ),
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(true, Duration.ofSeconds(100))
                        ),
                        CircuitNode.mk(true, Duration.ofSeconds(3)),
                        CircuitNode.mk(true, Duration.ofSeconds(3))
                )
        );

        var cv = solver.solve(c);

        checkIfTrue( timeCallable(cv::getValue) );

        solver.stop();


    }


}