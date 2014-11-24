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
	
}
