package ai.delegating;

import ai.delegates.CombatDelegates.GainUnitAdvantageCombatDelegate;
import ai.delegates.CombatDelegates.MaxExpectedDamageDealtCombatDelegate;
import ai.delegates.CombatDelegates.MinCounterAttackDamageCombatDelegate;
import ai.delegates.MovementDelegates.ExpandDangerRadiusMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackAndNotBeCounterAttackedMovementDelegate;
import ai.delegates.MovementDelegates.MoveToAttackMovementDelegate;
import ai.delegates.MovementDelegates.MoveToNotBeAttackedMovementDelegate;
import ai.delegates.SummonDelegates.SummonBuildingByNameDelegate;
import ai.delegates.SummonDelegates.SummonBuildingOnAncientGroundDelegate;
import ai.delegates.SummonDelegates.SummonCombatantByNameDelegate;
import ai.delegates.SummonDelegates.SummonSpendLessManaDelegate;
import ai.delegates.SummonDelegates.SummonUnitsDelegate;

import java.util.Random;

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
        .addDelegate(new SummonUnitsDelegate())
        .addDelegate(new SummonBuildingOnAncientGroundDelegate())
        .addDelegate(new SummonSpendLessManaDelegate())
        .addDelegate(new SummonBuildingByNameDelegate())
        .addDelegate(new SummonCombatantByNameDelegate())
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

  /** A delegating AIController with random weights for testing and generating test data. */
  public static DelegatingAIController randomWeightsDelegatingAIController() {
    double min = -0.5;
    double max = 5.0;
    return DelegatingAIControllerFactory.newBuilder()
        .addDelegate(
            new ExpandDangerRadiusMovementDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToNotBeAttackedMovementDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToAttackAndNotBeCounterAttackedMovementDelegate()
                .setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MoveToAttackMovementDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new GainUnitAdvantageCombatDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MaxExpectedDamageDealtCombatDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new MinCounterAttackDamageCombatDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(new SummonUnitsDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new SummonBuildingOnAncientGroundDelegate()
                .setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(new SummonSpendLessManaDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new SummonBuildingByNameDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .addDelegate(
            new SummonCombatantByNameDelegate().setWeight(RandomHelper.nextRandom(min, max)))
        .build();
  }
}
