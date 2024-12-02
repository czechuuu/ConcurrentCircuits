package cp2024.maxtests.CircuitGenerator;

import java.util.Random;

public class DeepRandomCircuitGenerator extends RandomCircuitGenerator {
  private int totalNodeCount;

  /**
   * @param totalNodeCount total number of nodes in the circuit (very roughly)
   * @param rand           a random number generator for circuit randomness
   */
  public DeepRandomCircuitGenerator(int totalNodeCount, Random rand) {
      super(rand, 5);
      this.totalNodeCount = totalNodeCount / 2;
  }

  @Override
  protected boolean leafNodeProbability(int currentDepth) {
    double probability = Math.min(1.0, (double) nonLeafNodeCount/ totalNodeCount);
    return rand.nextDouble() < probability;
  }

  @Override
  public String toString() {
    return "DeepRandomCircuitGenerator";
  }
}