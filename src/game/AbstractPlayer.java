package game;

import unit.AbstractUnit;

/** An instance is a player (not the commander piece).
 * Extended to be either human controlled or AI.
 * @author MPatashnik
 *
 */
public abstract class AbstractPlayer {

	
	/** Return true iff the tile u occupies is in this Player's vision */
	public boolean canSee(AbstractUnit u){
		return false;
	}
}
