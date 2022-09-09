package model.unit.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

/**
 * A building that can summon new units (but not buildings) like a commander.
 *
 * @author Mshnik
 */
public final class SummonerBuilding extends Building<SummonerBuilding.SummonerBuildingEffect>
    implements Summoner {

  /**
   * An effect for a summoner building.
   */
  static final class SummonerBuildingEffect {

    /**
     * How far away this building will be able to summon units.
     */
    private final int summonRadius;

    /**
     * How many actions per turn this unit will have.
     */
    private final int actionsPerTurn;

    SummonerBuildingEffect(int summonRadius, int actionsPerTurn) {
      this.summonRadius = summonRadius;
      this.actionsPerTurn = actionsPerTurn;
    }

    @Override
    public String toString() {
      return "Can summon units at radius "
          + summonRadius
          + " "
          + actionsPerTurn
          + " time(s) per turn";
    }
  }

  /**
   * Effect this building grants if this isn't on ancient ground.
   */
  private final SummonerBuildingEffect nonAncientGroundEffect;

  /**
   * Effect this building grants if this is on ancient ground.
   */
  private final SummonerBuildingEffect ancientGroundEffect;

  /**
   * Constructor for Building. Also adds this model.unit to the tile it is on as an occupant, and
   * its owner as a model.unit that player owns, Subtracts manaCost from the owner, but throws a
   * runtimeException if the owner doesn't have enough mana.
   *
   * @param owner                          - the player owner of this model.unit
   * @param name                           - the name of this model.unit.
   * @param imageFilename                  - the image to draw when drawing this unit.
   * @param level                          - the level of this model.unit - the age this belongs to
   * @param manaCost                       - the cost of summoning this model.unit. Should be a positive number. * @param
   * @param manaCostScaling                - the additional cost of summoning this model.unit for each copy beyond
   *                                       the first. Should be a non-negative number.
   * @param validTerrain                   - types of terrain this can be built on.
   * @param stats                          - the base unmodified stats of this model.unit. stats that remain used are
   * @param nonAncientGroundSummonerEffect - summon effect for this if this is on ancient ground.
   * @param ancientGroundSummonerEffect    - summon effect for this if this is on ancient ground.
   */
  SummonerBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      List<Terrain> validTerrain,
      Stats stats,
      SummonerBuildingEffect nonAncientGroundSummonerEffect,
      SummonerBuildingEffect ancientGroundSummonerEffect)
      throws RuntimeException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, validTerrain, stats);
    this.nonAncientGroundEffect = nonAncientGroundSummonerEffect;
    this.ancientGroundEffect = ancientGroundSummonerEffect;
  }

  @Override
  public List<SummonerBuildingEffect> getPossibleEffectsList() {
    LinkedList<SummonerBuildingEffect> list = new LinkedList<>();
    list.add(nonAncientGroundEffect);
    list.add(ancientGroundEffect);
    return Collections.unmodifiableList(list);
  }

  @Override
  public SummonerBuildingEffect getEffect() {
    return getLocation() != null && getLocation().terrain == Terrain.ANCIENT_GROUND
        ? ancientGroundEffect
        : nonAncientGroundEffect;
  }

  /**
   * Override summon range to be determined by effect instead of stat.
   */
  @Override
  public int getSummonRange() {
    return getEffect().summonRadius;
  }

  @Override
  public boolean hasSummonSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Can't build.
   */
  @Override
  public boolean hasBuildSpace() {
    return false;
  }

  /**
   * Can summon if it has actions remaining.
   */
  @Override
  public boolean canSummon() {
    return getActionsRemaining() > 0;
  }

  /**
   * Can summon the same things the commander can summon.
   */
  @Override
  public Map<String, Combatant> getSummonables() {
    return owner.getCommander().getSummonables();
  }

  /**
   * Can't build anything.
   */
  @Override
  public Map<String, Building> getBuildables() {
    return Collections.emptyMap();
  }

  @Override
  protected Unit createClone(Player owner, Tile cloneLocation) {
    return new SummonerBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        getValidTerrain(),
        new Stats(
            getStats(),
            new Stat(
                StatType.ACTIONS_PER_TURN,
                cloneLocation.terrain == Terrain.ANCIENT_GROUND
                    ? ancientGroundEffect.actionsPerTurn
                    : nonAncientGroundEffect.actionsPerTurn)),
        nonAncientGroundEffect,
        ancientGroundEffect);
  }
}
