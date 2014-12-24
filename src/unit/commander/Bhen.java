package unit.commander;

import game.Player;

import java.util.LinkedList;

import board.MPoint;
import board.Tile;
import unit.Combatant;
import unit.Commander;
import unit.ability.Ability;
import unit.ability.ModifierAbility;
import unit.modifier.CustomModifier;
import unit.modifier.ModifierBundle;
import unit.modifier.StatModifier;
import unit.modifier.StatModifier.ModificationType;
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
	
	/** Ability names */
	public static String[][] ABILITY_NAMES = {
		{"Battle Fury"},
		{"Enrage"}
	};
	
	/** Battle Fury Ability */
	private final Ability BATTLE_FURY = new ModifierAbility(ABILITY_NAMES[0][0], 0, this, 0, true, true, false, null, 
			new ModifierBundle(
					new CustomModifier(ABILITY_NAMES[0][0], "Units gain 2 movement after killing another unit", 
							2, Integer.MAX_VALUE, false, false, false, true)
			)
	  );
	
	/** Enrage Ability */
	private final Ability ENRAGE = new ModifierAbility(ABILITY_NAMES[1][0], 200, this, 0, true, true, false, 
			MPoint.ORIGIN.radialCloud(2), 
			new ModifierBundle(
					new StatModifier(ABILITY_NAMES[1][0], 1, false, StatType.MOVEMENT_TOTAL, ModificationType.ADD, 4),
					new StatModifier(ABILITY_NAMES[1][0], 1, false, StatType.ATTACK, ModificationType.ADD, 250)
			)
	  );
	
	/** Ability Choices */
	public final Ability[][] ABILITIES = {
		{BATTLE_FURY},
		{ENRAGE}
	};
	
	public Bhen(Player owner, Tile startingTile) throws RuntimeException, IllegalArgumentException {
		super("Bhen", owner, startingTile, STATS);
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
		return "link.png";
	}

	@Override
	public Ability[] getPossibleAbilities(int level) {
		return ABILITIES[level - 1];
	}

}
