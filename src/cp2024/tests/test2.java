package cp2024.tests;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.NodeType;
import cp2024.solution.ParallelCircuitSolver;
import java.time.Duration;

public class test2 {

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
        Thread.sleep(500);
        solver.stop();

        try {
            cv1.getValue();
            cv2.getValue();
            cv3.getValue();
            cv4.getValue();
            cv5.getValue();
            cv6.getValue();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException from CircuitValue.getValue()!");
        }

    }
}