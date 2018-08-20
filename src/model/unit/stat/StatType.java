package model.unit.stat;

/** The different stats that are present in a unitstats */
public enum StatType{
	/** The base (pre-modification) stats this was calculated from, if any */
	BASE,
	/** The health cap for this model.unit. Actual health level is kept track of by model.unit.
	 * maxHealth >= 0. */
	MAX_HEALTH,
	/** The mana generation/cost of this model.unit, if any. Positive is generation */
	MANA_PER_TURN,
	/** Damage dealt when attacking (Before applying defenses). attack >= 0 */
	ATTACK,
	/** Distance at which another model.unit can be attacked
	 * Distance is measured using manhattan distance - 1.
	 * Units with range 0 are melee, others are ranged
	 * range >= 0.
	 */
	ATTACK_RANGE,
	/** Distance at which another model.unit can be summoned
	 * Distance is measured using manhattan distance - 1.
	 * range >= 0.
	 */
	SUMMON_RANGE,
	/** Total movement a model.unit can do in a turn */
	MOVEMENT_TOTAL,
	/** Vision radius of this model.unit */
	VISION_RANGE,
	/** Grass movement cost */
	GRASS_COST,
	/** Woods movement cost */
	WOODS_COST,
	/** Mountain movement cost */
	MOUNTAIN_COST;
	
	/** Overrides traditional enum tostring - returns camel case, no underscores */
	@Override
	public String toString(){
		String s = super.toString();
		s = s.replace('_', ' ').toLowerCase();
		String finishedS = "";
		do{
			int i = s.indexOf(' ');
			if(i == -1)
				i = s.length();
			finishedS += Character.toUpperCase(s.charAt(0)) + s.substring(1, i);
			if(i != s.length()){
				finishedS += ' ';
				s = s.substring(i + 1);
			} else{
				break;
			}
		}while(true);
		
		return finishedS;
	}

	/** Returns true iff this stat is an attack-related stat. */
	public boolean isAttackStat() {
		return this == ATTACK || this == ATTACK_RANGE;
	}

	/** Returns true iff this stat is a movement-related stat. */
	public boolean isMovementStat() {
		return this == MOVEMENT_TOTAL || this == GRASS_COST || this == WOODS_COST || this == MOUNTAIN_COST;
	}

	/** Returns the class of the given type */
	public static Class<?> getClassOfType(StatType t){
		switch(t){
			case ATTACK:
			case ATTACK_RANGE:
			case GRASS_COST:
			case MANA_PER_TURN:
			case MAX_HEALTH:
			case MOUNTAIN_COST:
			case MOVEMENT_TOTAL:
			case SUMMON_RANGE:
			case VISION_RANGE:
			case WOODS_COST:
				return Integer.class;
			case BASE:
				return Stats.class;
			default:
				return null;
		}
	}
	
}