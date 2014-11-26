package unit;

import java.util.Collection;
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
	private Tile location;
	
	/** The board this unit is on. */
	public final Board board;

	/** The current health of this unit. If 0 or negative, this is dead */
	private int health;
	
	/** The current stats of this unit. These are updated whenever unitModifiers are added or removed */
	private UnitStats stats;
	
	/** A set of modifiers that are currently affecting this unit */
	private Collection<UnitModifier> modifiers;

	/** true iff this can still move this turn. Has an impact on how to draw this */
	private boolean canMove;

	/** true iff this can still fight this turn. Has an impact on how to draw this */
	private boolean canFight;

	/** Constructor for AbstractUnit
	 * @param owner - the player owner of this unit
	 * @param b - the board this unit exists within
	 * @param startingTile - the tile this unit begins the game on. Also notifies the tile of this.
	 */
	public AbstractUnit(AbstractPlayer owner, Board b, Tile startingTile, UnitStats stats){
		this.owner = owner;
		stats = new UnitStats(stats, null);
		board = b;
		location = startingTile;
		location.addOccupyingUnit(this);
		health = stats.maxHealth;
		modifiers = new LinkedList<UnitModifier>();
	}

	/** Returns the tile this Unit is on */
	public Tile getLocation(){
		return location;
	}
	
	//STATS
	/** Returns the current health of this unit */
	public int getHealth(){
		return health;
	}

	/** Returns true iff this is alive (health > 0) */
	public boolean isAlive(){
		return health > 0;
	}
	
	/** Returns the attack strength of this unit */
	public int getAttack(){
		return stats.attack;
	}
	
	/** Returns the counterattack strength of this unit */
	public int getCounterattack(){
		return stats.counterattack;
	}
	
	/** Returns the attack type of this unit */
	public AttackType getAttackType(){
		return stats.attackType;
	}
	
	/** Returns the physical defense of this unit */
	public int getPhysicalDefense(){
		return stats.physicalDefense;
	}
	
	/** Returns the ranged defense of this unit */
	public int getRangeDefense(){
		return stats.rangeDefense;
	}
	
	/** Returns the magic defense of this unit */
	public int getMagicDefense(){
		return stats.magicDefense;
	}
	
	
	/** Returns the Defense of this stats for the given attack type */
	public int getDefense(AttackType type){
		switch(type){
		case PHYSICAL: return getPhysicalDefense();
		case RANGE: return getRangeDefense();
		case MAGIC: return getMagicDefense();
		default: return 0;
		}
	}
	
	/** Returns the range of this unit */
	public int getRange(){
		return stats.range;
	}
	

	//MODIFIERS
	/** Returns the modifiers currently affecting this unit
	 * Pass-by-value, so editing the returned set doesn't do anything
	 */
	public Collection<UnitModifier> getModifiers(){
		return new LinkedList<UnitModifier>(modifiers);
	}
	
	/** Adds a new modifier to this unit. Also updates stats with the new modifiers,
	 * from its original base stats. */
	public void addModifier(UnitModifier m){
		modifiers.add(m);
		stats = new UnitStats(stats.base, modifiers);
	}
	
	/** Removes the given modifier from this unit. Also updates stats with new modifier
	 * from its original base stats. */
	public void removeModifier(UnitModifier m){
		modifiers.remove(m);
		stats = new UnitStats(stats.base, modifiers);
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
	
	/** Returns the mana cost of conjuring a new instance of this unit */
	public abstract int getManaCost();

	
	//FIGHTING
	/** Returns iff this can fight this turn */
	public boolean canFight(){
		return canFight;
	}
	
	/** Processes a pre-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid. */
	public abstract void preFight(AbstractUnit other);
	
	/** Processes a pre-counter-fight action that may be caused by modifiers.
	 * Still only called when the fight is valid, called after other.preFight().
	 * Only called if this will be able to counterAttack. */
	public abstract void preCounterFight(AbstractUnit other);

	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid.
	 * Notably, other or this may be dead when this is called.
	 */
	public abstract void postFight(AbstractUnit other);
	
	/** Processes a post-fight action that may be caused by modifiers.
	 * Only called when the fight is valid, called after other.postFight()
	 * Notably, other or this may be dead when this is called.
	 * Only called if this was able to counterAttack.
	 */
	public abstract void postCounterFight(AbstractUnit other);

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
		
		int room = location.manhattanDistance(other.getLocation()) - 1; //Account for melee = 0 range
		if(room > getRange())
			throw new IllegalArgumentException(this + " can't fight " + other + ", it is too far away.");

		//True if a counterAttack is happening, false otherwise.
		boolean counterAttack = other.isAlive() && other.owner.canSee(this) && room <= other.getRange();
		
		preFight(other);
		if(counterAttack) other.preCounterFight(this);

		//This attacks other
		other.health -= getAttack() - other.getDefense(getAttackType());
		
		//If other is still alive, can see the first unit, 
		//and this is within range, other counterattacks
		if(counterAttack){
			health -= other.getCounterattack() - getDefense(other.getAttackType());
			counterAttack = true;
		}

		//This can't attack this turn again
		canFight = false;
		
		postFight(other);
		if(counterAttack) other.postCounterFight(this);
		return ! other.isAlive();
	}

	//DRAWING
	/** Returns the name of the file that represents this unit as an image */
	public abstract String getImgFilename();

}
