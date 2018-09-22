package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import java.util.Collections;
import java.util.List;
import model.board.Terrain;
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

  /** Summon delegate that wants to summon buildings on AncientGround. */
  public static final class SummonBuildingOnAncientGroundDelegate extends SummonDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

    @Override
    public List<String> getSubweightsHeaders() {
      return Collections.emptyList();
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

  /**
   * Summon delegate that wants to summon less of a certain name of combatant by how many the player
   * already has, to encourage unit diversity.
   */
  public static final class SummonCombatantByNameScalingDelegate extends SummonByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.unitToSummon instanceof Combatant)) {
        return 0;
      }
      return -getSubWeight(action.unitToSummon.name)
          * action.player.getUnitCountByName(action.unitToSummon.name);
    }
  }

  /**
   * Summon delegate that wants to summon less of a certain name of building by how many the player
   * already has, to encourage unit diversity.
   */
  public static final class SummonBuildingByNameScalingDelegate extends SummonByNameDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.unitToSummon instanceof Building)) {
        return 0;
      }
      return -getSubWeight(action.unitToSummon.name)
          * action.player.getUnitCountByName(action.unitToSummon.name);
    }
  }
}
