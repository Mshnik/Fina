package unit;

import game.AbstractPlayer;

import board.Tile;

/** Represents a non-fighting combatant that generates mana
 * for the player that owns it.
 * @author MPatashnik
 *
 */
public abstract class Pawn extends MovingUnit {

	/** Precondition : stats.attackType is AttackType.NO_ATTACK
	 * @see Unit(AbstractPlayer owner, Tile tile, UnitStats stats) */
	public Pawn(AbstractPlayer owner, Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Pawn " + this + " must have attackType NO_ATTACK");
	}

}
