package board;

import gui.PathSelector;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import unit.AbstractUnit;

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
	
	/** Return the tile in the given direction from this tile.
	 * If oob, returns null or if direction invalid.
	 */
	public Tile getTileInDirection(Tile t, Direction d){
		try{
			switch(d){
			case DOWN: return getTileAt(t.row + 1, t.col);
			case LEFT: return getTileAt(t.row, t.col - 1);
			case RIGHT:return getTileAt(t.row, t.col + 1);
			case UP:   return getTileAt(t.row - 1, t.col);
			default:   return null;
			}
		}catch(IllegalArgumentException e){
			return null;
		}
	}
	/** Returns an array of neighbors of the given tile, null for oob spaces.
	 * Returns in order [left, up, right, down] */
	public Tile[] getTileNeighbors(Tile t){
		Tile[] neighbors = {
				getTileInDirection(t, Direction.LEFT),
				getTileInDirection(t, Direction.UP),
				getTileInDirection(t, Direction.RIGHT),
				getTileInDirection(t, Direction.DOWN)
		};
		return neighbors;
	}

	
	/** Returns the set of tiles the given path selector could move to from its
	 * current location with its movement cap.
	 */
	public HashSet<Tile> getMovementCloud( PathSelector ps){
		AbstractUnit unit = ps.unit;
		//Initialize
		for(Tile t : this){
			t.dist = Integer.MIN_VALUE;
			t.prev = null;
		}
		
		Tile start = ps.getPath().getLast();
		
			//Uses dist to hold remainingDistance as possible.
		start.dist = unit.getConvertedMovement() - ps.getTotalCost(); 
		
		// frontier sorts with higher distance earlier
		PriorityQueue<Tile> frontier = new PriorityQueue<Tile>(1, 
				new Comparator<Tile>(){
					@Override
					/** Use inverse of regular comparison (higher distance first) */
					public int compare(Tile o1, Tile o2) {
						return - o1.compareTo(o2);
					}
		});
		frontier.add(start);
		HashSet<Tile> settled = new HashSet<Tile>();
		
		//Iteration
		while(! frontier.isEmpty()){
			Tile current = frontier.poll();
			settled.add(current);
			for(Tile neighbor : getTileNeighbors(current)){
				if(neighbor != null){
					int nDist = current.dist - unit.getMovementCost(neighbor.terrain);
					if(nDist >= 0 && 
							(! neighbor.isOccupied() || neighbor.getOccupyingUnit().owner == unit.owner)
					){
						neighbor.dist = nDist;
						frontier.remove(neighbor);
						frontier.add(neighbor);
					}
				}
			}
		}
		
		//Just return settled tiles as possible movement.
		return settled;
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

