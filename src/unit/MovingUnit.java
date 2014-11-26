package unit;

import java.util.LinkedList;

import board.*;
import game.AbstractPlayer;

/** Represents a unit that is able to fight and move on the board.
 * Should be extended to fill in stats, abilities.
 * 
 * A Unit is responsible for keeping track of its location,
 * its stats, and other things..
 * @author MPatashnik
 *
 */
public abstract class Combatant extends Unit{

	/** true iff this can still move this turn. Has an impact on how to draw this */
	private boolean canMove;

	/** true iff this can still fight this turn. Has an impact on how to draw this */
	private boolean canFight;

	/** Constructor for Combatant
	 * @param owner - the player owner of this unit
	 * @param b - the board this unit exists within
	 * @param startingTile - the tile this unit begins the game on. Also notifies the tile of this.
	 */
	public Combatant(AbstractPlayer owner, Board b, Tile startingTile, UnitStats stats){
		super(owner, b, startingTile, stats);	
	}
	
	/** Call at the beginning of every turn.
	 *  Can be overridden in subclasses, but those classes should call the super
	 *  version before doing their own additions.
	 * 		- ticks down modifiers and re-calculates stats, if necessary.
	 * 		- refreshes canMove and canFight
	 */
	@Override
	public void refreshForTurn(){
		super.refreshForTurn();
		canMove = true;
		canFight = true;
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
		
		postMove(path);
		
		return true;
	}
	
	//FIGHTING
	/** Returns iff this can fight this turn */
	public boolean canFight(){
		return canFight;
	}
	
	/** Processes a pre-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid. */
	public abstract void preFight(Unit other);
	
	/** Processes a pre-counter-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid, called after other.preFight().
	 * Only called if this will be able to counterAttack. */
	public abstract void preCounterFight(Combatant other);

	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid and this is still alive.
	 */
	public abstract void postFight(Unit other);
	
	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid, called after other.postFight()
	 * Only called if this was able to counterAttack and is still alive.
	 */
	public abstract void postCounterFight(Combatant other);

	/** Causes this unit to fight the given unit.
	 * With this as the attacker and other as the defender.
	 * This will cause the health of the other to change
	 * @throws RuntimeException if...
	 * 		- this is dead
	 * 		- this can't attack currently
	 * @throws IllegalArgumentException for invalid fight when...
	 * 		- other is dead
	 * 		- both units belong to the same player
	 * 		- other is out of the range of this 
	 * 		- this' owner can't see other
	 * @return true iff other is killed because of this action
	 **/
	public final boolean fight(Combatant other) throws IllegalArgumentException, RuntimeException{
		if(! isAlive()) 
			throw new RuntimeException (this + " can't fight, it is dead.");
		if(! other.isAlive()) 
			throw new IllegalArgumentException(other + " can't fight, it is dead.");
		if(owner == other.owner) 
			throw new IllegalArgumentException(this + " can't fight " + other + ", they both belong to " + owner);
		if(! canFight)
			throw new RuntimeException(this + " can't fight again this turn");
		if(! owner.canSee(other))
			throw new IllegalArgumentException(owner + " can't see " + other);
		
		int room = location.manhattanDistance(other.getLocation()) - 1; //Account for melee = 0 range
		if(room > getRange())
			throw new IllegalArgumentException(this + " can't fight " + other + ", it is too far away.");

		//True if a counterAttack is happening, false otherwise.
		boolean counterAttack = other.isAlive() && other.owner.canSee(this) && room <= other.getRange();
		
		preFight(other);
		if(counterAttack) other.preCounterFight(this);

		//This attacks other
		other.changeHealth(- (getAttack() - other.getDefense(getAttackType())));
		
		//If other is still alive, can see the first unit, 
		//and this is within range, other counterattacks
		if(counterAttack){
			changeHealth(- (other.getCounterattack() - getDefense(other.getAttackType())));
			counterAttack = true;
		}

		//This can't attack this turn again
		canFight = false;
		
		if(isAlive()) postFight(other);
		if(other.isAlive() && counterAttack) other.postCounterFight(this);
		return ! other.isAlive();
	}

}
