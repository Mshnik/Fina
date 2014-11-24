package board;

import java.util.Iterator;

/** A Board represents the whole board state for the game as a matrix of tiles and other
 * information. <br><br>
 * 
 * 
 * @author MPatashnik
 *
 */
public class Board implements Iterable<Tile>{
	
	/** The tiles that make up this board. Must be rectangular (non-jagged) */
	private Tile[][] tiles;
	
	/** Construct a simple board of just terrain
	 * Throws IllegalArgumentException if input array is jagged. */
	public Board(Terrain[][] terrain) throws IllegalArgumentException{
		tiles = new Tile[terrain.length][terrain[0].length];
		for(int i = 0; i < terrain.length; i++){
			
			if(terrain[i].length != terrain[0].length)
				throw new IllegalArgumentException("Jagged Array passed into board constructor " 
													+ terrain);
			
			for(int j = 0; j < terrain[i].length; j++){
				tiles[i][j] = new Tile(i,j,terrain[i][j]);
			}
		}
	}
	
	/** Returns the height (# rows) of this Board */
	public int getHeight(){
		return tiles.length;
	}
	
	/** Returns the width (# columns) of this Board */
	public int getWidth(){
		return tiles[0].length;
	}
	
	/** Returns the tile at the given index, throws IllegalArgumentException */
	public Tile getTileAt(int r, int c) throws IllegalArgumentException{
		if(r < 0 || r >= tiles.length || c < 0 || c >= tiles[r].length)
			throw new IllegalArgumentException("Can't get tile from " + this + 
					" at index (" + r + "," + c + ")" );
		
		return tiles[r][c];
	}

	@Override
	/** Returns an iterator over the tiles in this Board */
	public Iterator<Tile> iterator() {
		return new BoardIterator();
	}
	
	/** An iterator over boards - goes along rows for its iteration */
	private class BoardIterator implements Iterator<Tile>{

		private int r;
		private int c;
		
		/** Constructs a new board iterator, with row and column set to 0 */
		private BoardIterator(){
			r = 0;
			c = 0;
		}
		
		@Override
		/** Return true iff r < tiles.length && c < tiles[r].length */
		public boolean hasNext() {
			return r < tiles.length && c < tiles[r].length;
		}

		@Override
		/** Gets the current tile, then advances one to the right.
		 * If this goes off the row, goes to the next row and resets the column counter
		 */
		public Tile next() {
			Tile t = getTileAt(r,c);
			c++;
			if(c == getWidth()){
				r++;
				c = 0;
			}
			return t;
		}

		@Override
		/** Not supported - do not call */
		public void remove() {
			throw new RuntimeException("Remove Operation Not Supported in Board Iterators");
		}
		
	}

	
	
}

