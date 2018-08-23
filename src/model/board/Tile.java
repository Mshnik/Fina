package model.board;

import controller.game.MatrixElement;
import model.game.Stringable;
import model.unit.Unit;
import model.util.MPoint;

import java.util.Set;

/**
 * A Tile is a single square in the model.board. Maintains information about its location, what kind
 * of tile it is, and what units are on it.
 *
 * <p>Comparability is on distance, for dijkstra's implementations.
 *
 * @author MPatashnik
 */
public final class Tile implements Comparable<Tile>, MatrixElement, Stringable {

  /** The model.board this belongs to */
  public final Board board;

  /** The row of this tile in its model.board */
  public final int row;

  /** The column of this tile in its model.board */
  public final int col;

  /** The terrain type of this tile. */
  public final Terrain terrain;

  /** The model.unit on this tile, if any */
  private Unit occupyingUnit;

  /** A convienence field for pathfinding implementations */
  int dist;

  /** A convienence field for pathfinding implementations */
  Tile prev;

  /**
   * The mountain range this tile belongs to, if any. Computed lazily, as needed. Should only be
   * used for mountains.
   */
  Set<Tile> mountainRange;

  /**
   * Constructor for Tile Class
   *
   * @param r - the row of this tile in the model.board matrix it belongs to
   * @param c - the column of this tile in the model.board matrix it belongs to
   * @param t - the terrain type of this tile.
   */
  public Tile(Board b, int r, int c, Terrain t) {
    board = b;
    row = r;
    col = c;
    terrain = t;
  }

  /**
   * Compares this to other using the row and col fields. Sort so that selection goes top to bottom,
   * left to right.
   */
  @Override
  public int compareTo(Tile other) {
    if (row < other.row) return -1;
    else if (row > other.row) return 1;
    else return col - other.col;
  }

  /** Return the Manhattan (only udlr) distance from this to other * */
  public int manhattanDistance(Tile other) {
    return Math.abs(row - other.row) + Math.abs(col - other.col);
  }

  /**
   * Returns the Direction to go from this to other, if they are bordering each other. Otherwise
   * returns null
   */
  public Direction directionTo(Tile other) {
    if (row == other.row && col == other.col + 1) return Direction.LEFT;
    if (row == other.row && col == other.col - 1) return Direction.RIGHT;
    if (col == other.col && row == other.row + 1) return Direction.UP;
    if (col == other.col && row == other.row - 1) return Direction.DOWN;
    return null;
  }

  /** Returns the occupyingUnit, if there is one */
  public Unit getOccupyingUnit() {
    return occupyingUnit;
  }

  /**
   * Adds the given model.unit to this tile
   *
   * @throws RuntimeException if this is already occupied
   * @throws IllegalArgumentException if u is null
   */
  public void addOccupyingUnit(Unit u) throws RuntimeException {
    if (occupyingUnit != null)
      throw new RuntimeException(
          "Can't add model.unit to " + this + ", already occupied by " + occupyingUnit);
    if (u == null) throw new IllegalArgumentException("Can't add a null model.unit to " + this);
    occupyingUnit = u;
  }

  /** Removes the current model.unit */
  public void removeOccupyingUnit() throws RuntimeException {
    occupyingUnit = null;
  }

  /**
   * Moves the given model.unit to the Tile other
   *
   * @throws IllegalArgumentException if other is already occupied
   */
  public void moveUnitTo(Tile other) throws IllegalArgumentException {
    other.addOccupyingUnit(occupyingUnit);
    removeOccupyingUnit();
  }

  /** Returns true iff there is an occupyingUnit */
  public boolean isOccupied() {
    return occupyingUnit != null;
  }

  /** Returns this.row */
  @Override
  public int getRow() {
    return row;
  }

  /** Returns this.col */
  @Override
  public int getCol() {
    return col;
  }

  /** Returns the location of this as a Point in (col, row) form */
  public MPoint getPoint() {
    return new MPoint(row, col);
  }

  /** ToString implementation - shows basic info of Tile. Remains brief to be useful in debugging */
  public String toString() {
    return getPoint().toString() + ":" + terrain;
  }

  @Override
  public String toStringShort() {
    return getPoint().toString() + ":" + terrain;
  }

  @Override
  public String toStringLong() {
    return getPoint().toString()
        + ":"
        + terrain
        + ", "
        + (isOccupied() ? "Unoccupied" : getOccupyingUnit().toStringShort());
  }

  @Override
  public String toStringFull() {
    return getPoint().toString()
        + ":"
        + terrain
        + ", "
        + (isOccupied() ? "Unoccupied" : getOccupyingUnit().toStringShort())
        + " in "
        + board.toStringShort();
  }
}
