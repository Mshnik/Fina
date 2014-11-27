package unit;

import game.Player;
import board.Tile;

/** Represents a commander for a player. Each player should have one.
 * 
 * Commanders have mana and a level to maintain
 * @author MPatashnik
 *
 */
public abstract class Commander extends MovingUnit {

	/** The max health for this commander. Increases with level.
	 * Specifically, this means the maxHealth stat attribute is unused. */
	private int maxHealth;
	
	/** The mana cap for this commander. Increases with level */
	private int maxMana;
	
	/** The current mana level of this player */
	private int mana;
	
	/** The current level of this player.
	 * Integer part is the actual level, fraction portion is the exp towards next level.
	 */
	private double level;
	
	public Commander(Player owner, Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Commander " + this + " must have attackType NO_ATTACK");
		level = 1;
		recalculateMaxHealthMana();
		setMana(getMaxMana());
		setHealth(getMaxHealth());
	}

	//HEALTH and MANA
	/** Returns the max health of this commander.
	 * Overrides Unit.getMaxHealth so level can affect maxHealth.
	 * This does mean the stats.maxHealth attribute is unused for commanders */
	@Override
	public int getMaxHealth(){
		return maxHealth;
	}
	
	/** Returns the current mana of this commander */
	public int getMana(){
		return mana;
	}
	
	/** Returns the max mana of this commander. */
	public int getMaxMana(){
		return maxMana;
	}
		
	protected boolean setMana(int newMana){
		return addMana(newMana - mana);
	}
	
	/** adds the given amount of mana, capping at maxMana.
	 * @returns true if this drains mana (mana < 0), false otherwise
	 */
	public boolean addMana(int deltaMana){
		mana = Math.min(mana + deltaMana, maxMana);
		return mana < 0;
	}
	
	/** Re-calculates maxHealth and maxMana off of level and other factors.
	 * Should start with base formulae:
	 * 		- health = 8 + 2 * lvl
	 * 		- mana 	 = 15 + 5 * lvl
	 * If max health or mana increase this way, increase health and mana by
	 * same amount */
	public abstract void recalculateMaxHealthMana();
	
	//LEVEL
	/** Returns the current level of this player
	 * Integer part is the actual level, fraction portion is the exp towards next level.
	 */
	public double getLevelAndExp(){
		return level;
	}
	
	/** Returns the actual level of this player (rounded down to nearest int) */
	public int getLevel(){
		return (int)level;
	}
	
	/** Adds the given amount of experience to this player, then calls levelUp on
	 * commander if this leveled up. Will call levelUp multiple times if this leveled up
	 * multiple times.
	 * 
	 * Scales the input by dividing by the current level. If gaining experience would
	 * cause multiple level-ups, adds one level at a time so scaling happens correctly
	 */
	public void addExp(double exp){
		//If enough to cause a levelup, move to the next rounded level before doing more.
		double toNextLevel = (getLevel() + 1 - level) * getLevel();
		if(exp >= toNextLevel){
			//Deduct just enough exp to get to next level
			exp -= toNextLevel;
			//Increase level to exactly next level and call levelup
			level = getLevel()+1;
			levelUp();
			//Recurse to process rest of exp
			addExp(exp);	
		} else{
			//Otherwise, just scale exp and add it.
			level += exp / getLevel();
		}
	}
	
	/** Called by Player class when this levels up.
	 * Can be overriden by subclass to cause affect when leveled up, 
	 * but this super method should be called first */
	public void levelUp(){
		recalculateMaxHealthMana();
	}
}
