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
	
	/** Returns true if it is this player's turn, false if some other player */
	public boolean isMyTurn(){
		return game.getCurrentPlayer() == this;
	}
	
	/** Return true iff the tile u occupies is in this Player's vision */
	public boolean canSee(Unit u){
		return visionCloud.contains(u.getLocation());
	}
	
	/** Returns the current mana for this player */
	public int getMana(){
		return commander.getMana();
	}
	
	/** Returns the maximum mana for this player */
	public int getMaxMana(){
		return commander.getMaxMana();
	}
	
	/** Returns the current level (not exp) of this player */
	public int getLevel(){
		return commander.getLevel();
	}

	//TURN
	/** Called when it becomes this player's turn. Does start of turn processing. 
	 * 		- calls refresh on each unit (no particular order) */
	protected final void turnStart(){
		//Refresh for turn and add manaPerTurn
		for(Unit u : units){
			u.refreshForTurn();
			commander.addMana(u.getManaPerTurn());
		}
		
		
		//If mana < 0, force player to choose units to sacrifice instead.
		if(commander.getMana() < 0){
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
