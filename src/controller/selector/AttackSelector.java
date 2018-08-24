package controller.selector;

import controller.game.GameController;
import java.awt.Color;
import model.board.Tile;
import model.unit.combatant.Combatant;

/** A selector for choosing a model.unit to attack */
public final class AttackSelector extends LocationSelector {

  /** The model.unit that is attacking from this selector */
  public final Combatant attacker;

  public AttackSelector(GameController gc, Combatant attacker) {
    super(gc);
    this.attacker = attacker;
    cloudColor = new Color(1.0f, 0f, 0f, 0.5f);
    refreshPossibilitiesCloud();
  }

  @Override
  protected void refreshPossibilitiesCloud() {
    cloud =
        controller.game.board.getRadialCloud(attacker.getLocation(), attacker.getAttackRange() + 1);
    int i = 0;
    while (i < cloud.size()) {
      Tile t = cloud.get(i);
      if (!attacker.owner.canSee(t)
          || !t.isOccupied()
          || t.getOccupyingUnit().owner == attacker.owner) cloud.remove(i);
      else i++;
    }
    controller.repaint();
  }
}
