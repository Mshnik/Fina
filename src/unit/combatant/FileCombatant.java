package unit.combatant;

import game.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import board.Terrain;
import board.Tile;
import unit.AttackType;
import unit.Combatant;
import unit.Unit;
import unit.UnitStats;
import util.TextIO;

/** A FileUnit is a combatant read in from file.
 * Full constructors are private,
 * but the class maintains a static collection of units
 * that can be cloned for either player.
 * 
 * @author MPatashnik
 *
 */
public class FileCombatant extends Combatant {

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

				//Check for new age - first element is currentAge + 1. Rest of line can still have a unit on it.
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
					double magDef = Double.parseDouble(comps[4]) / 100.0;
					int attack = Integer.parseInt(comps[5]);
					AttackType atkType = AttackType.fromAbbrevString(comps[6]);
				
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

					UnitStats stats = new UnitStats(hp, manaPerTurn, 
							attack, atkType, physDef, 
							magDef, range, vision);
					currentAgeUnits.add(new FileCombatant(name, manaCost, 
							stats, moveTotal, costs, img));
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
		return new LinkedList<FileCombatant>(FILE_COMBATANTS.get(age - 1));
	}

	/** Constructor for FileCombatant that clones the given dummy fileCombatant,
	 * starting on the given tile.
	 * @param owner			- the owner of the new FileCombatant
	 * @param startingTile 	- the tile on which the new FileCombatant is spawned.
	 * @param dummy			- the FileCombatant to clone
	 */
	private FileCombatant(Player owner, Tile startingTile, FileCombatant dummy){
		super(owner, dummy.name, dummy.manaCost, startingTile, dummy.getStats());
		this.img = dummy.img;
		this.moveCost = new HashMap<Terrain, Integer>(dummy.moveCost);
		this.movementCap = dummy.movementCap;
	}
	
	/** Clones this unit for the given player */
	@Override
	public Unit clone(Player owner, Tile t){
		return new FileCombatant(owner, t, this);
	}

	/** Constructor used during initialization to create a dummy instance
	 * Creates with null owner, tile.
	 * @param name			- the name of clones of this
	 * @param manaCost		- the mana cost for clones of this
	 * @param stats			- the stats of clones of this
	 * @param moveTotal		- the total movement cap of clones of this
	 * @param costs			- the momement costs of clones of this
	 * @param img			- the image to draw for clones of this
	 */
	private FileCombatant(String name, int manaCost, UnitStats stats, int moveTotal, 
			HashMap<Terrain, Integer> costs, String img){
		super(null, name, manaCost, null, stats);
		this.img = img;
		movementCap = moveTotal;
		moveCost = costs;
	}
	

	/** The image file associated with this FileCombatant */
	public final String img;

	/** The total movement cap for this unit */
	private int movementCap;
	
	/** The movement cost for traveling the given type of terrian */
	private HashMap<Terrain, Integer> moveCost;

	@Override
	public void preFight(Unit other) {}

	@Override
	public void postFight(Unit other) {}

	@Override
	public int getMovementCap() {
		return movementCap;
	}

	@Override
	public int getMovementCost(Terrain t) {
		if(! moveCost.containsKey(t)) return Integer.MAX_VALUE;
		return moveCost.get(t);
	}

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
