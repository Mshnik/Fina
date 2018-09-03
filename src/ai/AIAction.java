package ai;

import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;

import java.util.Collections;
import java.util.List;

/** A data class representing an action to take by the AI. */
public final class AIAction {

  /** Types of actions the AI can take. */
  public enum AIActionType {
    MOVE_UNIT,
    ATTACK,
    SUMMON_COMBATANT_OR_BUILD_BUILDING,
    CAST_SPELL
  }

  /**
   * The type of action to execute. Corresponds to the subclass of AIAction this should be processed
   * as.
   */
  public final AIActionType actionType;

  /** The unit performing the action. */
  public final Unit actingUnit;

  /**
   * The selected tile. Will be:
   *
   * <ul>
   *   <li>The adjacent tile to move actingUnit to if this is a MOVE_UNIT action
   *   <li>The tile containing the enemy unit to attack if this is a ATTACK action
   *   <li>The adjacent tile to summon/build/cast on if this is a SUMMON_COMBATANT, BUILD_BUILDING,
   *       or CAST_SPELL action
   * </ul>
   */
  public final Tile targetedTile;

  /** The path to travel when moving to the targetedTile. Only non-null for moving. */
  public final List<Tile> movePath;

  /** Unit to summon - should only be non-null for summoning. */
  public final Unit unitToSummon;

  /** Creates an AIAction that moves the given unit to the given adjacent tile. */
  public static AIAction moveUnit(MovingUnit unitToMove, Tile moveToTile, List<Tile> movePath) {
    return new AIAction(
        AIActionType.MOVE_UNIT,
        unitToMove,
        moveToTile,
        Collections.unmodifiableList(movePath),
        null);
  }

  /** Creates an AIAction that has the given unit attack the enemy unit on the given tile. */
  public static AIAction attack(Combatant attackingUnit, Tile tileToAttack) {
    return new AIAction(AIActionType.ATTACK, attackingUnit, tileToAttack, null, null);
  }

  /**
   * Creates an AIAction that has the given unit summon the given combatant or building on the given
   * tile.
   */
  public static <U extends Unit & Summoner> AIAction summonCombatantOrBuildBuilding(
      U summoningUnit, Tile tileToSummonOn, Unit unitToSummon) {
    return new AIAction(AIActionType.SUMMON_COMBATANT_OR_BUILD_BUILDING, summoningUnit, tileToSummonOn, null, unitToSummon);
  }

  /**
   * Creates an AIAction that has the given commander cast the given spell on the given tile target.
   */
  public static AIAction cast(Commander caster, Tile tileToTarget) {
    return new AIAction(AIActionType.CAST_SPELL, caster, tileToTarget, null, null);
  }

  /** Constructs an AIAction and asserts that the inputs are valid. */
  private AIAction(
      AIActionType actionType,
      Unit actingUnit,
      Tile targetedTile,
      List<Tile> movePath,
      Unit unitToSummon) {
    this.actionType = actionType;
    this.actingUnit = actingUnit;
    this.targetedTile = targetedTile;
    this.movePath = movePath;
    this.unitToSummon = unitToSummon;

    // Assert that this action construction is legal.
    checkPreconditions();
  }

  /** Asserts that all preconditions are valid for this Action, given the type. */
  private void checkPreconditions() {
    switch (actionType) {
      case MOVE_UNIT:
        assertPrecondition(
            actingUnit instanceof MovingUnit,
            "Can't do MOVE_UNIT action on " + actingUnit + ", it can't move");
        assertPrecondition(
            movePath.size() >= 2,
            "Can't do MOVE_UNIT action along " + movePath + ", it has fewer than two elements");
        assertPrecondition(
            movePath.get(0) == actingUnit.getLocation(),
            "Can't do MOVE_UNIT action along "
                + movePath
                + ", the unit isn't on the first location. It is on "
                + actingUnit.getLocation());
        assertPrecondition(
            movePath.get(movePath.size() - 1) == targetedTile,
            "Can't do MOVE_UNIT action along "
                + movePath
                + ", the final location isn't "
                + targetedTile);
        return;
      case ATTACK:
        assertPrecondition(
            actingUnit instanceof Combatant,
            "Can't do ATTACK action on " + actingUnit + ", it isn't a combatant");
        Combatant combatant = (Combatant) actingUnit;
        assertPrecondition(
            combatant.canFight(),
            "Can't do ATTACK action on " + actingUnit + ", it can't fight right now");
        assertPrecondition(
            combatant.getAttackableTiles(true).contains(targetedTile),
            "Can't do ATTACK action to " + targetedTile + ", it isn't in the attack range");
        assertPrecondition(
            targetedTile.isOccupied() && targetedTile.getOccupyingUnit().owner != actingUnit.owner,
            "Can't do ATTACK action to " + targetedTile + ", it doesn't contain an enemy unit");
        assertPrecondition(
            actingUnit.owner.canSee(targetedTile),
            "Can't do ATTACK action to " + targetedTile + ", this player can't see the tile");
        return;
      case SUMMON_COMBATANT_OR_BUILD_BUILDING:
        assertPrecondition(
            actingUnit instanceof Commander,
            "Can't do SUMMON_COMBATANT_OR_BUILD_BUILDING action on " + actingUnit);
        assertPrecondition(
            actingUnit.getActionsRemaining() > 0,
            "Can't do a SUMMON_COMBATANT_OR_BUILD_BUILDING action right now, unit has no actions left");
        assertPrecondition(
            actingUnit.getLocation().manhattanDistance(targetedTile) <= actingUnit.getSummonRange(),
            "Can't do SUMMON_COMBATANT_OR_BUILD_BUILDING action to "
                + targetedTile
                + ", it's too far away");
        assertPrecondition(
            unitToSummon.canOccupy(targetedTile.terrain),
            "Can't do SUMMON_COMBATANT_OR_BUILD_BUILDING on "
                + targetedTile
                + ", "
                + unitToSummon
                + " can't occupy that terrain");
        return;
      case CAST_SPELL:
        assertPrecondition(
            actingUnit instanceof Commander, "Can't do CAST_SPELL action on " + actingUnit);
        return;
      default:
        throw new RuntimeException("Got unsupported actionType " + actionType);
    }
  }

  /** Asserts the given boolean, or throws a runtime exception with the given message. */
  private static void assertPrecondition(boolean condition, String message) {
    if (!condition) {
      throw new RuntimeException(message);
    }
  }
}