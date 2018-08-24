package model.unit.building;

import java.util.ArrayList;
import model.board.Terrain;
import model.board.Tile;
import model.game.Player;
import model.unit.Building;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

public final class Baracks extends Building implements Summoner {

  /** The in model.game name of a Baracks */
  public static final String NAME = "Baracks";

  /** Minimum level for building a baracks */
  public static final int LEVEL = 2;

  /** Mana cost of constructing a baracks */
  public static final int COST = 800;

  /** Stats for maxHealth, defenses, range and visionRange of baracks */
  private static final Stats STATS =
      new Stats(
          new Stat(StatType.MAX_HEALTH, 1000),
          new Stat(StatType.SUMMON_RANGE, 1),
          new Stat(StatType.VISION_RANGE, 1));

  public Baracks(Player owner, Tile tile) throws RuntimeException, IllegalArgumentException {
    super(owner, NAME, "tower.png", LEVEL, COST, COST, tile, STATS);
  }

  /** Baracks are able to summon new units */
  @Override
  public boolean canSummon() {
    return true;
  }

  /** Returns a new Baracks for the given owner, on the given location */
  @Override
  public Unit clone(Player owner, Tile location) {
    return new Baracks(owner, location);
  }

  /**
   * Returns true if summoning a model.unit is currently ok - checks surrounding area for free space
   */
  public boolean hasSummonSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied()) return true;
    }
    return false;
  }

  /**
   * Returns true if building a model.unit is currently ok - checks surrounding area for free
   * ancient ground
   */
  public boolean hasBuildSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied() && t.terrain == Terrain.ANCIENT_GROUND) return true;
    }
    return false;
  }
}
