package unit.buildings;

import game.Player;
import board.Tile;
import unit.AttackType;
import unit.Building;
import unit.Stat;
import unit.StatType;
import unit.Unit;
import unit.UnitStats;

/** A Well is a building that grants its owner mana per turn
 * as long as it is controlled.
 * @author MPatashnik
 *
 */
public class Well extends Building {
	/** Name of a well in game */
	public static final String NAME = "Well";
	
	/** Mana cost of constructing a well */
	public static final int COST = 500;
	
	/** ManaPerTurn generated per well */
	public static final int MANAPT = 100;
	
	/** Stats for wells */
	private static final UnitStats STATS = new UnitStats(
			new Stat(StatType.MAX_HEALTH, 800),
			new Stat(StatType.MANA_PER_TURN, MANAPT),
			new Stat(StatType.MAGIC_DEFENSE, 0.15),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.05)
	);

	/** Constructor for a Well
	 * @param owner		- owner of this well
	 * @param tile		- tile to put this well on
	 */
	public Well(Player owner, Tile tile){
		super(owner, NAME, COST, tile, STATS);
	}

	/** Wells are drawn as well.png" */
	@Override
	public String getImgFilename() {
		return "well.png";
	}

	/** Returns a new well for the given owner, location */
	@Override
	public Unit clone(Player owner, Tile location) {
		return new Well(owner, location);
	}

}
