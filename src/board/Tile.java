package board;

/** A Tile is a single square in the board.
 * Maintains information about its location, what kind of tile it is, and what units are on it.
 * @author MPatashnik
 *
 */
public class Tile {

	/** The row of this tile in its board */
	public final int row;
	
	/** The column of this tile in its board */
	public final int col;
	
	/** The terrain type of this tile. */
	public final Terrain terrain;
	
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
	
}
