package model.unit.combatant;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Combatant;
import model.unit.Unit;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

import util.TextIO;

/**
 * A FileUnit is a combatant read in from file.
 * Full constructors are private,
 * but the class maintains a static collection of units
 * that can be cloned for either player.
 * 
 * @author MPatashnik
 *
 */
public final class FileCombatant extends Combatant {

	/**
	 * The avaliable combatants read into storage during initialization.
	 */
	private static final List<FileCombatant> FILE_COMBATANTS;

	/** File containing the units, in csv format */
	private static final File UNITS_FILE = new File("game/units.csv");

	/* Initializes the combatants available */
	static{
		LinkedList<FileCombatant> units = new LinkedList<>();
		try{
			String[] unitLines = TextIO.read(UNITS_FILE).split("\\n");

			for(String line : unitLines){
				String[] comps = line.split(",");
				if(comps.length == 0) continue;	//Blank line.
				if(comps[0].equals("Name")) continue; //Header line.

				try{
					// Column layout of file:
					// 0: Name (String)
					// 1: Image filename (String)
					// 2: Level (Int 1-4)
					// 3: Cost (Int)
					// 4: Type (F/T/A/R with combinations)
					// 5: Health (Int)
					// 6: Min Attack (Int)
					// 7: Max Attack (Int)
					// 8: Attack Range (Int or --)
					// 9: Move (Int)
					// 10: Vision (Int)

					String name = comps[0];
					String img = comps[1];
					int level = Integer.parseInt(comps[2]);
					int manaCost = Integer.parseInt(comps[3]);

					List<CombatantClass> classes =
							Arrays.stream(comps[4].split("/"))
								.map(CombatantClass::valueOfShort)
								.collect(Collectors.toList());

					int hp = Integer.parseInt(comps[5]);
					int minAttack = Integer.parseInt(comps[6]);
					int maxAttack = Integer.parseInt(comps[7]);
					int range = comps[8].equals("--") ? 0 : Integer.parseInt(comps[8]);
					int moveTotal = Integer.parseInt(comps[9]);
					int vision = Integer.parseInt(comps[10]);

					int manaPerTurn = 0;

					HashMap<Terrain, Integer> costs = new HashMap<Terrain, Integer>();
					costs.put(Terrain.GRASS, 1);
					costs.put(Terrain.ANCIENT_GROUND, 1); //Same as grass cost
					costs.put(Terrain.WOODS, 1);
					costs.put(Terrain.MOUNTAIN, 1);

					Stats stats = new Stats(
							new Stat(StatType.MAX_HEALTH, hp),
							new Stat(StatType.MIN_ATTACK, minAttack),
							new Stat(StatType.MAX_ATTACK, maxAttack),
							new Stat(StatType.ATTACK_RANGE, range),
							new Stat(StatType.VISION_RANGE, vision),
							new Stat(StatType.MANA_PER_TURN, manaPerTurn),
							new Stat(StatType.MOVEMENT_TOTAL, moveTotal),
							new Stat(StatType.GRASS_COST, costs.get(Terrain.GRASS)),
							new Stat(StatType.WOODS_COST, costs.get(Terrain.WOODS)),
							new Stat(StatType.MOUNTAIN_COST, costs.get(Terrain.MOUNTAIN))
					);
					units.add(new FileCombatant(name, level, classes, manaCost, stats, img));
				} catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					throw new RuntimeException(e);
				}
				// Must be header line, other bad line
			}
		} catch(IOException e){
			e.printStackTrace();
		}

		units.sort(Comparator.comparingInt(Unit::getLevel).thenComparing(Unit::getManaCost));
		FILE_COMBATANTS = Collections.unmodifiableList(units);
	}

	/** Returns a list of units for the given age (minus 1 because age is 1 indexed, this is 0 indexed) - returns by value */
	public static List<FileCombatant> getCombatantsForAge(int age){
		LinkedList<FileCombatant> combatants = new LinkedList<FileCombatant>();
		FILE_COMBATANTS.stream().filter(c -> c.level == age).forEach(combatants::add);
		return combatants;
	}

	/** The image file associated with this FileCombatant */
	private final String img;

	/** Constructor for FileCombatant that clones the given dummy fileCombatant,
	 * starting on the given tile.
	 * @param owner      - the owner of the new FileCombatant
	 * @param startingTile  - the tile on which the new FileCombatant is spawned.
	 * @param dummy      - the FileCombatant to clone
	 */
	private FileCombatant(Player owner, Tile startingTile, FileCombatant dummy){
		super(owner, dummy.name, dummy.getLevel(), dummy.combatantClasses, dummy.manaCost, startingTile, dummy.getStats());
		this.img = dummy.img;
	}
	
	/** Clones this model.unit for the given player */
	@Override
	public Unit clone(Player owner, Tile t){
		return new FileCombatant(owner, t, this);
	}

	/** Constructor used during initialization to create a dummy instance
	 * Creates with null owner, tile.
	 * @param name			- the name of clones of this
	 * @param level 		- the level of clones of this
	 * @param classes   - combatant classes of clones of this
	 * @param manaCost		- the mana cost for clones of this
	 * @param stats			- the stats of clones of this
	 * @param img			- the image to draw for clones of this
	 */
	private FileCombatant(String name, int level, List<CombatantClass> classes, int manaCost, Stats stats, String img){
		super(null, name, level, classes, manaCost, null, stats);
		this.img = img;
	}

	@Override
	public void preFight(Unit other) {}

	@Override
	public void postFight(Unit other) {}

	@Override
	public void preMove(LinkedList<Tile> path) {}

	@Override
	public void postMove(LinkedList<Tile> path) {}

	@Override
	public void preCounterFight(Combatant other) {}

	@Override
	public void postCounterFight(Combatant other) {}

	/** Returns the image file associated with this combatant -- 
	 * {@code return img} */
	@Override
	public String getImgFilename() {
		return img;
	}

}
