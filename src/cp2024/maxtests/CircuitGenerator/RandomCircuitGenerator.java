package cp2024.maxtests.CircuitGenerator;

import java.util.Random;
import cp2024.circuit.Circuit;
import cp2024.circuit.CircuitNode;
import cp2024.circuit.LeafNode;
import cp2024.circuit.NodeType;
import cp2024.circuit.ThresholdNode;

public abstract class RandomCircuitGenerator {
  protected int currTotal = 0;
  protected int nonLeafNodeCount = 0;
  protected int maxHeight = 0;
  protected Random rand;
  protected int maxArity;

  public class CircuitWithStats {
    private Circuit circuit;
    private int numNonLeafNodes;
    private int numTotalNodes;
    private int height;

    public CircuitWithStats(Circuit circuit, int nonLeafNodeCount, int totalNodes, int height) {
      this.circuit = circuit;
      this.numNonLeafNodes = nonLeafNodeCount;
      this.numTotalNodes = totalNodes;
      this.height = height;
    }

    public Circuit getCircuit() {
      return circuit;
    }

    public int getNumNonLeafNodes() {
      return numNonLeafNodes;
    }

    public int getNumTotalNodes() {
      return numTotalNodes;
    }

    public int getHeight() {
      return height;
    }

    @Override
    public String toString() {
      return "Number of nodes: " + numTotalNodes
             + ". Non-leaf nodes: " + numNonLeafNodes
             + ". Height: " + height;
    }
  }

  public void setRandom(Random rand) { this.rand = rand; }

  protected RandomCircuitGenerator(Random rand, int maxArity) {
      this.rand = rand;
      this.maxArity = maxArity;
  }

  public CircuitWithStats generateCircuit() {
    Circuit circuit = new Circuit(randomNodeGeneration(0));
    var res = new CircuitWithStats(circuit, nonLeafNodeCount, currTotal, maxHeight);
    reset();
    return res;
  }

  private void reset() {
    currTotal = 0;
    nonLeafNodeCount = 0;
    maxHeight = 0;
  }

  protected CircuitNode LeafCreator() {
    return CircuitNode.mk(rand.nextBoolean());
  }

  public final void printCircuit(Circuit circuit) {
    CircuitNode root = circuit.getRoot();
    print(root);
    System.out.println();
  }

  public void printStatistics() {
    System.out.println("Number of nodes: " + currTotal + ". Non-leaf nodes: " + nonLeafNodeCount);
  }

  public static int findDepth(Circuit circuit) {
    var root = circuit.getRoot();
    return findDepthHelper(root, 0);
  }

  private static int findDepthHelper(CircuitNode node, int depth) {
    if (node.getType() == NodeType.LEAF) 
      return 0;
    int childDepth = 0;
    
    try {
      CircuitNode[] children = node.getArgs();
      for (var child : children) {
        childDepth = Math.max(childDepth, 1 + findDepthHelper(child, depth));
      }
    } catch (InterruptedException e) {}
    return childDepth;
  }

  protected abstract boolean leafNodeProbability(int currentDepth);

  private void print(CircuitNode node) {
    NodeType type = node.getType();
    try {
      if (type == NodeType.LEAF) {
          System.out.print(((LeafNode) node).getValue() ? "T" : "F");
      } else {
          System.out.print(type);
          if (type == NodeType.LT || type == NodeType.GT) {
              System.out.print(((ThresholdNode) node).getThreshold());
          }
          System.out.print("(");

          CircuitNode[] args = node.getArgs();
          for (int i = 0; i < args.length; i++) {
              print(args[i]);
              if (i < args.length - 1) {
                  System.out.print(", ");
              }
          }
          System.out.print(")");
      }
    } catch (InterruptedException e) {
      System.out.println("Exception happened");
    }
  }

  protected NodeType generateType(int currentDepth) {
    int numberOfTypes = NodeType.values().length;
    int typeIndex = currentDepth == 0 ? 1 + rand.nextInt(numberOfTypes - 1)
                                      :  rand.nextInt(numberOfTypes);
    return NodeType.values()[typeIndex];
  }

  protected int generateArity(NodeType type) {
    return switch (type) {
      case IF -> 3;
      case NOT -> 1;
      default -> 2 + rand.nextInt(this.maxArity - 2);
    };

  }
  protected CircuitNode randomNodeGeneration(int currentDepth) {
    ++currTotal;

    NodeType type = generateType(currentDepth);

    if (type != NodeType.LEAF) nonLeafNodeCount++;

    int arity = generateArity(type);

    ++currentDepth;

    maxHeight = Math.max(maxHeight, currentDepth);
    boolean leafNodesOnly = leafNodeProbability(currentDepth);
    return switch (type) {
      case LEAF -> LeafCreator();
      
      case IF, AND, OR -> CircuitNode.mk(type, createArgs(arity, leafNodesOnly, currentDepth));


      case GT, LT -> CircuitNode.mk(type, rand.nextInt(arity),
                                          createArgs(arity, leafNodesOnly, currentDepth));

      case NOT -> CircuitNode.mk(type, leafNodesOnly ? LeafCreator()
                                                     : randomNodeGeneration(currentDepth));

      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }

  private CircuitNode[] createArgs(int arity, boolean leafNodesOnly, int currentDepth) {
    if (leafNodesOnly) {
      currTotal += arity;
    }
    CircuitNode[] args = new CircuitNode[arity];
    for (int i = 0; i < args.length; ++i) {
      args[i] = leafNodesOnly ? LeafCreator() 
                              : randomNodeGeneration(currentDepth);
    }
    return args;
  }
}
