package unit;

/** The different stats that are present in a unitstats */
public enum StatType{
	/** The base (pre-modification) stats this was calculated from, if any */
	BASE,
	/** The health cap for this unit. Actual health level is kept track of by unit.
	 * maxHealth >= 0. */
	MAX_HEALTH,
	/** The mana generation/cost of this unit, if any. Positive is generation */
	MANA_PER_TURN,
	/** Damage dealt when attacking (Before applying defenses). attack >= 0 */
	ATTACK,
	/** Type of damage dealt when attacking. */
	ATTACK_TYPE,
	/** Defense against physical damage - percent of physical damage prevented.
	 * 1 > physicalDefense >= 0 */
	PHYSICAL_DEFENSE,
	/** Defense against magic damage - percent of magic damage prevented
	 * 1 > magicDefense >= 0 */
	MAGIC_DEFENSE,
	/** Distance at which another unit can be attacked
	 * Distance is measured using manhattan distance - 1.
	 * Units with range 0 are melee, others are ranged
	 * range >= 0.
	 */
	ATTACK_RANGE,
	/** Distance at which another unit can be summoned
	 * Distance is measured using manhattan distance - 1.
	 * range >= 0.
	 */
	SUMMON_RANGE,
	/** Vision radius of this unit */
	VISION_RANGE
}