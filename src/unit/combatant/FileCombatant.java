package unit.combatant;

import game.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import board.Terrain;
import board.Tile;
import unit.Combatant;
import unit.Unit;
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
			
			LinkedList<FileCombatant> currentAgeUnits;
			int currentAge = 0;
			for(String line : unitLines){
				if(line.length() == 0) continue;	//blank line - do nothing on this iteration
				
				String[] comps = line.split("\\t");
				
				//Check for new age - first element is currentAge + 1. Rest of line is header.
				try{
					if(Integer.parseInt(comps[0]) == currentAge + 1){
						currentAgeUnits = new LinkedList<FileCombatant>();
						units.add(currentAgeUnits);
						currentAge++;
						continue;
					}
				}catch(NumberFormatException e){}	//Not this line, that's ok.
				
			}
			
		} catch(IOException e){
			e.printStackTrace();
		}
		FILE_COMBATANTS = Collections.unmodifiableList(units);
	}
	
	
	
	/** Constructor for FileCombatant that clones the given dummy fileCombatant,
	 * starting on the given tile.
	 * @param owner			- the owner of the new FileCombatant
	 * @param startingTile 	- the tile on which the new FileCombatant is spawned.
	 * @param dummy			- the FileCombatant to clone
	 */
	public FileCombatant(Player owner, Tile startingTile, FileCombatant dummy){
		super(owner, dummy.manaCost, startingTile, dummy.getStats());
		this.img = dummy.img;
	}
	
	
	
//	public FileCombatant(Player owner, int manaCost, Tile startingTile,
//			UnitStats stats) throws RuntimeException, IllegalArgumentException {
//		super(owner, manaCost, startingTile, stats);
//	}

	/** The image file associated with this FileCombatant */
	public final String img;
	
	
	
	@Override
	public void preFight(Unit other) {}

	@Override
	public void postFight(Unit other) {}

	@Override
	public int getMovementCap() {
		return 0;
	}

	@Override
	public int getMovementCost(Terrain t) {
		return 0;
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
