package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Tile;
import model.unit.combatant.Combatant;

/** A list of delegates for moving units. */
public final class MovementDelegates {
  private MovementDelegates() {}

  /** A movement delegate that wants to expand the danger radius as much as possible. */
  public static final class ExpandDangerRadiusMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack. Wants to get the unit into immediate
   * attack range.
   */
  public static final class MoveToAttackMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      if (action.actionType != AIActionType.MOVE_UNIT
          || !(action.actingUnit instanceof Combatant)) {
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
      implements Delegate {
    @Override
    public double getScore(AIAction action) {
      if (action.actionType != AIActionType.MOVE_UNIT
          || !(action.actingUnit instanceof Combatant)) {
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
  public static final class MoveToNotBeAttackedMovementDelegate implements Delegate {
    @Override
    public double getScore(AIAction action) {
      if (action.actionType != AIActionType.MOVE_UNIT) {
        return 0;
      }

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
