package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Tile;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** A list of delegates for moving units. */
public final class MovementDelegates {
  private MovementDelegates() {}

  /** Parent class for MovementDelegates. */
  private abstract static class MovementDelegate extends Delegate {
    private MovementDelegate() {
      super(AIActionType.MOVE_UNIT);
    }
  }

  /**
   * A movement delegate that wants to expand the danger radius as much as possible. No subweights.
   */
  public static final class ExpandDangerRadiusMovementDelegate extends MovementDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

    @Override
    double getRawScore(AIAction action) {
      if (!(action.actingUnit instanceof Combatant)) {
        return 0;
      }
      Map<Combatant, Set<Tile>> dangerRadius = new HashMap<>(action.player.getDangerRadius());
      int preMoveDangerRadiusCount = getDangerRadiusSize(dangerRadius);
      Combatant combatant = (Combatant) action.actingUnit;
      dangerRadius.put(combatant, combatant.getDangerRadiusFromTile(action.targetedTile, true));
      int postMoveDangerRadiusCount = getDangerRadiusSize(dangerRadius);
      return postMoveDangerRadiusCount - preMoveDangerRadiusCount;
    }

    private int getDangerRadiusSize(Map<Combatant, Set<Tile>> dangerRadius) {
      return dangerRadius.values().stream().flatMap(Set::stream).collect(Collectors.toSet()).size();
    }
  }

  /**
   * A movement delegate that wants to allow units to attack. Wants to get the unit into immediate
   * attack range. No subWeights.
   */
  public static final class MoveToAttackMovementDelegate extends MovementDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

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
        return 1;
      }
      return 0;
    }
  }

  /**
   * A movement delegate that wants to allow units to attack and not be counter attacked. Wants to
   * get the unit into attack range that doesn't allow the opponent to counter attack. No
   * subweights.
   */
  public static final class MoveToAttackAndNotBeCounterAttackedMovementDelegate
      extends MovementDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 0;
    }

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
        return 1;
      }
      return 0;
    }
  }

  /**
   * A movement delegate that wants to keep units from being attacked. Wants to get the unit out of
   * immediate danger range. Returns a lower score the more units that can attack the targeted tile.
   * Subweights are Commander, Other.
   */
  public static final class MoveToNotBeAttackedMovementDelegate extends MovementDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 2;
    }

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
          * -1
          * (action.actingUnit instanceof Commander ? getSubWeight(0) : getSubWeight(1));
    }
  }
}
