package ai.delegates;

import ai.AIAction;
import ai.AIAction.AIActionType;
import model.board.Board;
import model.board.Terrain;
import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.building.Building;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
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
    public List<String> getSubweightsHeaders() {
      return Collections.emptyList();
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
    public List<String> getSubweightsHeaders() {
      return Collections.emptyList();
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
    public List<String> getSubweightsHeaders() {
      return Collections.emptyList();
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
    public List<String> getSubweightsHeaders() {
      return Arrays.asList("Commander", "Combatant");
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

  /**
   * A movement delegate that wants the commander to move to a space where it could use all of its
   * actions to summon.
   */
  public static final class MoveToSummonDelegate extends MovementDelegate {

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
      if (!(action.actingUnit instanceof Commander)) {
        return 0;
      }
      return Math.min(
          action.actingUnit.getActionsRemaining(),
          action
              .player
              .game
              .board
              .getRadialCloud(action.targetedTile, action.actingUnit.getSummonRange())
              .stream()
              .filter(t -> !t.isOccupied() && action.player.canSee(t))
              .count());
    }
  }

  /** A movement delegate that wants the commander towards the nearest un-built ancient ground. */
  public static final class MoveToBuildOnAncientGroundDelegate extends MovementDelegate {

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
      if (!(action.actingUnit instanceof Commander)) {
        return 0;
      }
      Board board = action.player.game.board;
      List<Tile> wholeBoardPreMove =
          board.getMovementCloudWholeBoard(
              (MovingUnit) action.actingUnit, action.actingUnit.getLocation());
      int pathComputationId = board.getPathComputationId();
      OptionalInt distToNearestUnbuiltAncientGroundPreMove =
          wholeBoardPreMove
              .stream()
              .filter(t -> t.terrain == Terrain.ANCIENT_GROUND)
              .filter(t -> !t.isOccupied() || !(t.getOccupyingUnit() instanceof Building))
              .filter(t -> t != action.actingUnit.getLocation())
              .mapToInt(t -> board.getDist(pathComputationId, t))
              .max();
      if (distToNearestUnbuiltAncientGroundPreMove.isPresent()) {
        List<Tile> wholeBoardPostMove =
            board.getMovementCloudWholeBoard((MovingUnit) action.actingUnit, action.targetedTile);
        int newPathComputationId = board.getPathComputationId();
        OptionalInt distToNearestUnbuiltAncientGroundPostMove =
            wholeBoardPostMove
                .stream()
                .filter(t -> t.terrain == Terrain.ANCIENT_GROUND)
                .filter(t -> !t.isOccupied() || !(t.getOccupyingUnit() instanceof Building))
                .filter(t -> t != action.targetedTile)
                .mapToInt(t -> board.getDist(newPathComputationId, t))
                .max();
        if (distToNearestUnbuiltAncientGroundPostMove.isPresent()) {
          return distToNearestUnbuiltAncientGroundPostMove.getAsInt()
              - distToNearestUnbuiltAncientGroundPreMove.getAsInt();
        }
      }
      return 0;
    }
  }

  /**
   * A movement delegate that wants to move units towards the enemy commander. Returns 0 if the
   * player can't see the enemy commander. Subweights are Commander, Other.
   */
  public static final class MoveTowardsEnemyCommanderMovementDelegate extends MovementDelegate {

    @Override
    int getExpectedSubweightsLength() {
      return 2;
    }

    @Override
    public List<String> getSubweightsHeaders() {
      return Arrays.asList("Commander", "Combatant");
    }

    @Override
    double getRawScore(AIAction action) {
      Board board = action.player.game.board;
      List<Tile> wholeBoardPreMove =
          board.getMovementCloudWholeBoard(
              (MovingUnit) action.actingUnit, action.actingUnit.getLocation());
      int pathComputationId = board.getPathComputationId();
      OptionalInt distToNearestVisibleCommanderPreMove =
          wholeBoardPreMove
              .stream()
              .filter(t -> t.isOccupied() && t.getOccupyingUnit() instanceof Commander && t.getOccupyingUnit().owner != action.player)
              .filter(action.player::canSee)
              .mapToInt(t -> board.getDist(pathComputationId, t))
              .max();

      if (distToNearestVisibleCommanderPreMove.isPresent()) {
        List<Tile> wholeBoardPostMove =
            board.getMovementCloudWholeBoard((MovingUnit) action.actingUnit, action.targetedTile);
        int newPathComputationId = board.getPathComputationId();
        OptionalInt distToNearestVisibleCommanderPostMove =
            wholeBoardPostMove
                .stream()
                .filter(t -> t.isOccupied() && t.getOccupyingUnit() instanceof Commander && t.getOccupyingUnit().owner != action.player)
                .filter(action.player::canSee)
                .mapToInt(t -> board.getDist(newPathComputationId, t))
                .max();
        if (distToNearestVisibleCommanderPostMove.isPresent()) {
          return distToNearestVisibleCommanderPostMove.getAsInt()
              - distToNearestVisibleCommanderPostMove.getAsInt();
        }
      }
      return 0;
    }
  }
}
