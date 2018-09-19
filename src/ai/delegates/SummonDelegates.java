package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Terrain;
import model.unit.MovingUnit;
import model.unit.building.Building;
import model.unit.combatant.Combatant;

/** Delegate for summoning a new unit or building. */
public final class SummonDelegates {
  private SummonDelegates() {}

  /** Parent class for SummonDelegates. */
  private abstract static class SummonDelegate extends Delegate {
    private SummonDelegate() {
      super(AIActionType.SUMMON_COMBATANT_OR_BUILD_BUILDING);
    }
  }

  /** Parent class for SummonByNameDelegates. */
  private abstract static class SummonByNameDelegate extends ByNameDelegate {
    private SummonByNameDelegate() {
      super(AIActionType.SUMMON_COMBATANT_OR_BUILD_BUILDING);
    }
  }

  /** Summon delegate that wants to not spend mana. */
  public static final class SummonSpendLessManaDelegate extends SummonDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

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
    int getExpectedSubweightsLength() {
      return 2;
    }

    @Override
    double getRawScore(AIAction action) {
      if (action.unitToSummon instanceof MovingUnit) {
        return getSubWeight(0);
      } else if (action.unitToSummon instanceof Building) {
        return getSubWeight(1);
      } else {
        return 0;
      }
    }
  }

  /** Summon delegate that wants to summon buildings on AncientGround. */
  public static final class SummonBuildingOnAncientGroundDelegate extends SummonDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

    @Override
    double getRawScore(AIAction action) {
      if (action.unitToSummon instanceof Building
          && action.targetedTile.terrain == Terrain.ANCIENT_GROUND) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /** Summon delegate that wants to summon a certain name of combatant. */
  public static final class SummonCombatantByNameDelegate extends SummonByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.unitToSummon instanceof Combatant)) {
        return 0;
      }
      return getSubWeight(action.unitToSummon.name);
    }
  }

  /** Summon delegate that wants to summon a certain name of building. */
  public static final class SummonBuildingByNameDelegate extends SummonByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.unitToSummon instanceof Building)) {
        return 0;
      }
      return getSubWeight(action.unitToSummon.name);
    }
  }
}
