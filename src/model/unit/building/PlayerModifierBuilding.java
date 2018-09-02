package model.unit.building;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Unit;
import model.unit.building.PlayerModifierBuilding.PlayerModifierEffect;
import model.unit.stat.Stats;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** An extension of building that grants the player (not a particular unit) a bonus. */
public final class PlayerModifierBuilding extends Building<PlayerModifierEffect> {

  /** Types of effects that can occur at start of each turn. */
  public enum PlayerModifierEffectType {
    /** Bonus mana generation. */
    MANA_GENERATION,
    /** Bonus research. */
    RESEARCH_GENERATION,
    /** Percent discount on Summoning. */
    SUMMON_DISCOUNT,
    /** Percent discount on Building. */
    BUILD_DISCOUNT,
    /** Percent discount on Casting. */
    CAST_DISCOUNT,
    /** Increase selection range for non-cloud casting. */
    CAST_SELECT_BOOST,
    /** Increased cloud level for cloud-casting. */
    CAST_CLOUD_BOOST;
  }

  public static final class PlayerModifierEffect {
    /** The type of effect for this effect. */
    public final PlayerModifierEffectType effectType;

    /** The value of this effect. */
    public final int value;

    /** A string representation of this effect. */
    public final String description;

    PlayerModifierEffect(PlayerModifierEffectType effectType, int value, String description) {
      this.effectType = effectType;
      this.value = value;
      this.description = description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  /** Effect this building grants if this isn't on ancient ground. */
  public final PlayerModifierEffect nonAncientGroundEffect;

  /** Effect this building grants if this is on ancient ground. */
  public final PlayerModifierEffect ancientGroundEffect;

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
   *     the first. Should be non-negative.
   * @param validTerrain - types of terrain this can be built on.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundEffect - the effect this gives if this isn't on ancient ground.
   * @param ancientGroundEffect - the effect this gives if this is on ancient ground.
   */
  PlayerModifierBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      PlayerModifierEffect nonAncientGroundEffect,
      PlayerModifierEffect ancientGroundEffect)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundEffect = nonAncientGroundEffect;
    this.ancientGroundEffect = ancientGroundEffect;
  }

  @Override
  public List<PlayerModifierEffect> getPossibleEffectsList() {
    LinkedList<PlayerModifierEffect> list = new LinkedList<>();
    list.add(nonAncientGroundEffect);
    list.add(ancientGroundEffect);
    return Collections.unmodifiableList(list);
  }

  @Override
  public PlayerModifierEffect getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundEffect
        : nonAncientGroundEffect;
  }

  @Override
  protected Unit createClone(Player owner, Tile cloneLocation) {
    return new PlayerModifierBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        getStats(),
        nonAncientGroundEffect,
        ancientGroundEffect);
  }

  /** PlayerModifierBuilding can't summon. */
  @Override
  public boolean canSummon() {
    return false;
  }
}
