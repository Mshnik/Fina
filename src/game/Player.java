package game;

import java.util.HashSet;

import board.*;
import unit.*;

/** An instance is a player (not the commander piece).
 * Extended to be either human controlled or AI.
 * @author MPatashnik
 *
 */
public abstract class Player {

	/** The Game this player is playing in */
	public final Game game;
	
	/** The Units this player controls */
	private HashSet<Unit> units;
	
	/** The commander belonging to this player */
	public final Commander commander;
	
	/** The Tiles in the board this player has vision of */
	private HashSet<Tile> visionCloud;
	
	/** The mana cap for this player */
	private int maxMana;
	
	/** The current mana level of this player */
	private int mana;
	
	/** The current level of this player.
	 * Integer part is the actual level, fraction portion is the exp towards next level.
	 */
	private double level;
	
	/** Returns true if it is this player's turn, false if some other player */
	public boolean isMyTurn(){
		return game.getCurrentPlayer() == this;
	}
	
	/** Return true iff the tile u occupies is in this Player's vision */
	public boolean canSee(Unit u){
		return visionCloud.contains(u.getLocation());
	}
	
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
	 * multiple times
	 */
	public void addExp(double exp){
		int oldLevel = getLevel();
		level += exp;
		for(int i = 0; i < getLevel() - oldLevel; i++){
			commander.levelUp();
		}
	}

	//TURN
	/** Called when it becomes this player's turn. Does start of turn processing. 
	 * 		- calls refresh on each unit (no particular order) */
	protected final void turnStart(){
		//Refresh for turn and add manaPerTurn
		for(Unit u : units){
			u.refreshForTurn();
			mana += u.getManaPerTurn();
		}
		
		//Check for mana > max, or mana < 0
		mana = Math.min(mana, maxMana);
		//If mana < 0, force player to choose units to sacrifice instead.
		if(mana < 0){
			//TODO
		}
	}
	
	/** Called when it becomes this player's turn to do things. Passes control to player.
	 * Shouldn't be recursive, and should terminate when it finishes doing things. */
	protected abstract void turn();
	
	/** Called when this player's turn ends. Does end of turn processing */
	protected final void turnEnd(){
		
	}
}
