package model.unit.modifier;

/**
 * A modifier applied to a player. Notably does not extend {@link Modifier} because this does not attach to a unit.
 */
public final class PlayerModifier {
  /**
   * Types of player modifiers.
   */
  public enum PlayerModifierType {
    /**
     * Bonus mana generation.
     */
    MANA_GENERATION,
    /**
     * Bonus research.
     */
    RESEARCH_GENERATION,
    /**
     * Percent discount on Summoning.
     */
    SUMMON_DISCOUNT,
    /**
     * Percent discount on Building.
     */
    BUILD_DISCOUNT,
    /**
     * Percent discount on Casting.
     */
    CAST_DISCOUNT,
    /**
     * Increase selection range for non-cloud casting.
     */
    CAST_SELECT_BOOST,
    /**
     * Increased cloud level for cloud-casting.
     */
    CAST_CLOUD_BOOST
  }

  /**
   * The type of effect for this effect.
   */
  public final PlayerModifierType effectType;

  /**
   * The value of this effect.
   */
  public final int value;

  /**
   * A string representation of this effect.
   */
  public final String description;

  public PlayerModifier(PlayerModifierType effectType, int value, String description) {
    this.effectType = effectType;
    this.value = value;
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }
}
