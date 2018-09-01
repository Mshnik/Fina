package ai.dummy;

import ai.AIAction;
import ai.AIController;
import model.board.Tile;
import model.game.Player;
import model.unit.commander.Commander;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A dummy AI controller that just moves the commander randomly to valid terrain, then ends turn.
 */
public final class MoveCommanderRandomlyAIController implements AIController {

  /** The string to show on the UI for selecting a MoveCommanderRandomlyAIController as a player. */
  public static final String MOVE_COMMANDER_RANDOMLY_AI_TYPE = "AI - Move Commander Randomly";

  /** Random used to pick tiles for commander. */
  private final Random random = new Random();

  @Override
  public AIAction getNextAction(Player player) {
    Commander commander = player.getCommander();
    if (commander.canMove()) {
      List<Tile> movableTiles =
          player
              .game
              .board
              .getRadialCloud(commander.getLocation(), 1)
              .stream()
              .filter(tile -> commander.getMovementCost(tile.terrain) <= commander.getMovement())
              .collect(Collectors.toList());
      movableTiles.remove(commander.getLocation());
      Tile toMoveTo = movableTiles.get(random.nextInt(movableTiles.size()));
      return AIAction.moveUnit(commander, toMoveTo);
    }
    return null;
  }
}
