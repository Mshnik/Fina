package ai.delegating;

import ai.delegates.ByNameDelegate;
import ai.delegates.CastDelegates.CastSpellByNameDelegate;
import ai.delegates.CastDelegates.MaximizeUnitsEffectedCastByNameDelegate;
import ai.delegates.CastDelegates.MinimizeRedundantEffectByNameCastDelegate;
import ai.delegates.CombatDelegates.GainUnitAdvantageCombatDelegate;
import ai.delegates.CombatDelegates.MaxExpectedDamageDealtCombatDelegate;
import ai.delegates.CombatDelegates.MinCounterAttackDamageCombatDelegate;
import ai.delegates.MovementDelegates.ExpandDangerRadiusMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackAndNotBeCounterAttackedMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackFavoredEnemiesMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackMovementDelegate;
import ai.delegates.MovementDelegates.MoveToBuildOnAncientGroundDelegate;
import ai.delegates.MovementDelegates.MoveToNotBeAttackedMovementDelegate;
import ai.delegates.MovementDelegates.MoveToSummonDelegate;
import ai.delegates.MovementDelegates.MoveTowardsEnemyCommanderMovementDelegate;
import ai.delegates.SummonDelegates.SummonBuildingByNameDelegate;
import ai.delegates.SummonDelegates.SummonBuildingByNameScalingDelegate;
import ai.delegates.SummonDelegates.SummonBuildingOnAncientGroundDelegate;
import ai.delegates.SummonDelegates.SummonCombatantByNameDelegate;
import ai.delegates.SummonDelegates.SummonCombatantByNameScalingDelegate;
import ai.delegates.SummonDelegates.SummonCombatantWithTypeAdvantageDelegate;
import controller.game.CreatePlayerOptions;
import controller.game.GameController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import model.game.Game.FogOfWar;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

/** A listing of DelegatingAIControllers. */
public final class DelegatingAIControllers {

  /** The type to show on the ui for a default delegating AI controller. */
  public static final String DELEGATING_DEFAULT_AI_TYPE = "Del AI - Default";

  /** The type to show on the ui for a random delegating AI controller. */
  public static final String DELEGATING_RANDOM_AI_TYPE = "Del AI - Random";

  /** The type to show on the ui for a random delegating AI controller. */
  public static final String DELEGATING_RANDOM_WITH_EDITS_AI_TYPE = "Del AI - Random w/Edits";

