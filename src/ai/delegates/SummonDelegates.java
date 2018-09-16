package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.unit.MovingUnit;
import model.unit.building.Building;

/** Delegate for summoning a new unit or building. */
public final class SummonDelegates {
  private SummonDelegates() {}

  /** Parent class for SummonDelegates. */
  private abstract static class SummonDelegate extends Delegate {
    private SummonDelegate() {
      super(AIActionType.SUMMON_COMBATANT_OR_BUILD_BUILDING);
    }
  }

  /** Summon delegate that wants to summon units. */
  public static final class SummonUnitsDelegate extends SummonDelegate {
    @Override
    double getRawScore(AIAction action) {
      return action.unitToSummon instanceof MovingUnit ? 1 : 0;
    }
  }

  /** Summon delegate that wants to summon buildings. */
  public static final class SummonBuildingDelegate extends SummonDelegate {
    @Override
    double getRawScore(AIAction action) {
      return action.unitToSummon instanceof Building ? 1 : 0;
    }
  }
}
