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
    controller.repaint();
  }

  @Override
  public List<Tile> getEffectCloud() {
    return Collections.emptyList();
  }

  @Override
  public void refreshEffectCloud() {
  }
}
