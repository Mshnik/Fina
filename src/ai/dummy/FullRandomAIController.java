package ai.dummy;

import ai.AIAction;
import ai.AIController;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dummy AI controller that moves all units randomly, summons / builds new ones randomly, and
 * attacks units if possible.
 */
public final class FullRandomAIController implements AIController {

  /** The string to show on the UI for selecting a FullRandomAIController as a player. */
  public static final String FULL_RANDOM_AI_TYPE = "AI - Full Random";

  /** Chance to end turn even if there are actions available. */
  private static final double END_TURN_EARLY_CHANCE = 0.2;

  /**
   * Random used to pick tiles for moving and summoning as well as which unit/building to summon.
   */
  private final Random random = new Random();

  @Override
  public AIAction getNextAction(Player player) {
    if (random.nextDouble() <= END_TURN_EARLY_CHANCE) {
      return null;
    }
    AIAction nextMoveAction = getMoveAction(player);
    AIAction nextAttackAction = getAttackAction(player);
    AIAction nextSummonAction = getSummonAction(player);
    List<AIAction> actions =
        Stream.of(nextAttackAction, nextMoveAction, nextSummonAction)
            .filter(a -> a != null)
            .collect(Collectors.toList());
    if (actions.isEmpty()) {
      return null;
    }
    return actions.get(random.nextInt(actions.size()));
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
    if (movableTiles.isEmpty()) {
      return null;
    }
    Tile toMoveTo = movableTiles.get(random.nextInt(movableTiles.size()));
    return AIAction.moveUnit(movableUnit, toMoveTo, player.game.board.getMovementPath(toMoveTo));
  }

  /** Returns an action for a random unit to attack. Returns null if no unit can attack. */
  private AIAction getAttackAction(Player player) {
    List<Combatant> unitsThatCanAttack =
        player
            .getUnits()
            .stream()
            .filter(Unit::canFight)
            .filter(u -> u instanceof Combatant)
            .map(u -> (Combatant) u)
            .filter(Combatant::hasFightableTarget)
            .collect(Collectors.toList());
    if (unitsThatCanAttack.isEmpty()) {
      return null;
    }
    Combatant unitToAttackWith = unitsThatCanAttack.get(random.nextInt(unitsThatCanAttack.size()));
    List<Tile> attackableTiles = unitToAttackWith.getAttackableTiles(true);
    return AIAction.attack(
        unitToAttackWith, attackableTiles.get(random.nextInt(attackableTiles.size())));
  }

  /**
   * Returns a summon action if the commander can still summon. Returns null if it can't. (Out of
   * mana, out of actions, no room).
   */
  private AIAction getSummonAction(Player player) {
    if (player.getCommanderActionsRemaining() == 0) {
      return null;
    }
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
