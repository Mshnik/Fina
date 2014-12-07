package unit;

import java.util.LinkedList;


import board.*;
import game.Player;

/** Represents a unit that is able to move around the board.
 * 
 * @author MPatashnik
 *
 */
public abstract class MovingUnit extends Unit{

	/** Image root for moving unit (people) files */
	public static final String IMAGE_ROOT = "unit/";
	
	/** true iff this can still move this turn. Has an impact on how to draw this */
	private boolean canMove;

	/** Constructor for MovingUnit.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns,
	 * Subtracts manaCost from the owner, but throws a runtimeException if 
	 * the owner doesn't have enough mana.
	 * @param owner - the player owner of this unit
	 * @param name - the name of this unit
	 * @param manaCost - the cost of summoning this unit. Should be a positive number.
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 * @param stats - the base unmodified stats of this unit.
	 */
	public MovingUnit(Player owner, String name, int manaCost, Tile startingTile, UnitStats stats)
			throws RuntimeException, IllegalArgumentException {
		super(owner, name, manaCost, startingTile, stats);	
	}
	
	/** Call at the beginning of every turn.
	 *  Can be overridden in subclasses, but those classes should call the super
	 *  version before doing their own additions.
	 * 		- ticks down modifiers and re-calculates stats, if necessary.
	 * 		- refreshes canMove
	 */
	@Override
	public void refreshForTurn(){
		super.refreshForTurn();
		canMove = true;
	}
	
	//MOVEMENT
	/** Returns iff this can move */
	public boolean canMove(){
		return canMove;
	}

	/** Returns the total converted movement this unit can take in a turn */
	public abstract int getMovementCap();

	/** Returns the movement cost of traveling terrain t, infinity if this unit
	 * can't travel the given terrain
	 */
	public abstract int getMovementCost(Terrain t);

	/** Processes a pre-move action that may be caused by modifiers.
	 * Still only called when the move is valid. */
	public abstract void preMove(LinkedList<Tile> path);

	/** Processes a post-move action that may be caused by modifiers.
	 * Only called when the move is valid.
	 */
	public abstract void postMove(LinkedList<Tile> path);
	
	/** Attempts to move this unit along the given path.
	 * @param path - the path to travel, where the first of path is location, 
	 * 	last is the desired destination, and whole path is manhattan contiguous.
	 * @return true iff the movement happens.
	 * @throws RuntimeException if...
	 * 		- this can't move this turn (already moved)
	 * @throws IllegalArgumentException if...
	 * 		- the first tile isn't location
	 * 		- The ending tile is occupied
	 * 		- the total cost of movement exceeds this' movement total
	 */
	public final boolean move(LinkedList<Tile> path) throws IllegalArgumentException, RuntimeException{
		if(! canMove)
			throw new RuntimeException(this + " can't move again this turn");
		if(path.get(0) != location)
			throw new IllegalArgumentException(this + " can't travel path " + path + ", it is on " + location);
		
		int cost = 0;
		for(Tile t : path){
			cost += getMovementCost(t.terrain);
		}
		
		if(cost > getMovementCap())
			throw new IllegalArgumentException(this + " can't travel path " + path + ", total cost of " + cost + " is too high");
	
		preMove(path);
		
		//movement probably ok. If end is occupied, tile move call will throw
		location.moveUnitTo(path.getLast());
		location = path.getLast();
		
		canMove = false;
		owner.refreshVisionCloud();
		
		postMove(path);
		
		return true;
	}

}
