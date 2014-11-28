package unit;

import game.Player;
import board.Tile;

/** Represents a Building on the board, controllable by a player */
public abstract class Building extends Unit {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(Player owner, Tile tile, UnitStats stats) */
	public Building(Player owner, Tile tile, UnitStats stats) throws IllegalArgumentException {
		super(owner,tile, stats);
	}
	
	//RESTRICTIONS
	/** Restricted attack - has val 0. */
	@Override
	public int getAttack(){
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

}
