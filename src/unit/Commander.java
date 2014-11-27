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

	/** Base level of health for lvl1 commanders */
	public static final int BASE_HEALTH = 10;
	
	/** Base amount of max health gained per level */
	public static final int SCALE_HEALTH = 2;
	
	/** Base level of max mana for lvl1 commanders */
	public static final int BASE_MANA = 15;
	
	/** Base amount of max mana gained per level */
	public static final int SCALE_MANA = 5;
	
	/** Base level of mana per turn for lvl1 commanders */
	public static final int BASE_MANA_PT = 5;
	
	/** Base amount of mana per turn gained per level */
	public static final int SCALE_MANA_PT = 1;
	
	/** The max health for this commander. Increases with level.
	 * Specifically, this means the maxHealth stat attribute is unused. */
	protected int maxHealth;
	
	/** The mana cap for this commander. Increases with level */
	protected int maxMana;
	
	/** The current mana level of this commander. Varies over the course of the game. */
	private int mana;
	
	/** The current mana per turn generation of this commander. Increases with level. */
	protected int manaPerTurn;
	
	/** The current level of this player.
	 * Integer part is the actual level, fraction portion is the exp towards next level.
	 */
	private double level;
	
	/** Constructor for Commanders
	 * @param owner			- player who owns this commander
	 * @param startingTile	- the tile in the board this commander occupies
	 * @param stats			- the stats of this commander.
	 * 							Notably, because of restrictions on commander,
	 * 							the maxHealth, attack, manaPerTurn, counterattack, and attackType
	 * 							Attributes are unused, because they are either unnecessary 
	 * 							or calculated elsewhere.
	 */
	public Commander(Player owner, Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);
		level = 1;
		recalculateScalingStats();
		setMana(getMaxMana());
		setHealth(getMaxHealth());
	}
	//RESTRICTIONS
	/** Restricted attack - has val 0. */
	@Override
	public int getAttack(){
		return 0;
	}
	
	/** Restricted counterattack - has val 0. */
	@Override
	public int getCounterattack(){
		return 0;
	}
	
	/** Commanders can't fight */
	public boolean canFight(){
		return false;
	}
	
	/** Restricted attackType - has val NO_ATTACK. */
	@Override
	public AttackType getAttackType(){
		return AttackType.NO_ATTACK;
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
	
	/** Returns the mana per turn this commander generates
	 * Overrides Unit.getMaxHealth so level can affect maxHealth.
	 * This does mean the stats.maxHealth attribute is unused for commanders */
	@Override
	public int getManaPerTurn(){
		return manaPerTurn;
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
	
	/** A basic scalingStats implementation.
	 * Consider using this, then making alterations.
	 */
	
	/** Re-calculates maxHealth, maxMana, and manaPerTurn off of level and other factors.
	 * Should start with base formulae based off of static constants
	 * 	stat = BASE_STAT + SCALE_STAT * (lvl - 1)
	 * If max health or mana increase this way, increase health and mana by
	 * same amount */
	public abstract void recalculateScalingStats();
	
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
		recalculateScalingStats();
	}
}
