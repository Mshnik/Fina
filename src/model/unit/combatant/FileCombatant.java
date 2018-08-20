package model.unit.combatant;


import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Combatant;
import model.unit.Unit;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

import util.TextIO;

/** A FileUnit is a combatant read in from file.
 * Full constructors are private,
 * but the class maintains a static collection of units
 * that can be cloned for either player.
 * 
 * @author MPatashnik
 *
 */
public final class FileCombatant extends Combatant {

	/** The avaliable combatants read into storage during initialization.
	 * All Lists is immutable. Outer most list is by age (index = age - 1),
	 * Inner ordered by increasing mana cost.
	 */
	public static final List<List<FileCombatant>> FILE_COMBATANTS;

	/** File containing the units, in csv format */
	private static final File UNITS_FILE = new File("game/units.csv");

	/** Initializes the combatants available */
	static{
		LinkedList<List<FileCombatant>> units = new LinkedList<>();
		try{
			String[] unitLines = TextIO.read(UNITS_FILE).split("\\n");

			LinkedList<FileCombatant> currentAgeUnits = null;
			int currentAge = 0;
			final Comparator<FileCombatant> manaCostSorter = new Comparator<FileCombatant>(){
				@Override
				public int compare(FileCombatant o1, FileCombatant o2) {
					return o1.manaCost - o2.manaCost;
				}
			};
			for(String line : unitLines){
				String[] comps = line.split(",");
				if(comps.length == 0) continue;	//Blank line

				//Check for new age - first element is currentAge + 1. Rest of line can still have a model.unit on it.
				try{
					int x = Integer.parseInt(comps[0]);
					if(x == currentAge + 1){
						//Sort old age, if any
						if(currentAgeUnits != null){
							Collections.sort(currentAgeUnits, manaCostSorter);
						}
						
						currentAgeUnits = new LinkedList<FileCombatant>();
						units.add(currentAgeUnits);
						currentAge++;
					}
				}catch(NumberFormatException e){}	//Not this line, that's ok.

				try{
					String name = comps[1];
					int hp = Integer.parseInt(comps[2]);
					double physDef = Double.parseDouble(comps[3]) / 100.0;
					int attack = Integer.parseInt(comps[5]);

					int moveTotal = Integer.parseInt(comps[7]);
					HashMap<Terrain, Integer> costs = new HashMap<Terrain, Integer>();
					costs.put(Terrain.GRASS, Integer.parseInt(comps[8]));
					costs.put(Terrain.ANCIENT_GROUND, costs.get(Terrain.GRASS)); //Same as grass cost
					costs.put(Terrain.WOODS, Integer.parseInt(comps[9]));
					costs.put(Terrain.MOUNTAIN, Integer.parseInt(comps[10]));
					int range = Integer.parseInt(comps[11]);
					int vision = Integer.parseInt(comps[12]);
					int manaCost = Integer.parseInt(comps[13]);
					int manaPerTurn = Integer.parseInt(comps[14]);
					String img = comps[15];

					Stats stats = new Stats(
							new Stat(StatType.MAX_HEALTH, hp),
							new Stat(StatType.PHYSICAL_DEFENSE, physDef),
							new Stat(StatType.ATTACK, attack),
							new Stat(StatType.ATTACK_RANGE, range),
							new Stat(StatType.VISION_RANGE, vision),
							new Stat(StatType.MANA_PER_TURN, manaPerTurn),
							new Stat(StatType.MOVEMENT_TOTAL, moveTotal),
							new Stat(StatType.GRASS_COST, costs.get(Terrain.GRASS)),
							new Stat(StatType.WOODS_COST, costs.get(Terrain.WOODS)),
							new Stat(StatType.MOUNTAIN_COST, costs.get(Terrain.MOUNTAIN))
					);
					currentAgeUnits.add(new FileCombatant(name, currentAge, manaCost,
							stats, img));
				} catch(NumberFormatException | ArrayIndexOutOfBoundsException e){} 
				// Must be header line, other bad line
			}

		} catch(IOException e){
			e.printStackTrace();
		}
		FILE_COMBATANTS = Collections.unmodifiableList(units);
	}

	/** Returns a list of units for the given age (minus 1 because age is 1 indexed, this is 0 indexed) - returns by value */
	public static List<FileCombatant> getCombatantsForAge(int age){
		LinkedList<FileCombatant> combatants = new LinkedList<FileCombatant>();
		combatants.addAll(FILE_COMBATANTS.get(age -1));
		return combatants;
	}

	/** Constructor for FileCombatant that clones the given dummy fileCombatant,
	 * starting on the given tile.
	 * @param owner			- the owner of the new FileCombatant
	 * @param startingTile 	- the tile on which the new FileCombatant is spawned.
	 * @param dummy			- the FileCombatant to clone
	 */
	private FileCombatant(Player owner, Tile startingTile, FileCombatant dummy){
		super(owner, dummy.name, dummy.getLevel(), dummy.manaCost, startingTile, dummy.getStats());
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
	 * @param manaCost		- the mana cost for clones of this
	 * @param stats			- the stats of clones of this
	 * @param img			- the image to draw for clones of this
	 */
	private FileCombatant(String name, int level, int manaCost, Stats stats, String img){
		super(null, name, level, manaCost, null, stats);
		this.img = img;
	}
	

	/** The image file associated with this FileCombatant */
	public final String img;

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
