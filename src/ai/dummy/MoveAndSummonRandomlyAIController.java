package ai.dummy;

import ai.AIAction;
import ai.AIController;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.commander.Commander;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** A dummy AI controller that moves all units randomly and summons / builds new ones randomly. */
public final class MoveAndSummonRandomlyAIController implements AIController {

  /** The string to show on the UI for selecting a MoveAndSummonRandomlyAIController as a player. */
  public static final String MOVE_AND_SUMMON_RANDOMLY_AI_TYPE = "AI - Move And Summon Randomly";

  /**
   * Random used to pick tiles for moving and summoning as well as which unit/building to summon.
   */
  private final Random random = new Random();

  @Override
  public AIAction getNextAction(Player player) {
    AIAction nextMoveAction = getMoveAction(player);
    AIAction nextSummonAction = getSummonAction(player);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      return null;
    }
    if (nextMoveAction == null && nextSummonAction == null) {
      return null;
    }
    if (nextMoveAction == null) {
      return nextSummonAction;
    }
    if (nextSummonAction == null) {
      return nextMoveAction;
    }
    return random.nextBoolean() ? nextMoveAction : nextSummonAction;
  }

  /**
   * Returns a movement action for a random unit that can still move. Returns null if no unit can
   * move.
   */
  private AIAction getMoveAction(Player player) {
    List<Unit> movableUnits =
        player.getUnits().stream().filter(Unit::canMove).collect(Collectors.toList());
    if (movableUnits.isEmpty()) {
      return null;
    }
    MovingUnit movableUnit = (MovingUnit) movableUnits.get(random.nextInt(movableUnits.size()));
    List<Tile> movableTiles =
        player
            .game
            .board
            .getMovementCloud(movableUnit, false)
            .stream()
            .filter(t -> !t.isOccupied())
            .collect(Collectors.toList());
    Tile toMoveTo = movableTiles.get(random.nextInt(movableTiles.size()));
    return AIAction.moveUnit(movableUnit, toMoveTo, player.game.board.getMovementPath(toMoveTo));
  }

  /**
   * Returns a summon action if the commander can still summon. Returns null if it can't. (Out of
   * mana, out of actions, no room).
   */
  private AIAction getSummonAction(Player player) {
    Commander commander = player.getCommander();
    Map<String, Unit> summonableUnits =
        Stream.concat(
                player.getCommander().getSummonables().entrySet().stream(),
                player.getCommander().getBuildables().entrySet().stream())
            .filter(
                e ->
                    e.getValue().getManaCostWithScalingAndDiscountsForPlayer(player)
                            <= commander.getMana()
                        && !player.game.board.getSummonCloud(commander, e.getValue()).isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));

    if (summonableUnits.isEmpty()) {
      return null;
    }
    List<String> unitNames = new ArrayList<>(summonableUnits.keySet());
    String unitToSummonName = unitNames.get(random.nextInt(unitNames.size()));
    Unit unitToSummon = summonableUnits.get(unitToSummonName);
    List<Tile> summonableLocations = player.game.board.getSummonCloud(commander, unitToSummon);
    return AIAction.summonCombatantOrBuildBuilding(
        commander,
        summonableLocations.get(random.nextInt(summonableLocations.size())),
        unitToSummon);
  }
}
