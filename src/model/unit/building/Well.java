package model.unit.building;

import model.board.Tile;
import model.game.Player;
import model.unit.Building;
import model.unit.Unit;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

/**
 * A Well is a building that grants its owner mana per turn as long as it is controlled.
 *
 * @author MPatashnik
 */
public final class Well extends Building {
  /** Name of a well in model.game */
  public static final String NAME = "Well";

  /** Mana cost of constructing a well */
  public static final int COST = 500;

  /** Minimum level for building a well */
  public static final int LEVEL = 1;

  /** ManaPerTurn generated per well */
  public static final int MANAPT = 100;

  /** Stats for wells */
  private static final Stats STATS =
      new Stats(new Stat(StatType.MAX_HEALTH, 800), new Stat(StatType.MANA_PER_TURN, MANAPT));

  public Well(Player owner, Tile tile) {
    super(owner, NAME, "well.png", LEVEL, COST, COST, tile, STATS);
  }

  /** Returns a new well for the given owner, location */
  @Override
  public Unit clone(Player owner, Tile location) {
    return new Well(owner, location);
  }
}
