package unit;

/** A Modifier for a unit - a buff or nerf, etc. */
public class UnitModifier {

	/** The unit this is modifying */
	public final Combatant unit;
	
	/** The remaining turns for this Modifier.
	 * If 0, on its last turn.
	 */
	private int remainingTurns;
	
	/** Constructor
	 * @param u - The unit this is modifying
	 * @param turns - the total duration of this modifier (turns after this one)
	 */
	public UnitModifier(Combatant u, int turns){
		unit = u;
		remainingTurns = turns;
	}
	
	/** Returns the remaining turns of this modifier */
	public int getRemainingTurns(){
		return remainingTurns;
	}
	
	/** Decrements the remaining turns.
	 * Method calling this method should use the return of the method to maybe
	 * remove this modifier.
	 * @return true iff this is still valid (remainingTurns >= 0)
	 */
	public boolean decRemainingTurns(){
		remainingTurns--;
		return remainingTurns >= 0;
	}
}
