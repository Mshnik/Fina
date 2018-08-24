package controller.selector;

import controller.game.GameController;
import java.util.ArrayList;
import model.board.Tile;
import model.unit.ability.Ability;
import model.unit.commander.Commander;

public final class CastSelector extends LocationSelector {
  /** The commander doing the casting */
  public final Commander caster;

  /** The cloud of effect range based on the current location of the boardCursor */
  private ArrayList<Tile> effectCloud;

  /** The ability this selector is trying to summon */
  public final Ability toCast;

  public CastSelector(GameController gc, Commander caster, Ability toCast) {
    super(gc);
    effectCloud = new ArrayList<>();
    this.caster = caster;
    this.toCast = toCast;
    refreshPossibilitiesCloud();
  }

  /** Returns the effectCloud for the current location of the boardCursor */
  public ArrayList<Tile> getEffectCloud() {
    return new ArrayList<>(effectCloud);
  }

  /** Refreshes the possible cast locations for this castSelector */
  @Override
  protected void refreshPossibilitiesCloud() {
    cloud = controller.game.board.getRadialCloud(caster.getLocation(), toCast.castDist);
    refreshEffectCloud();
  }

  /** Refreshes the effectCloud for the current location of the boardCursor */
  public void refreshEffectCloud() {
    effectCloud = toCast.getEffectCloud(controller.frame.getGamePanel().boardCursor.getElm());
    controller.repaint();
  }
}
