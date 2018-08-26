package model.unit.commander;

import model.board.Tile;
import model.game.Player;
import model.unit.ability.Ability;
import model.unit.combatant.Combatant;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

import java.util.LinkedList;

public final class DummyCommander extends Commander {

  /** Stats for maxHealth, range and visionRange of dummy commander */
  private static final Stats STATS =
      new Stats(
          new Stat(StatType.MAX_HEALTH, Commander.BASE_HEALTH),
          new Stat(StatType.MANA_PER_TURN, 200),
          new Stat(StatType.COMMANDER_ACTIONS_PER_TURN, Commander.BASE_ACTIONS_PT),
          new Stat(StatType.SUMMON_RANGE, 1),
          new Stat(StatType.VISION_RANGE, 2),
          new Stat(StatType.MOVEMENT_TOTAL, 2),
          new Stat(StatType.GRASS_COST, 1),
          new Stat(StatType.WOODS_COST, 2),
          new Stat(StatType.MOUNTAIN_COST, 9999));

  public DummyCommander(Player owner, int startingLevel) {
    super("Dummy", "mario.png", owner, STATS, startingLevel);
  }

  @Override
  public void preMove(LinkedList<Tile> path) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postMove(LinkedList<Tile> path) {}

  @Override
  public void preCounterFight(Combatant other) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postCounterFight(int damageDealt, Combatant other, int damageTaken) {
    // TODO Auto-generated method stub

  }

  @Override
  public Ability[] getPossibleAbilities(int level) {
    return null;
  }
}
