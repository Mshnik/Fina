package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.unit.Unit;
import model.unit.combatant.Combat;
import model.unit.combatant.Combatant;

/** A list of delegates for combat. */
public final class CombatDelegates {
  private CombatDelegates() {}

  /** Parent class for CombatDelegates. */
  private abstract static class CombatDelegate extends Delegate {
    private CombatDelegate() {
      super(AIActionType.ATTACK);
    }
  }

  /**
   * A Delegate for combat that wants the most damage dealt.
   *
   * <p>SubWeights:
   *
   * <ol>
   *   <li>0: MinAttack
   *   <li>1: MaxAttack
   * </ol>
   */
  public static final class MaxExpectedDamageDealtCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
      checkSubWeightsLength(2);
      Combat combat =
          new Combat((Combatant) action.actingUnit, action.targetedTile.getOccupyingUnit());
      return combat.getProjectedMaxAttack() * subWeights[0]
          + combat.getProjectedMinAttack() * subWeights[1];
    }
  }

  /**
   * A delegate that tries to minimize counter attack damage. All returns will be negative, since
   * this delegate would prefer to never attack at all.
   *
   * <p>SubWeights:
   *
   * <ol>
   *   <li>0: MinCounterAttack
   *   <li>1: MaxCounterAttack
   * </ol>
   */
  public static final class MinCounterAttackDamageCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
      checkSubWeightsLength(2);
      Combat combat =
          new Combat((Combatant) action.actingUnit, action.targetedTile.getOccupyingUnit());
      return -(combat.getProjectedMaxCounterAttack() * subWeights[0]
          + combat.getProjectedMinCounterAttack() * subWeights[1]);
    }
  }

  /**
   * A delegate that rewards keeping your units on the board and removing enemy units from the
   * board.
   *
   * <p>SubWeights:
   *
   * <ol>
   *   <li>0: ChanceOpponentDies
   *   <li>1: ChanceAllyDies (negative value).
   * </ol>
   */
  public static final class GainUnitAdvantageCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
      checkSubWeightsLength(2);
      Unit defender = action.targetedTile.getOccupyingUnit();
      Combat combat = new Combat((Combatant) action.actingUnit, defender);
      double chanceOpponentDies =
          Math.min(
              1,
              Math.max(
                  0,
                  (combat.getProjectedMaxAttack() - defender.getHealth())
                      / (combat.getProjectedMaxAttack() + 1 - combat.getProjectedMinAttack())));
      double chanceAllyDies =
          Math.min(
              1,
              Math.max(
                  0,
                  (combat.getProjectedMaxCounterAttack() - action.actingUnit.getHealth())
                      / (combat.getProjectedMaxCounterAttack()
                          + 1
                          - combat.getProjectedMinCounterAttack())));
      return chanceOpponentDies * subWeights[0] - chanceAllyDies * subWeights[1];
    }
  }
}
