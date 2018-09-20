package ai.delegating;

import ai.delegates.ByNameDelegate;
import ai.delegates.CombatDelegates.GainUnitAdvantageCombatDelegate;
import ai.delegates.CombatDelegates.MaxExpectedDamageDealtCombatDelegate;
import ai.delegates.CombatDelegates.MinCounterAttackDamageCombatDelegate;
import ai.delegates.MovementDelegates.ExpandDangerRadiusMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackAndNotBeCounterAttackedMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackMovementDelegate;
import ai.delegates.MovementDelegates.MoveToNotBeAttackedMovementDelegate;
import ai.delegates.SummonDelegates.SummonBuildingByNameDelegate;
import ai.delegates.SummonDelegates.SummonBuildingByNameScalingDelegate;
import ai.delegates.SummonDelegates.SummonBuildingOnAncientGroundDelegate;
import ai.delegates.SummonDelegates.SummonCombatantByNameDelegate;
import ai.delegates.SummonDelegates.SummonCombatantByNameScalingDelegate;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/** A listing of DelegatingAIControllers. */
public final class DelegatingAIControllers {

  /** The type to show on the ui for a default delegating AI controller. */
  public static final String DELEGATING_DEFAULT_AI_TYPE = "Del AI - Default";

  /** The type to show on the ui for a default delegating AI controller. */
  public static final String DELEGATING_RANDOM_AI_TYPE = "Del AI - Random";

  /** A delegating AIController for testing. */
  public static DelegatingAIController defaultDelegatingAIController() {
    return DelegatingAIControllerFactory.newBuilder()
        .addDelegate(new ExpandDangerRadiusMovementDelegate())
        .addDelegate(new MoveToNotBeAttackedMovementDelegate())
        .addDelegate(new MoveToAttackAndNotBeCounterAttackedMovementDelegate())
        .addDelegate(new MoveToAttackMovementDelegate())
        .addDelegate(new GainUnitAdvantageCombatDelegate())
        .addDelegate(new MaxExpectedDamageDealtCombatDelegate())
        .addDelegate(new MinCounterAttackDamageCombatDelegate())
        .addDelegate(new SummonBuildingOnAncientGroundDelegate())
        .addDelegate(new SummonBuildingByNameDelegate())
        .addDelegate(new SummonBuildingByNameScalingDelegate())
        .addDelegate(new SummonCombatantByNameDelegate())
        .addDelegate(new SummonCombatantByNameScalingDelegate())
        .build();
  }

  /** A helper for generating random weights and weight arrays. */
  private static final class RandomHelper {
    private static final Random random = new Random();

    /** Returns a random double in the given range, (effectively) inclusively. */
    private static double nextRandom(double min, double max) {
      return random.nextDouble() * (max - min) - min;
    }

    /**
     * Returns an array of random doubles in the given range, (effectively) inclusively, of the
     * given length.
     */
    private static double[] nextRandoms(double min, double max, int length) {
      double[] arr = new double[length];
      for (int i = 0; i < arr.length; i++) {
        arr[i] = nextRandom(min, max);
      }
      return arr;
    }
  }

  /**
   * Populates the given ByNameDelegate with the given names to indexes, with min and max random
   * values.
   */
  private static ByNameDelegate populateNamesWithRandomWeights(
      ByNameDelegate byNameDelegate, List<String> names, double min, double max) {
    int i = 0;
    for (String name : names) {
      byNameDelegate.withNameToSubweightIndex(name, i);
      i++;
    }
    byNameDelegate.withSubweights(RandomHelper.nextRandoms(min, max, i));
    return byNameDelegate;
  }

  /** A delegating AIController with random weights for testing and generating test data. */
  public static DelegatingAIController randomWeightsDelegatingAIController() {
    double min = -0.5;
    double max = 5.0;
    double subMin = -0.5;
    double subMax = 3.0;

    List<String> buildingNames =
        Buildings.getBuildings().stream().map(b -> b.name).collect(Collectors.toList());
    List<String> combatantNames =
        Combatants.getCombatants().stream().map(c -> c.name).collect(Collectors.toList());

    return DelegatingAIControllerFactory.newBuilder()
        // Movement delegates.
        .addDelegate(
            new ExpandDangerRadiusMovementDelegate().withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToNotBeAttackedMovementDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 2)))
        .addDelegate(
            new MoveToAttackAndNotBeCounterAttackedMovementDelegate()
                .withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToAttackMovementDelegate().withWeight(RandomHelper.nextRandom(min, max)))
        // Combat delegates.
        .addDelegate(
            new GainUnitAdvantageCombatDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 2)))
        .addDelegate(
            new MaxExpectedDamageDealtCombatDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 2)))
        .addDelegate(
            new MinCounterAttackDamageCombatDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 2)))
        // Summon delegates.
        .addDelegate(
            new SummonBuildingOnAncientGroundDelegate()
                .withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonBuildingByNameDelegate().withWeight(RandomHelper.nextRandom(min, max)),
                buildingNames,
                subMin,
                subMax))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonBuildingByNameScalingDelegate()
                    .withWeight(RandomHelper.nextRandom(min, max)),
                buildingNames,
                subMin,
                subMax))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonCombatantByNameDelegate().withWeight(RandomHelper.nextRandom(min, max)),
                combatantNames,
                subMin,
                subMax))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonCombatantByNameScalingDelegate()
                    .withWeight(RandomHelper.nextRandom(min, max)),
                combatantNames,
                subMin,
                subMax))
        .build();
  }
}
