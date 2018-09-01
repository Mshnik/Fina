package controller.selector;

import controller.game.GameController;
import model.unit.combatant.Combatant;

import java.awt.Color;

/** A selector for choosing a model.unit to attack */
public final class AttackSelector extends LocationSelector {

  /** Color to shade area an attack selector is on. */
  public static final Color SHADE_COLOR = new Color(1.0f, 0f, 0f, 0.5f);

  /** The model.unit that is attacking from this selector */
  public final Combatant attacker;

  public AttackSelector(GameController gc, Combatant attacker) {
    super(gc);
    this.attacker = attacker;
    cloudColor = SHADE_COLOR;
    refreshPossibilitiesCloud();
  }

  @Override
  protected void refreshPossibilitiesCloud() {
    cloud = attacker.getAttackableTiles(true);
    controller.repaint();
  }
}
