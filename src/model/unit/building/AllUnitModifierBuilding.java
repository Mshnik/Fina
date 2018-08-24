package model.unit.building;

import model.board.Tile;
import model.game.Player;
import model.unit.Building;
import model.unit.Unit;
import model.unit.modifier.ModifierBundle;
import model.unit.stat.Stats;

/**
 * A building that grants all units the player controls a modifier as long as this building is
 * alive.
 *
 * @author Mshnik
 */
public final class AllUnitModifierBuilding extends Building {

  /** The modifiers granted to all units the player controls. */
  private final ModifierBundle modifierBundle;

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
   * @param manaCost - the cost of summoning this model.unit. Should be a positive number. * @param
   * @param manaCostScaling - the additional cost of summoning this model.unit for each copy beyond the
   *     first. Should be a non-negative number.
   * @param tile - the tile this model.unit begins the model.game on. Also notifies the tile of
   *     this.
   * @param stats - the base unmodified stats of this model.unit. stats that remain used are
   * @param modifierBundle - the modifiers to grant to all units this player controls.
   */
  AllUnitModifierBuilding(
      Player owner,
      String name,
      String imageFilename,
      int level,
      int manaCost,
      int manaCostScaling,
      Tile tile,
      Stats stats,
      ModifierBundle modifierBundle)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, level, manaCost, manaCostScaling, tile, stats);
    this.modifierBundle = modifierBundle;
  }

  /** Applies the modifiers in this building to the given unit. */
  public void applyModifiersTo(Unit u) {
    modifierBundle.clone(u, this);
  }

  @Override
  public Unit clone(Player owner, Tile location) {
    return new AllUnitModifierBuilding(
        owner,
        name,
        getImgFilename(),
        level,
        manaCost,
        manaCostScaling,
        location,
        getStats(),
        new ModifierBundle(modifierBundle));
  }
}
