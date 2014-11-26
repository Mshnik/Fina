package unit;

import game.AbstractPlayer;
import board.Tile;

/** Represents a commander for a player. Each player should have one.
 * @author MPatashnik
 *
 */
public abstract class Commander extends MovingUnit {

	public Commander(AbstractPlayer owner, Tile startingTile, UnitStats stats) {
		super(owner, startingTile, stats);
		
		if(! stats.attackType.equals(AttackType.NO_ATTACK))
			throw new IllegalArgumentException("Commander " + this + " must have attackType NO_ATTACK");
	}

}
