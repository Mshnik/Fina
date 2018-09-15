package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Tile;
import model.unit.combatant.Combatant;

/** A list of delegates for moving units. */
public final class MovementDelegates {
  private MovementDelegates() {}

  /** Parent class for MovementDelegates. */
  private abstract static class MovementDelegate extends Delegate {
    private MovementDelegate() {
      super(AIActionType.MOVE_UNIT);
    }
  }

  /** A movement delegate that wants to expand the danger radius as much as possible. */
  public static final class ExpandDangerRadiusMovementDelegate extends MovementDelegate {
    @Override
    double getRawScore(AIAction action) {
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack. Wants to get the unit into immediate
   * attack range.
   */
  public static final class MoveToAttackMovementDelegate extends MovementDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.actingUnit instanceof Combatant)) {
        return 0;
      }
      Combatant combatant = (Combatant) action.actingUnit;
      if (combatant
          .getAttackableTilesFrom(action.targetedTile)
          .stream()
          .anyMatch(t -> t.isOccupied() && t.getOccupyingUnit().owner != action.player)) {
        return 100;
      }
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack and not be counter attacked. Wants to
   * get the unit into attack range that doesn't allow the opponent to counter attack.
   */
  public static final class MoveToAttackAndNotBeCounterAttackedMovementDelegate
      extends MovementDelegate {
    @Override
    double getRawScore(AIAction action) {
      if (!(action.actingUnit instanceof Combatant)) {
        return 0;
      }
      Combatant combatant = (Combatant) action.actingUnit;
      if (combatant
          .getAttackableTilesFrom(action.targetedTile)
          .stream()
          .filter(t -> t.isOccupied() && t.getOccupyingUnit().owner != action.player)
          .map(Tile::getOccupyingUnit)
          .anyMatch(
              u ->
                  !(u instanceof Combatant)
                      || ((Combatant) u).getAttackableTiles(true).contains(action.targetedTile))) {
        return 100;
      }
      return 0;
    }
  }

  /**
   * A movement delegate that wants to keep units from being attacked. Wants to get the unit out of
   * immediate danger range. Returns a lower score the more units that can attack the targeted tile.
   */
  public static final class MoveToNotBeAttackedMovementDelegate extends MovementDelegate {
    @Override
    double getRawScore(AIAction action) {
      return action
              .player
              .game
              .getDangerRadius(action.player)
              .entrySet()
              .stream()
              .filter(e -> e.getKey().owner != action.player)
              .filter(e -> e.getValue().contains(action.targetedTile))
              .count()
          * -10;
    }
  }
}
