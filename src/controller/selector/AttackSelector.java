package controller.selector;

import controller.game.GameController;
import model.board.Tile;
import model.unit.combatant.Combatant;
import view.gui.image.Colors;

import java.util.Collections;
import java.util.List;

/**
 * A selector for choosing a model.unit to attack
 */
public final class AttackSelector extends LocationSelector {

  /**
   * The model.unit that is attacking from this selector
   */
  public final Combatant attacker;

  /**
   * The cloud of effect range based on the current location of the boardCursor
   */
  private List<Tile> effectCloud;

  public AttackSelector(GameController gc, Combatant attacker) {
    super(gc);
    this.attacker = attacker;
    effectFillColor = Colors.ATTACK_FILL_COLOR;
    effectTraceColor = Colors.ATTACK_BORDER_COLOR;
    refreshPossibilitiesCloud();
  }

  @Override
  protected void refreshPossibilitiesCloud() {
    cloud = attacker.getAttackableTiles(true);
    if (!cloud.isEmpty()) {
      controller.frame.getGamePanel().boardCursor.setElm(cloud.get(0));
      refreshEffectCloud();
    }
  }

  @Override
  public List<Tile> getEffectCloud() {
    return Collections.unmodifiableList(effectCloud);
  }

  @Override
  public void refreshEffectCloud() {
    effectCloud = Collections.singletonList(controller.frame.getGamePanel().boardCursor.getElm());
    controller.repaint();
  }
}
