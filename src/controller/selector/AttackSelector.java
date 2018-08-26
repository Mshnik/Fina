package controller.selector;

import controller.game.GameController;
import model.board.Tile;
import model.unit.combatant.Combatant;
import model.util.ExpandableCloud;

import java.awt.*;

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
    cloud =
        ExpandableCloud.create(
                ExpandableCloud.ExpandableCloudType.CIRCLE, attacker.getMaxAttackRange() + 1)
            .difference(
                ExpandableCloud.create(
                    ExpandableCloud.ExpandableCloudType.CIRCLE, attacker.getMinAttackRange()))
            .translate(attacker.getLocation().getPoint())
            .toTileSet(controller.game.board);
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
