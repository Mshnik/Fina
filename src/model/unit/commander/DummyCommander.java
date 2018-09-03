package model.unit.commander;

import model.board.Tile;
import model.game.Player;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

public final class DummyCommander extends Commander {

  /** Stats for maxHealth, range and visionRange of dummy commander */
  private static final Stats STATS =
      new Stats(
          new Stat(StatType.MAX_HEALTH, Commander.BASE_HEALTH),
          new Stat(StatType.MANA_PER_TURN, 200),
          new Stat(StatType.ACTIONS_PER_TURN, Commander.BASE_ACTIONS_PT),
          new Stat(StatType.SUMMON_RANGE, 1),
          new Stat(StatType.VISION_RANGE, 2),
          new Stat(StatType.MOVEMENT_TOTAL, 2),
          new Stat(StatType.GRASS_COST, 1),
          new Stat(StatType.WOODS_COST, 2),
          new Stat(StatType.MOUNTAIN_COST, 9999));

  public DummyCommander() {
    this(null, null);
  }

  private DummyCommander(Player owner, Tile location) {
    super(owner, location, "Dummy", "mario.png", STATS);
  }

  @Override
  protected Commander createClone(Player owner, Tile cloneLocation) {
    if (this.owner != null) {
      throw new RuntimeException(
          "Shouldn't make a clone of an already on the board commander " + this);
    }
    return new DummyCommander(owner, cloneLocation);
  }
}
