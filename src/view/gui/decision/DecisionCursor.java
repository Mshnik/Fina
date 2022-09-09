package view.gui.decision;

import controller.decision.Choice;

import java.awt.Color;

import model.board.Direction;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combat;
import model.unit.modifier.Modifiers.ModifierDescription;
import view.gui.Cursor;

/**
 * A default cursor implementation for when no special cursor actions are necessary
 */
public final class DecisionCursor extends Cursor<Choice, DecisionPanel> {

  /**
   * Message to check for if a unit is hovered to show modifier info instead of unit info. Will
   * check if the message string contains this string.
   */
  public static final String SHOW_EXTENDED_MODIFIERS_INFO_MESSAGE = "Modifiers";

  /**
   * DecisionCursor Constructor. Starts at index (0,0). Has custom yellow color.
   *
   * @param panel - the panel this is on
   */
  DecisionCursor(DecisionPanel panel) {
    super(panel, panel.getElmAt(0, 0));
    setColor(new Color(255, 240, 28));
  }

  /**
   * Can only select if the decision it is on is selectable
   */
  public boolean canSelect() {
    return getElm().isSelectable();
  }

  /**
   * Allows the cursor to wrap vertically on vertical decisions.
   */
  @Override
  public boolean move(Direction d) {
    boolean moved = super.move(d);
    if (moved) {
      return true;
    }

    // If vertical layout, allow for vertical wrapping - check for being on first
    // or last row and hitting up/down.
    if (getPanel().verticalLayout) {
      if (d == Direction.DOWN && getRow() == getPanel().getMatrixHeight() - 1) {
        setElm(getPanel().getElmAt(0, 0));
        moved();
        return true;
      }
      if (d == Direction.UP && getRow() == 0) {
        setElm(getPanel().getElmAt(getPanel().getMatrixHeight() - 1, 0));
        moved();
        return true;
      }
    }

    return false;
  }

  /**
   * Moves are always oked. Returns true, so long as destination isn't null
   */
  @Override
  protected boolean willMoveTo(Direction d, Choice destination) {
    return destination != null;
  }

  /**
   * Needs to have the gamePanel repaint
   */
  @Override
  public void moved() {
    super.moved();

    // Check for having linked object. If so, inspect it
    Object obj = getElm().getVal();
    if (obj != null) {
      if (obj instanceof Unit) {
        panel.getFrame().showUnitStats((Unit) obj);
      } else if (obj instanceof ModifierDescription) {
        panel.getFrame().showModifierDescription((ModifierDescription) obj);
      } else if (obj instanceof Ability) {
        panel.getFrame().showAbilityStats((Ability) obj);
      } else if (obj instanceof Combat) {
        panel.getFrame().showCombatStats((Combat) obj);
      }
    }

    panel.getFrame().repaint();
  }
}
