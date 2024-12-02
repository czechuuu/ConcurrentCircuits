package cp2024.maxtests;

import java.time.Duration;

import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.CircuitSolver;
import cp2024.circuit.CircuitValue;
import cp2024.circuit.NodeType;
import cp2024.circuit.SleepyLeafNode;

public class TestUtils {
  public static class LeafNodeInterruptsCount extends SleepyLeafNode {
    private int countInterrupts = 0;
    private int called = 0;
    private Duration delay_args;
    public LeafNodeInterruptsCount(boolean value) {
        super(value);
    }

    public LeafNodeInterruptsCount(boolean value, Duration delay_leaf) {
        super(value, delay_leaf);
    }

    public LeafNodeInterruptsCount(boolean value, Duration delay_leaf, Duration delay_args) {
        super(value, delay_leaf);
        this.delay_args = delay_args;
    }

    public int getCountInterrupts() {
      return countInterrupts;
    }
    
    public int getCalled() {
      return called;
    }

    @Override
    public CircuitNode[] getArgs() throws InterruptedException {
        if (delay_args != null) Thread.sleep(delay_args.toMillis()); 
        return super.getArgs();
    }

    @Override
    public boolean getValue() throws InterruptedException {
        try {
            ++called;
            boolean val = super.getValue();
            return val;
        } catch (InterruptedException e) {
            ++countInterrupts;
            throw e;
        }
    } 
  }

    public static class Pair<U, V> {
        public final U first;
        public final V second;

        public Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }        
    }

    static CircuitNode[] args(boolean... values) {
        CircuitNode[] args = new CircuitNode[values.length];
        for (int i = 0; i < values.length; ++i) {
            args[i] = CircuitNode.mk(values[i]);
        }
        return args;
    }

    static boolean getVal(CircuitValue val) {
        try {
            return val.getValue();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interruption", e);
        }
    }

    static void activeWait(long millis) {
        long start = System.currentTimeMillis();
        long end = start + millis;
        while (System.currentTimeMillis() < end) {}
    }
    
    static Pair<Boolean, Long> measureExecutionMilis(CircuitSolver solver, Circuit c) {
        long start = System.currentTimeMillis();
        Boolean val = TestUtils.getVal(solver.solve(c));
        long elapsed = System.currentTimeMillis() - start;

        return new Pair<Boolean,Long>(val, elapsed);
    }

    static Pair<Boolean, Long> measureExecutionNanos(CircuitSolver solver, Circuit c) {
        long start = System.nanoTime();
        Boolean val = TestUtils.getVal(solver.solve(c));
        long elapsed = System.nanoTime() - start;

        return new Pair<Boolean,Long>(val, elapsed);
    }

    static CircuitNode deepNotLeafNode(int notCount, CircuitNode node) {
    if (notCount == 0) {
      return node;
    }
    return CircuitNode.mk(NodeType.NOT, deepNotLeafNode(notCount - 1, node));
  }

}
