package cp2024.maxtests.CircuitGenerator;

import java.util.Random;

public class BalancedRandomCircuitGenerator extends RandomCircuitGenerator {
  int desirableHeight = 0;

  public BalancedRandomCircuitGenerator(int desirableHeight, Random rand) {
    super(rand,5);
    this.desirableHeight = desirableHeight;
  }

  public BalancedRandomCircuitGenerator(int desirableHeight, Random rand, int maxArity) {
    super(rand, maxArity);
    this.desirableHeight = desirableHeight;
  }

  @Override
  protected boolean leafNodeProbability(int currentDepth) {
    return currentDepth >= desirableHeight;
  }

  @Override
  public String toString() {
    return "BalancedRandomCircuitGenerator";
  }

}