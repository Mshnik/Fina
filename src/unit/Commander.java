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

	/** The mana cap for this player */
	private int maxMana;
	
	/** The current mana level of this player */
	private int mana;
	
	/** The current level of this player.
	 * Integer part is the actual level, fraction portion is the exp towards next level.
	 */
	private double level;
	
	
	public Commander(Player owner, Tile startingTile, UnitStats stats, int maxMana) {
		super(owner, startingTile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Commander " + this + " must have attackType NO_ATTACK");
		
		this.maxMana = maxMana;
		mana = maxMana;
		level = 1;
	}

	/** Returns the current mana of this commander */
	public int getMana(){
		return mana;
	}
	
	/** Returns the max mana of this commander. */
	public int getMaxMana(){
		return maxMana;
	}
	
	/** adds the given amount of mana, capping at maxMana.
	 * @returns true if this drains mana (mana < 0), false otherwise
	 */
	public boolean addMana(int deltaMana){
		mana = Math.min(mana + deltaMana, maxMana);
		return mana < 0;
	}
	
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
	
	/** Called by Player class when this levels up */
	public abstract void levelUp();
}
