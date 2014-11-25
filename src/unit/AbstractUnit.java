package unit;

import java.util.HashSet;
import java.util.LinkedList;

import board.*;
import game.AbstractPlayer;

/** Represents a single unit on the board.
 * Should be extended to fill in stats, abilities.
 * 
 * A Unit is responsible for keeping track of its location,
 * its stats, and other things..
 * @author MPatashnik
 *
 */
public abstract class AbstractUnit {

	/** The player that owns this unit */
	public final AbstractPlayer owner;

	/** The tile this unit is on. Should not ever share a tile with
	 * another unit
	 */
	private Tile occupiedTile;
	
	/** The board this unit is on. */
	public final Board board;

	/** The current health of this unit. If 0 or negative, this is dead */
	private int health;

	/** A set of modifiers that are currently affecting this unit */
	private HashSet<UnitModifier> modifiers;

	/** true iff this can still move this turn. Has an impact on how to draw this */
	private boolean canMove;

	/** true iff this can still fight this turn. Has an impact on how to draw this */
	private boolean canFight;

	/** Constructor for AbstractUnit
	 * @param owner - the player owner of this unit
	 * @param b - the board this unit exists within
	 * @param startingTile - the tile this unit begins the game on. Also notifies the tile of this.
	 */
	public AbstractUnit(AbstractPlayer owner, Board b, Tile startingTile){
		this.owner = owner;
		board = b;
		occupiedTile = startingTile;
		occupiedTile.addOccupyingUnit(this);
		health = getMaxHealth();
		modifiers = new HashSet<UnitModifier>();
	}

	/** Returns the tile this Unit is on */
	public Tile getOccupiedTile(){
		return occupiedTile;
	}

	/** Returns the current health of this unit */
	public int getHealth(){
		return health;
	}

	/** Returns true iff this is alive (health > 0) */
	public boolean isAlive(){
		return health > 0;
	}

	/** Returns the modifiers currently affecting this unit
	 * Pass-by-value, so editing the returned set doesn't do anything
	 */
	public HashSet<UnitModifier> getModifiers(){
		return new HashSet<UnitModifier>(modifiers);
	}

	/** Returns iff this can move */
	public boolean canMove(){
		return canMove;
	}

	/** Returns iff this can attack */
	public boolean canAttack(){
		return canFight;
	}

	/** Sets the value of canMove and canFight to true */
	public void refreshMoveAndAttack(){
		canMove = true;
		canFight = true;
	}

	/** Returns the total converted movement this unit can take in a turn */
	public abstract int getConvertedMovement();

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
	 * @param path - the path to travel, where the first of path is occupiedTile, 
	 * 	last is the desired destination, and whole path is manhattan contiguous.
	 * @return true iff the movement happens.
	 * @throws RuntimeException if...
	 * 		- this can't move this turn (already moved)
	 * @throws IllegalArgumentException if...
	 * 		- the first tile isn't occupiedTile
	 * 		- The ending tile is occupied
	 * 		- the total cost of movement exceeds this' movement total
	 */
	public final boolean move(LinkedList<Tile> path) throws IllegalArgumentException, RuntimeException{
		if(! canMove)
			throw new RuntimeException(this + " can't move again this turn");
		if(path.get(0) != occupiedTile)
			throw new IllegalArgumentException(this + " can't travel path " + path + ", it is on " + occupiedTile);
		
		int cost = 0;
		for(Tile t : path){
			cost += getMovementCost(t.terrain);
		}
		
		if(cost > getConvertedMovement())
			throw new IllegalArgumentException(this + " can't travel path " + path + ", total cost of " + cost + " is too high");
	
		preMove(path);
		
		//movement probably ok. If end is occupied, tile move call will throw
		occupiedTile.moveUnitTo(path.getLast());
		occupiedTile = path.getLast();
		
		canMove = false;
		
		postMove(path);
		
		return true;
	}
	
	/** Returns the mana cost of conjuring a new instance of this unit */
	public abstract int getManaCost();
	
	/** Returns the MaxHealth of this unit.
	 * Should process modifiers in calculation. */
	public abstract int getMaxHealth();

	/** Returns the Attack power of this unit.
	 * Should process modifiers in calculation. */
	public abstract int getAttack();

	/** Returns the CounterAttack power of this unit. */
	public abstract int getCounterAttack();

	/** Returns the attack type of this unit.
	 * Should process modifiers in calculation. */
	public abstract AttackType getAttackType();

	/** Returns the Defense of this unit for the given attack type */
	public int getDefense(AttackType type){
		switch(type){
		case PHYSICAL: return getPhysicalDefense();
		case RANGE: return getRangeDefense();
		case MAGIC: return getMagicDefense();
		default: return 0;
		}
	}

	/** Returns the Physical Defense of this unit.
	 * Should process modifiers in calculation. */
	public abstract int getPhysicalDefense();

	/** Returns the Ranged Defense of this unit.
	 * Should process modifiers in calculation. */
	public abstract int getRangeDefense();

	/** Returns the Magic Defense of this unit.
	 * Should process modifiers in calculation. */
	public abstract int getMagicDefense();

	/** Returns the attack range (number of squares between this and other unit
	 * to attack, melee = 0) of this unit.
	 * Should process modifiers in calculation.
	 */
	public abstract int getRange();

	/** Processes a pre-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid. */
	public abstract void preFight(AbstractUnit other);

	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid.
	 * Notably, other or this may be dead when this is called.
	 */
	public abstract void postFight(AbstractUnit other);

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
	public final boolean fight(AbstractUnit other) throws IllegalArgumentException, RuntimeException{
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
		
		int room = occupiedTile.manhattanDistance(other.getOccupiedTile()) - 1; //Account for melee = 0 range
		if(room > getRange())
			throw new IllegalArgumentException(this + " can't fight " + other + ", it is too far away.");

		preFight(other);

		//This attacks other
		other.health -= getAttack() - other.getDefense(getAttackType());
		
		//If other is still alive, can see the first unit, 
		//and this is within range, other counterattacks
		if(other.isAlive() && other.owner.canSee(this) && room <= other.getRange()){
			health -= other.getCounterAttack() - getDefense(other.getAttackType());
		}

		//This can't attack this turn again
		canFight = false;
		
		postFight(other);
		return ! other.isAlive();
	}

	/** Returns the name of the file that represents this unit as an image */
	public abstract String getImgFilename();

}
