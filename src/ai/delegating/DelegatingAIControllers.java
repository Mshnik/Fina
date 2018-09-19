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

/** A listing of DelegatingAIControllers. */
public final class DelegatingAIControllers {

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
}
