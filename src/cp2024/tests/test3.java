package cp2024.tests;

import cp2024.circuit.*;
import cp2024.solution.ParallelCircuitSolver;

import java.time.Duration;

import static cp2024.tests.Utils.checkIfTrue;

public class test3 {

    public static void main(String[] args) throws Exception {
        CircuitSolver solver = new ParallelCircuitSolver();

        Circuit[] c = {
                new Circuit(
                        CircuitNode.mk(NodeType.GT, 3,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(3))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.GT, 3,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(2)),
                                CircuitNode.mk(false, Duration.ofSeconds(3))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.LT, 2,
                                CircuitNode.mk(false),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(3)),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(3)),
                                CircuitNode.mk(false, Duration.ofSeconds(10))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.AND,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.OR,
                                CircuitNode.mk(false),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.NOT,
                                CircuitNode.mk(false, Duration.ofSeconds(1))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.IF,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.IF,
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.GT, 2,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false),
                                CircuitNode.mk(true, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.LT, 3,
                                CircuitNode.mk(true),
                                CircuitNode.mk(true),
                                CircuitNode.mk(false, Duration.ofSeconds(1)),
                                CircuitNode.mk(false, Duration.ofSeconds(2))
                        )
                ),
                new Circuit(
                        CircuitNode.mk(NodeType.IF,
                                CircuitNode.mk(true),
                                CircuitNode.mk(false, Duration.ofSeconds(1)),
                                CircuitNode.mk(true, Duration.ofSeconds(2))
                        )
                )
        };

        CircuitValue[] cv = new CircuitValue[c.length];

        for (int i = 0; i < c.length; i++) {
            cv[i] = solver.solve(c[i]);
        }

        boolean[] b = {true, true, false, false, true, true, true, false, false, true, false};

        for (int i = 0; i < c.length; i++) {
            checkIfTrue(cv[i].getValue() == b[i]);
        }

        solver.stop();


    }

}