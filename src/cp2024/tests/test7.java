package cp2024.tests;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

import static cp2024.tests.Utils.checkIfTrue;
import static cp2024.tests.Utils.timeCallable;

public class test7 {

    public static void main(String[] args) throws Exception {
        CircuitSolver solver = new ParallelCircuitSolver();

        var node1 = CircuitNode.mk(NodeType.IF,
                CircuitNode.mk(false, Duration.ofSeconds(100)),
                CircuitNode.mk(false, Duration.ofSeconds(1)),
                CircuitNode.mk(false, Duration.ofSeconds(1))
        );
        var node2 = CircuitNode.mk(NodeType.IF,
                CircuitNode.mk(false, Duration.ofSeconds(100)),
                CircuitNode.mk(true, Duration.ofSeconds(1)),
                CircuitNode.mk(true, Duration.ofSeconds(1))
        );
        var node3 = CircuitNode.mk(NodeType.IF,
                CircuitNode.mk(false, Duration.ofSeconds(2)),
                CircuitNode.mk(true, Duration.ofSeconds(1)),
                CircuitNode.mk(false, Duration.ofSeconds(1))
        );

        var c1 = new Circuit(CircuitNode.mk(NodeType.IF,
                node1, node2, node3
        ));
        var cv1 = solver.solve(c1);

        checkIfTrue(!timeCallable(cv1::getValue));

        var c2 = new Circuit(CircuitNode.mk(NodeType.IF,
                node2, node1, node3
        ));
        var cv2 = solver.solve(c2);

        checkIfTrue(!timeCallable(cv2::getValue));


         solver.stop();


    }


}