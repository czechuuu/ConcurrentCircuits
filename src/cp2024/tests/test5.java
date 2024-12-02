package cp2024.tests;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

import static cp2024.tests.Utils.checkIfTrue;
import static cp2024.tests.Utils.timeCallable;

public class test5 {

    public static void main(String[] args) throws Exception {
        CircuitSolver solver = new ParallelCircuitSolver();

        Circuit c = new Circuit(
                CircuitNode.mk(NodeType.OR,
                        CircuitNode.mk(false, Duration.ofSeconds(100)),
                        CircuitNode.mk(false, Duration.ofSeconds(100)),
                        CircuitNode.mk(false, Duration.ofSeconds(100)),
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(false, Duration.ofSeconds(100)),
                                CircuitNode.mk(false, Duration.ofSeconds(100)),
                                CircuitNode.mk(true, Duration.ofSeconds(100))
                        ),
                        CircuitNode.mk(true, Duration.ofSeconds(2))
                )
        );

        var cv = solver.solve(c);

        checkIfTrue(timeCallable(cv::getValue));
//        Thread.sleep(1000);

        solver.stop();


    }


}