  /** A delegating AIController for testing. */
  public static DelegatingAIController defaultDelegatingAIController() {
    List<String> buildingNames =
        Buildings.getBuildings().stream().map(b -> b.name).collect(Collectors.toList());
    List<String> combatantNames =
        Combatants.getCombatants().stream().map(c -> c.name).collect(Collectors.toList());
    List<String> abilityNames =
        Abilities.getAbilities().stream().map(c -> c.name).collect(Collectors.toList());

    return DelegatingAIControllerFactory.newBuilder()
        .addDelegate(new ExpandDangerRadiusMovementDelegate())
        .addDelegate(new MoveToNotBeAttackedMovementDelegate())
        .addDelegate(new MoveToAttackAndNotBeCounterAttackedMovementDelegate())
        .addDelegate(new MoveToAttackMovementDelegate())
        .addDelegate(new MoveToAttackFavoredEnemiesMovementDelegate())
        .addDelegate(new MoveToSummonDelegate())
        .addDelegate(new MoveToBuildOnAncientGroundDelegate())
        .addDelegate(new MoveTowardsEnemyCommanderMovementDelegate())
        .addDelegate(new GainUnitAdvantageCombatDelegate())
        .addDelegate(new MaxExpectedDamageDealtCombatDelegate())
        .addDelegate(new MinCounterAttackDamageCombatDelegate())
        .addDelegate(new SummonCombatantWithTypeAdvantageDelegate())
        .addDelegate(new SummonBuildingOnAncientGroundDelegate())
        .addDelegate(
            populateNamesWithRandomWeights(new SummonBuildingByNameDelegate(), buildingNames, 1, 1))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonBuildingByNameScalingDelegate(), buildingNames, 1, 1))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonCombatantByNameDelegate(), combatantNames, 1, 1))
        .addDelegate(
            populateNamesWithRandomWeights(
                new SummonCombatantByNameScalingDelegate(), combatantNames, 1, 1))
        .addDelegate(
            populateNamesWithRandomWeights(new CastSpellByNameDelegate(), abilityNames, 1, 1))
        .addDelegate(new MaximizeUnitsEffectedCastByNameDelegate())
        .addDelegate(
            populateNamesWithRandomWeights(
                new MinimizeRedundantEffectByNameCastDelegate(), abilityNames, 1, 1))
        .build();
  }

  /** A helper for generating random weights and weight arrays. */
  private static final class RandomHelper {
    private static final Random random = new Random();

    /** Returns a random double in the given range, (effectively) inclusively. */
    private static double nextRandom(double min, double max) {
      return random.nextDouble() * (max - min) + min;
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
    double min = 0;
    double max = 6.0;
    double subMin = 0.5;
    double subMax = 1.5;

    List<String> buildingNames =
        Buildings.getBuildings().stream().map(b -> b.name).collect(Collectors.toList());
    List<String> combatantNames =
        Combatants.getCombatants().stream().map(c -> c.name).collect(Collectors.toList());
    List<String> abilityNames =
        Abilities.getAbilities().stream().map(c -> c.name).collect(Collectors.toList());

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
            new MoveToAttackMovementDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 3)))
        .addDelegate(
            new MoveToAttackFavoredEnemiesMovementDelegate()
                .withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(new MoveToSummonDelegate().withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToBuildOnAncientGroundDelegate().withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveTowardsEnemyCommanderMovementDelegate()
                .withWeight(RandomHelper.nextRandom(min, max))
                .withSubweights(RandomHelper.nextRandoms(subMin, subMax, 2)))
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
            new SummonCombatantWithTypeAdvantageDelegate()
                .withWeight(RandomHelper.nextRandom(min, max)))
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
        // Casting
        .addDelegate(
            populateNamesWithRandomWeights(
                new CastSpellByNameDelegate().withWeight(RandomHelper.nextRandom(min, max)),
                abilityNames,
                subMin,
                subMax))
        .addDelegate(
            new MaximizeUnitsEffectedCastByNameDelegate()
                .withWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            populateNamesWithRandomWeights(
                new MinimizeRedundantEffectByNameCastDelegate()
                    .withWeight(RandomHelper.nextRandom(min, max)),
                abilityNames,
                subMin,
                subMax))
        .build();
  }

  /**
   * A delegating AIController with random weights plus manual edits for testing and generating test
   * data.
   */
  public static DelegatingAIController randomWeightsWithManualEditsDelegatingAIController() {
    DelegatingAIController randomWeightsController = randomWeightsDelegatingAIController();
    randomWeightsController
        .getDelegate(MoveTowardsEnemyCommanderMovementDelegate.class)
        .changeWeight(1);
    randomWeightsController.getDelegate(MoveToSummonDelegate.class).changeWeight(2);
    randomWeightsController
        .getDelegate(SummonCombatantWithTypeAdvantageDelegate.class)
        .changeWeight(5);
    randomWeightsController.getByNameDelegate(SummonCombatantByNameDelegate.class).changeWeight(3);
    randomWeightsController.getByNameDelegate(SummonBuildingByNameDelegate.class).changeWeight(1);
    randomWeightsController.getByNameDelegate(CastSpellByNameDelegate.class).changeWeight(-4);
    return randomWeightsController;
  }

  /** Runnable method that plays delegating AI controllers against each other. */
  public static void main(String[] args) throws Exception {
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Backyard.csv";
    genDataLoop(boardFilename);
  }

  /** Runs games repeatedly on the given boardFilename between two Delegating random AIs. */
  private static void genDataLoop(String boardFilename) throws Exception {
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(DELEGATING_RANDOM_WITH_EDITS_AI_TYPE);
    int i = 0;
    while (true) {
      Collections.shuffle(defaultPlayerTypes);
      GameController controller =
          GameController.loadAndStartHeadless(
              "game/boards/" + boardFilename,
              defaultPlayerTypes
                  .stream()
                  .map(CreatePlayerOptions::new)
                  .collect(Collectors.toList()),
              FogOfWar.REGULAR,
              1);
      Thread.sleep(150);
      System.out.println("Game " + i + " started");
      while (controller.game.isRunning()) {
        Thread.sleep(20);
      }
      System.out.println("Game " + i + " finished");
      i++;
    }
  }
}
