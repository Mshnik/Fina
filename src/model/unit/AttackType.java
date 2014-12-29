package model.unit;

/** The different types of attacks a model.unit can have */
public enum AttackType {
	NO_ATTACK,
	PHYSICAL,
	MAGIC,
	TRUE;
	
	/** Returns the attack type from the given abbreviated (one char) string.
	 * @param s - either "p" - Physical, "m" - Magic, "t" - true, or anything else - NO_ATTACK
	 * @return
	 */
	public static AttackType fromAbbrevString(String s){
		switch(s.toLowerCase()){
		case "p": return PHYSICAL;
		case "m": return MAGIC;
		case "t": return TRUE;
		default: return NO_ATTACK;
		}
	}
	
	/** Returns the abbreviated string (one char) from the given attack type
	 * @return - either "p" - Physical, "m" - Magic, "t" - true, No Attack - "n"
	 */
	public static String getAbbrevString(AttackType t){
		switch(t){
		case MAGIC:		return "m";
		case PHYSICAL:	return "p";
		case TRUE:		return "t";
		default:		return "n";
		}
	}
}
