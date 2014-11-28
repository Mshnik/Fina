package unit;

import java.util.LinkedList;


import game.Player;
import board.Tile;

/** Represents any thing that can be owned by a player.
 * Maintains its health and stats, and modifiers.
 * 
 * @author MPatashnik
 *
 */
public abstract class Unit{

	/** The player that owns this unit */
	public final Player owner;

	/** The tile this unit is on. Should not ever share a tile with
	 * another unit
	 */
	protected Tile location;

	/** The current health of this Unit. If 0 or negative, this is dead. Can't be above maxHealth */
	private int health;

	/** The current stats of this unit. These are updated whenever unitModifiers are added or removed.
	 * Should never be null, but may be empty */
	private UnitStats stats;
	
	/** A set of modifiers that are currently affecting this unit */
	private LinkedList<UnitModifier> modifiers;

	/** Constructor for Unit.
	 * Also adds this unit to the tile it is on as an occupant, and
	 * its owner as a unit that player owns
	 * @param owner - the player owner of this unit
	 * @param b - the board this unit exists within
	 * @param tile - the tile this unit begins the game on. Also notifies the tile of this.
	 */
	public Unit(Player owner, Tile tile, UnitStats stats){
		this.owner = owner;
		location = tile;
		location.addOccupyingUnit(this);
		this.stats = new UnitStats(stats, null);
		modifiers = new LinkedList<UnitModifier>();
		
		owner.addUnit(this);
	}
	
	/** Call at the beginning of every turn.
	 *  Can be overridden in subclasses, but those classes should call the super
	 *  version before doing their own additions.
	 * 		- ticks down modifiers and re-calculates stats, if necessary.
	 */
	public void refreshForTurn(){
		LinkedList<UnitModifier> deadModifiers = new LinkedList<UnitModifier>();
		for(UnitModifier m : modifiers){
			if(m.decRemainingTurns())
				deadModifiers.add(m);
		}
		if(! deadModifiers.isEmpty()){
			modifiers.removeAll(deadModifiers);
			stats = stats.modifiedWith(modifiers);
		}
	}
	
	/** Returns the tile this Unit is on */
	public Tile getLocation(){
		return location;
	}	
	
	/** Returns whether this can move this turn. Non-movable things should always return false *
	 */
	public abstract boolean canMove();
	
	/** Returns whether this can fight this turn. Non-fighting things should always return false *
	 */
	public abstract boolean canFight();

	//STATS
	/** Returns the max health of this unit */
	public int getMaxHealth(){
		return stats.getMaxHealth();
	}
	
	/** Returns the current health of this Unit */
	public int getHealth(){
		return health;
	}
	
	/** Sets the current health of this unit (alters it by difference)
	 * @see changeHealth(desired - current)
	 */
	protected void setHealth(int newHealth){
		changeHealth(newHealth - health);
	}

	/** Alters the current health of this unit. Maxes health at maxHealth.
	 * if health <= 0 because of this call, calls died().
	 * @param deltaHealth - amount to change health by.
	 */
	protected void changeHealth(int deltaHealth){
		health += deltaHealth;
		health = Math.min(health, getMaxHealth());
		if(health <= 0) died();
	}
	
	
	/** Returns true iff this is alive (health > 0) */
	public boolean isAlive(){
		return health > 0;
	}
	
	/** Called when a change in health causes this to die.
	 * Can be overriden in subclasses to add additional behavior,
	 * but this method should be called somewhere in that overriden method */
	protected void died(){
		owner.removeUnit(this);
	}
	
	/** Returns the attack strength of this unit. 0 if this is not a combatant. */
	public int getAttack(){
		return stats.getAttack();
	}

	/** Returns the attack type of this unit. NO_ATTACK if this is not a combatant. */
	public AttackType getAttackType(){
		return stats.getAttackType();
	}

	/** Returns the physical defense of this unit */
	public int getPhysicalDefense(){
		return stats.getPhysicalDefense();
	}

	/** Returns the magic defense of this unit */
	public int getMagicDefense(){
		return stats.getMagicDefense();
	}


	/** Returns the Defense of this stats for the given attack type.
	 * Infinite defense for NO_ATTACK type damage, no defense for TRUE damage */
	public int getDefense(AttackType type){
		switch(type){
		case NO_ATTACK: return Integer.MAX_VALUE;
		case PHYSICAL: return getPhysicalDefense();
		case MAGIC: return getMagicDefense();
		case TRUE: return 0;
		default: return 0;
		}
	}
	
	/** Returns true if this is a ranged unit (range > 0)
	 * false if this is melee (range == 0)
	 */
	public boolean isRanged(){
		return getRange() > 0;
	}

	/** Returns the range of this unit.
	 * If this is a combatant, this is its attack range.
	 * If this is a summoner, this is its summon range.  */
	public int getRange(){
		return stats.getRange();
	}
	
	/** Returns the vision range of this unit. */
	public int getVisionRange(){
		return stats.getVisionRange();
	}
	
	/** Returns the mana per turn this unit costs/generates */
	public int getManaPerTurn(){
		return stats.getManaPerTurn();
	}

	/** Returns true if this is a generator (manaPerTurn > 0) */
	public boolean isGenerator(){
		return getManaPerTurn() > 0;
	}
	
	/** Returns true if this is a burden (manaPerTurn < 0) */
	public boolean isBurden(){
		return getManaPerTurn() < 0;
	}
	
	//MODIFIERS
	/** Returns the modifiers currently affecting this unit
	 * Pass-by-value, so editing the returned set doesn't do anything
	 */
	public LinkedList<UnitModifier> getModifiers(){
		return new LinkedList<UnitModifier>(modifiers);
	}
	
	/** Adds a new modifier to this unit. Also updates stats with the new modifiers,
	 * from its original base stats. */
	public void addModifier(UnitModifier m){
		modifiers.add(m);
		stats = stats.modifiedWith(modifiers);
	}
	
	/** Removes the given modifier from this unit. Also updates stats with new modifier
	 * from its original base stats. */
	public void removeModifier(UnitModifier m){
		modifiers.remove(m);
		stats = stats.modifiedWith(modifiers);
	}
	
	//FIGHTING
	/** Processes a pre-counter-fight action (this was attacked) 
	 * that may be caused by modifiers.
	 * Still only called when the fight is valid, called after other.preFight().
	 * Only called if this will be able to counterAttack. */
	public abstract void preCounterFight(Combatant other);
	
	/** Processes a post-counter-fight (this was attacked) 
	 * action that may be caused by modifiers.
	 * Only called when the fight is valid, called after other.postFight()
	 * Only called if this was able to counterAttack and is still alive.
	 */
	public abstract void postCounterFight(Combatant other);
	
	//DRAWING
	/** Returns the name of the file that represents this unit as an image */
	public abstract String getImgFilename();
}
