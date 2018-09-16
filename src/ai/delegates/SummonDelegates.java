package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.unit.MovingUnit;
import model.unit.building.Building;
import model.unit.combatant.Combatant;
import model.unit.combatant.Combatant.CombatantClass;

/** Delegate for summoning a new unit or building. */
public final class SummonDelegates {
  private SummonDelegates() {}

  /** Parent class for SummonDelegates. */
  private abstract static class SummonDelegate extends Delegate {
    private SummonDelegate() {
      super(AIActionType.SUMMON_COMBATANT_OR_BUILD_BUILDING);
    }
  }

  /** Summon delegate that wants to not spend mana. */
  public static final class SummonSpendLessManaDelegate extends SummonDelegate {
    @Override
    double getRawScore(AIAction action) {
      return -action.unitToSummon.getManaCostWithScalingAndDiscountsForPlayer(action.player);
    }
  }

  /**
   * Summon delegate that wants to summon a certain type of units.
   *
   * <p>
   *
   * <ol>
   *   <li>0: Units
   *   <li>1: Buildings
   * </ol>
   */
  public static final class SummonUnitsDelegate extends SummonDelegate {
    @Override
    double getRawScore(AIAction action) {
      checkSubWeightsLength(2);
      if (action.unitToSummon instanceof MovingUnit) {
        return subWeights[0];
      } else if (action.unitToSummon instanceof Building) {
        return subWeights[1];
      } else {
        return 0;
      }
    }
  }

  /**
   * Summon delegate that wants to summon a certain type of combatant. MultiClassed combatants are
   * calculated as the average of their two values.
   *
   * <p>
   *
   * <ol>
   *   <li>0: Fighter
   *   <li>1: Assassin
   *   <li>2: Mage
   *   <li>3: Ranger
   *   <li>4: Tank
   * </ol>
   */
  public static final class SummonCombatantTypeDelegate extends SummonDelegate {
    @Override
    double getRawScore(AIAction action) {
      checkSubWeightsLength(5);
      if (!(action.unitToSummon instanceof Combatant)) {
        return 0;
      }
      Combatant combatant = (Combatant) action.unitToSummon;
      double weight = 0;
      for (CombatantClass combatantClass : combatant.combatantClasses) {
        weight += subWeights[combatantClass.ordinal()];
      }
      return weight / combatant.combatantClasses.size();
    }
  }
}
