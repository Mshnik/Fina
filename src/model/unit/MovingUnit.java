package model.unit;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

import java.util.LinkedList;

/**
 * Represents a model.unit that is able to move around the model.board.
 *
 * @author MPatashnik
 */
public abstract class MovingUnit extends Unit {

  /** Units of movement remaining this turn. Can't move if this is 0 */
  private int movement;

  /**
   * Constructor for MovingUnit. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana.
   *
   * @param owner - the player owner of this unit
   * @param name - the name of this unit
   * @param level - the level of this unit - the age this belongs to
   * @param manaCost - the cost of summoning this unit. Should be a positive number.
   * @param startingTile - the tile this model.unit begins the game on. Also notifies the tile of
   *     this.
   * @param stats - the base unmodified stats of this unit.
   */
  public MovingUnit(
      Player owner, String name, int level, int manaCost, Tile startingTile, Stats stats)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, level, manaCost, startingTile, stats);
  }

  /**
   * Call at the beginning of every turn. Can be overridden in subclasses, but those classes should
   * call the super version before doing their own additions. - ticks down modifiers and
   * re-calculates stats, if necessary. - refreshes canMove
   */
  @Override
  public void refreshForTurn() {
    super.refreshForTurn();
    movement = getMovementCap();
  }

  /** Adds the given amount of movement. This is a temporary addition, not a recurring one */
  public void addMovement(int deltaMovement) {
    movement = Math.max(movement + deltaMovement, 0);
  }

  /** Refreshes Just movement. Can be done mid-turn for effect purposes */
  public void refreshMovement() {
    movement = getMovementCap();
  }

  // MOVEMENT
  /** Returns iff this can move */
  public final boolean canMove() {
    return getMovement() > 0;
  }

  /** Returns the amount of movement this can still make this turn */
  public int getMovement() {
    return movement;
  }

  /**
   * Returns the total converted movement this unit can take in a given turn (not updated for this
   * turn).
   */
  public final int getMovementCap() {
    return (Integer) stats.getStat(StatType.MOVEMENT_TOTAL);
  }

  /**
   * Returns the movement cost of traveling terrain t, infinity if this unit can't travel the given
   * terrain
   */
  public final int getMovementCost(Terrain t) {
    switch (t) {
      case ANCIENT_GROUND:
      case GRASS:
        return (Integer) stats.getStat(StatType.GRASS_COST);
      case MOUNTAIN:
        return (Integer) stats.getStat(StatType.MOUNTAIN_COST);
      case WOODS:
        return (Integer) stats.getStat(StatType.WOODS_COST);
      default:
        return Integer.MAX_VALUE;
    }
  }

  /** Returns true iff movementCost(t) <= getMovementCap() */
  @Override
  public boolean canOccupy(Terrain t) {
    return getMovementCost(t) <= getMovementCap();
  }

  /**
   * Processes a pre-move action that may be caused by modifiers. Still only called when the move is
   * valid.
   */
  public abstract void preMove(LinkedList<Tile> path);

  /**
   * Processes a post-move action that may be caused by modifiers. Only called when the move is
   * valid.
   */
  public abstract void postMove(LinkedList<Tile> path);

  /**
   * Attempts to move this model.unit along the given path.
   *
   * @param path - the path to travel, where the first of path is location, last is the desired
   *     destination, and whole path is manhattan contiguous.
   * @return The tile the movement ended on
   * @throws RuntimeException if... - this can't move this turn (already moved)
   * @throws IllegalArgumentException if... - the first tile isn't location - The ending tile is
   *     occupied - the total cost of movement exceeds this' movement total
   */
  public final Tile move(LinkedList<Tile> path) throws IllegalArgumentException, RuntimeException {
    if (!canMove()) throw new RuntimeException(this + " can't move this turn");
    if (path.get(0) != location)
      throw new IllegalArgumentException(
          this + " can't travel path " + path + ", it is on " + location);

    // Ok, starting at current location. ok to drop starting tile
    path.remove(0);

    int cost = 0;
    for (Tile t : path) {
      cost += getMovementCost(t.terrain);
    }

    if (cost > movement)
      throw new IllegalArgumentException(
          this + " can't travel path " + path + ", total cost of " + cost + " is too high");

    preMove(path);

    cost = 0;
    // Movement seems ok. Try to move along path, break if another model.unit of other player is
    // encountered.
    // Recalc cost as movement occurs
    Tile oldLoc = location;
    for (Tile t : path) {
      if (t.isOccupied() && t.getOccupyingUnit().owner != owner) break;
      location = t;
      cost += getMovementCost(t.terrain);
    }
    if (oldLoc != location) oldLoc.moveUnitTo(location);
    movement -= cost;
    owner.refreshVisionCloud();

    owner.game.refreshPassiveAbilities();

    postMove(path);

    return location;
  }
}
