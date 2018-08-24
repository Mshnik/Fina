package model.unit.building;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.building.StartOfTurnEffectBuilding.StartOfTurnEffect;
import model.unit.stat.Stats;
import model.util.Cloud;

/**
 * A building that has an effect at the start of the player's turn.
 *
 * @author Mshnik
 */
public final class StartOfTurnEffectBuilding extends Building<StartOfTurnEffect> {

  /** Types of effects that can occur at start of each turn. */
  public enum StartOfTurnEffectType {
    /** Gain value extra mana per turn (cloud is null) */
    MANA_GENERATION,
    /** Gain value extra research per turn (cloud is null) */
    RESEARCH_GAIN,
    /** Heal all combatants in range by value health per turn */
    HEAL_COMBATANT
  }

  /** An effect that occurs at the start of each turn. */
  public static final class StartOfTurnEffect {
    /** The StartOfTurnEffect for this building. */
    public final StartOfTurnEffectType type;

    /** The value of the StartOfTurnEffect for this building. */
    public final int value;

    /** The cloud the effect is applied to, if any. */
    public final Cloud cloud;

    public StartOfTurnEffect(StartOfTurnEffectType type, int value, Cloud cloud) {
      this.type = type;
      this.value = value;
      this.cloud = cloud;
    }
  }

  /** The effect this grants if not built on ancient ground. */
  private final StartOfTurnEffect nonAncientGroundEffect;

  /** The effect this grants if built on ancient ground. */
  private final StartOfTurnEffect ancientGroundEffect;

  /**
   * Constructor for Building. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana. Throws an illegalArgumentException if a
   * building is constructed on land other than AncientGround
   *
   * @param owner - the player owner of this model.unit
   * @param name - the name of this model.unit.
   * @param imageFilename - the image to draw when drawing this unit.
   * @param level - the level of this model.unit - the age this belongs to
   * @param manaCost - the cost of summoning this model.unit. Should be a positive number.
   * @param manaCostScaling - the additional cost of summoning this model.unit for each copy beyond
   *     the first. Should be a non-negative number.
   * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of
   *     this.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundEffect - the effect this building grants if not built on ancient ground
   * @param ancientGroundEffect - the effect this building grants if built on ancient ground
   */
  StartOfTurnEffectBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      Tile tile,
      Stats stats,
      StartOfTurnEffect nonAncientGroundEffect,
      StartOfTurnEffect ancientGroundEffect)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, tile, stats);
    this.nonAncientGroundEffect = nonAncientGroundEffect;
    this.ancientGroundEffect = ancientGroundEffect;
  }

  @Override
  public StartOfTurnEffect getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundEffect
        : nonAncientGroundEffect;
  }

  @Override
  public Unit clone(Player owner, Tile location) {
    return new StartOfTurnEffectBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        location,
        getStats(),
        nonAncientGroundEffect,
        ancientGroundEffect);
  }
}
