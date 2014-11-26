package board;

import unit.Unit;

/** A Tile is a single square in the board.
 * Maintains information about its location, what kind of tile it is, and what units are on it.
 * 
 * Comparability is on distance, for dijkstra's implementations.
 * @author MPatashnik
 *
 */
public class Tile implements Comparable<Tile>{

	/** The row of this tile in its board */
	public final int row;

	/** The column of this tile in its board */
	public final int col;

	/** The terrain type of this tile. */
	public final Terrain terrain;

	/** The unit on this tile, if any */
	private Unit occupyingUnit;

	/** A convienence field for pathfinding implementations */
	public int dist;

	/** A convienence field for pathfinding implementations */
	public Tile prev;

	/** Constructor for Tile Class
	 * @param r - the row of this tile in the board matrix it belongs to
	 * @param c - the column of this tile in the board matrix it belongs to
	 * @param t - the terrain type of this tile.
	 */
	public Tile(int r, int c, Terrain t){
		row = r;
		col = c;
		terrain = t;
	}

	/** Compares this to other using the dist field - lower dist comes first. */
	@Override
	public int compareTo(Tile other){
		return dist - other.dist;
	}

	/** ToString implementation - shows basic info of Tile. Remains brief to be useful in debugging */
	public String toString(){
		return "(" + row + "," + col + "):" + terrain; 
	}

	/** Return the Manhattan (only udlr) distance from this to other *
	 */
	public int manhattanDistance(Tile other){
		return Math.abs(row - other.row) + Math.abs(col - other.col);
	}

	/** Returns the Direction to go from this to other,
	 * if they are bordering each other. Otherwise returns null
	 */
	public Direction directionTo(Tile other){
		if(row == other.row && col == other.col + 1) return Direction.LEFT;
		if(row == other.row && col == other.col - 1) return Direction.RIGHT;
		if(col == other.col && row == other.row + 1) return Direction.UP;
		if(col == other.col && row == other.row - 1) return Direction.DOWN;
		return null;
	}

	/** Returns the occupyingUnit, if there is one */
	public Unit getOccupyingUnit(){
		return occupyingUnit;
	}

	/** Adds the given unit to this tile
	 * @throws RuntimeException if this is already occupied
	 * @throws IllegalArgumentException if u is null */
	public void addOccupyingUnit(Unit u) throws RuntimeException {
		if(occupyingUnit != null)
			throw new RuntimeException ("Can't add unit to " + this + 
					", already occupied by " + occupyingUnit);
		if(u == null)
			throw new IllegalArgumentException("Can't add a null unit to " + this);
		occupyingUnit = u;
	}

	/** Removes the current unit */
	public void removeOccupyingUnit() throws RuntimeException{
		occupyingUnit = null;
	}

	/** Moves the given unit to the Tile other 
	 * @throws IllegalArgumentException if other is already occupied*/
	public void moveUnitTo(Tile other) throws IllegalArgumentException {
		other.addOccupyingUnit(occupyingUnit);
		removeOccupyingUnit();
	}

	/** Returns true iff there is an occupyingUnit */
	public boolean isOccupied(){
		return occupyingUnit != null;
	}

}
