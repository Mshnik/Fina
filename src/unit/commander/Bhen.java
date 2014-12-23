package unit.commander;

import game.Player;

import java.util.LinkedList;
import java.util.List;

import board.Tile;
import unit.Combatant;
import unit.Commander;
import unit.modifier.CustomModifier;
import unit.modifier.Modifier;
import unit.modifier.ModifierBundle;
import unit.stat.Stat;
import unit.stat.StatType;
import unit.stat.Stats;

public class Bhen extends Commander {

	/** Stats for maxHealth, defenses, range and visionRange of dummy commander */
	private static final Stats STATS = new Stats(
			new Stat(StatType.MAX_HEALTH, Commander.BASE_HEALTH),
			new Stat(StatType.MANA_PER_TURN, Commander.BASE_MANA_PT),
			new Stat(StatType.MAGIC_DEFENSE, 0.1),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.15),
			new Stat(StatType.SUMMON_RANGE, 2),
			new Stat(StatType.VISION_RANGE, 3),
			new Stat(StatType.MOVEMENT_TOTAL, 4),
			new Stat(StatType.GRASS_COST, 1),
			new Stat(StatType.WOODS_COST, 2),
			new Stat(StatType.MOUNTAIN_COST, 9999)
	);
	
	public Bhen(Player owner, Tile startingTile) throws RuntimeException, IllegalArgumentException {
		super("Bhen", owner, startingTile, STATS);
	}

	@Override
	public List<ModifierBundle> getCommanderGrantingModifiers() {
		return null;
	}

	@Override
	public void preMove(LinkedList<Tile> path) {}

	@Override
	public void postMove(LinkedList<Tile> path) {}

	@Override
	public void preCounterFight(Combatant other) {}

	@Override
	public void postCounterFight(Combatant other) {}

	@Override
	public String getImgFilename() {
		return null;
	}

}
