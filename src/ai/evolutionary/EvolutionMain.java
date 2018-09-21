package ai.evolutionary;

import ai.delegating.DelegatingAIControllers;
import java.io.FileNotFoundException;

/**
 * An executable class that runs an evolutionary algorithm on a population of AIs. Starts with a
 * population of random AIs, then has every pair play each-other repeatedly (with random
 * reselection). Players that win gain points, players that lose lose points. If a player loses all
 * points, they are knocked out of the pool. If a player gains enough points, it splits in half,
 * where the other half is a copy of itself with some modifications.
 */
final class EvolutionMain {

  public static void main(String[] args) throws FileNotFoundException {
    EvoPopulation population = new EvoPopulation("Backyard.csv");
    for (int i = 0; i < 50; i++) {
      population.addPlayer(
          new EvoPlayer(
              DelegatingAIControllers.randomWeightsDelegatingAIController().getDelegates()));
    }
    population.runSimulation(200);
  }
}
