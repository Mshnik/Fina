package unit.ability;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import unit.Commander;

public abstract class Ability {

	/** The commander that owns this ability */
	public final Commander caster;
	
	/** The name of this ability */
	public final String name;
	
	/** The cost of casting this Ability */
	public final int manaCost;
	
	/** The effect range of this Ability, as a collection of locations
	 * relative to its origin location
	 */
	private final Set<Point2D> effectCloud;
	
	/** Returns a copy of the effectCloud as a set of points relative to the origin location as (0,0). */
	public Set<Point2D> getEffectCloud(){
		return new HashSet<Point2D>(effectCloud);
	}
	
	/** Distance from the commander this can be cast. 0 for Requiring commander as origin */
	public final int castDist;
	
}
