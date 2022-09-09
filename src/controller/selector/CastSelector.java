package controller.selector;

import controller.game.GameController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import model.board.Tile;
import model.unit.ability.Ability;
import model.unit.commander.Commander;
import view.gui.image.Colors;

public final class CastSelector extends LocationSelector {
  /**
   * The commander doing the casting
   */
  public final Commander caster;

  /**
   * The cloud of effect range based on the current location of the boardCursor
   */
  private List<Tile> effectCloud;

  /**
   * The ability this selector is trying to summon
   */
  public final Ability toCast;

  public CastSelector(GameController gc, Commander caster, Ability toCast) {
    super(gc);
    effectFillColor = Colors.CAST_FILL_COLOR;
    effectTraceColor = Colors.CAST_BORDER_COLOR;
    effectCloud = new ArrayList<>();
    this.caster = caster;
    this.toCast = toCast;
    refreshPossibilitiesCloud();
  }

  /**
   * Refreshes the possible cast locations for this castSelector
   */
  @Override
  protected void refreshPossibilitiesCloud() {
    cloud = caster.owner.game.board.getCastCloud(this);
    if (!cloud.isEmpty()) {
      refreshEffectCloud();
    }
  }

  @Override
  public List<Tile> getEffectCloud() {
    return Collections.unmodifiableList(effectCloud);
  }

  /**
   * Refreshes the effectCloud for the current location of the boardCursor
   */
  @Override
  public void refreshEffectCloud() {
    effectCloud =
        toCast.getTranslatedEffectCloud(
            caster,
            controller.frame.getGamePanel().boardCursor.getElm(),
            caster.owner.getCastCloudBoost());
    controller.repaint();
  }
}
