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

  /** A Delegate for combat that wants the most damage dealt. */
  public static final class MaxExpectedDamageDealtCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
      Combat combat =
          new Combat((Combatant) action.actingUnit, action.targetedTile.getOccupyingUnit());
      return (combat.getProjectedMaxAttack() + combat.getProjectedMinAttack()) / 2;
    }
  }

  /**
   * A delegate that tries to minimize counter attack damage. All returns will be negative, since
   * this delegate would prefer to never attack at all.
   */
  public static final class MinCounterAttackDamageCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
      Combat combat =
          new Combat((Combatant) action.actingUnit, action.targetedTile.getOccupyingUnit());
      return -(combat.getProjectedMaxCounterAttack() + combat.getProjectedMinCounterAttack()) / 2;
    }
  }

  /**
   * A delegate that rewards keeping your units on the board and removing enemy units from the
   * board.
   */
  public static final class GainUnitAdvantageCombatDelegate extends CombatDelegate {

    @Override
    double getRawScore(AIAction action) {
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
      return chanceOpponentDies * 100 - chanceAllyDies * 100;
    }
  }
}
