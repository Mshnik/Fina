package unit.buildings;

import game.Player;
import board.Tile;
import unit.AttackType;
import unit.Building;
import unit.UnitStats;

/** A Well is a building that grants its owner mana per turn
 * as long as it is controlled.
 * @author MPatashnik
 *
 */
public class Well extends Building {
	/** Mana cost of constructing a well */
	public static final int COST = 50;
	
	/** ManaPerTurn generated per well */
	public static final int MANAPT = 5;
	
	/** Stats for maxHealth, defenses, range and visionRange of wells */
	public static final UnitStats STATS = 
			new UnitStats(800, MANAPT, 0, AttackType.NO_ATTACK, 0.05, 0.20, 0, 0);

	/** Constructor for a Well
	 * @param owner		- owner of this well
	 * @param tile		- tile to put this well on
	 * @throws RuntimeException
	 * @throws IllegalArgumentException
	 */
	public Well(Player owner, Tile tile) throws RuntimeException, IllegalArgumentException {
		super(owner, COST, tile, STATS);
	}

	/** Wells are drawn as well.png" */
	@Override
	public String getImgFilename() {
		return "well.png";
	}

}
