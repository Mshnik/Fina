package unit.buildings;

import game.Player;
import board.Tile;
import unit.Building;
import unit.Stat;
import unit.StatType;
import unit.Unit;
import unit.UnitStats;

public class Baracks extends Building {

	/** The in game name of a Baracks */
	public static final String NAME = "Baracks";
	
	/** Mana cost of constructing a baracks */
	public static final int COST = 800;
	
	/** Stats for maxHealth, defenses, range and visionRange of baracks */
	private static final UnitStats STATS = new UnitStats(
			new Stat(StatType.MAX_HEALTH, 1000),
			new Stat(StatType.MAGIC_DEFENSE, 0.1),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.25),
			new Stat(StatType.SUMMON_RANGE, 1),
			new Stat(StatType.VISION_RANGE, 1)
	);
	
	public Baracks(Player owner, Tile tile) throws RuntimeException, IllegalArgumentException {
		super(owner, NAME, COST, tile, STATS);
	}

	/** Baracks are able to summon new units */
	@Override
	public boolean canSummon(){
		return true;
	}
	
	/** Returns a new Baracks for the given owner, on the given location */
	@Override
	public Unit clone(Player owner, Tile location) {
		return new Baracks(owner, location);
	}

	/** Returns a the filename for the baracks image, in buildings image root */
	@Override
	public String getImgFilename() {
		return "tower.png";
	}

}
