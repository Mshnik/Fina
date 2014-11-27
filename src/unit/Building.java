package unit;

import game.Player;
import board.Tile;

/** Represents a Building on the board, controllable by a player */
public abstract class Building extends Unit {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(Player owner, Tile tile, UnitStats stats) */
	public Building(Player owner, Tile tile, UnitStats stats) throws IllegalArgumentException {
		super(owner,tile, stats);
		
		if(! stats.getAttackType().equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Building " + this + " must have attackType NO_ATTACK");
	}

}
