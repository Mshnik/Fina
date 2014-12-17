package unit.dummy;

import game.Player;

import java.util.LinkedList;

import board.Tile;
import unit.Combatant;
import unit.Commander;
import unit.Stat;
import unit.StatType;
import unit.UnitStats;

public class DummyCommander extends Commander {

	/** Stats for maxHealth, defenses, range and visionRange of dummy commander */
	private static final UnitStats STATS = new UnitStats(
			new Stat(StatType.MAX_HEALTH, Commander.BASE_HEALTH),
			new Stat(StatType.MANA_PER_TURN, Commander.BASE_MANA_PT),
			new Stat(StatType.MAGIC_DEFENSE, 0.1),
			new Stat(StatType.PHYSICAL_DEFENSE, 0.25),
			new Stat(StatType.SUMMON_RANGE, 2),
			new Stat(StatType.VISION_RANGE, 3),
			new Stat(StatType.MOVEMENT_TOTAL, 5),
			new Stat(StatType.GRASS_COST, 1),
			new Stat(StatType.WOODS_COST, 2),
			new Stat(StatType.MOUNTAIN_COST, 9999)
	);

	
	public DummyCommander(Player owner, Tile startingTile) {
		super("Dummy", owner, startingTile, STATS);
	}

	@Override
	public void refreshForTurn(){
		super.refreshForTurn();
		setHealth(getHealth() + 50, this);
	}

	@Override
	public void preMove(LinkedList<Tile> path) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postMove(LinkedList<Tile> path) {
		
	}
	
	@Override
	public void preCounterFight(Combatant other) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCounterFight(Combatant other) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getImgFilename() {
		// TODO Auto-generated method stub
		return "chrono.gif";
	}

}
