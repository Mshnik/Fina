package view.gui.decision;

import controller.decision.Choice;
import controller.decision.Decision;
import controller.game.GameController;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import model.game.Player;
import model.unit.ability.Ability;
import model.unit.combatant.Combatant;
import model.unit.modifier.Modifiers.ModifierDescription;
import view.gui.Frame;
import view.gui.MatrixPanel;
import view.gui.Paintable;
import view.gui.image.ImageIndex;

/**
 * Panel on the bottom of the frame that shows text as necessary for decisions
 *
 * @author MPatashnik
 */
public class DecisionPanel extends MatrixPanel<Choice> implements Paintable {

  /** */
  private static final long serialVersionUID = 1L;

  /** Background color for DecisionPanel */
  private static final Color BACKGROUND = new Color(117f / 255f, 74f / 255f, 0 / 255f, 0.5f);

  /** Border color for DecisionPanel */
  private static final Color BORDER = new Color(145, 116, 0);

  /** Width of borders for DecisionPanel */
  private static final int BORDER_WIDTH = 4;

  /** Text color */
  private static final Color TEXT_COLOR = Color.white;

  /** Font for DecisionPanel */
  private static final Font TEXT_FONT = new Font(Frame.FONTNAME, Font.BOLD, 17);

  /** Text x margin */
  private static final int TEXT_X = 17;

  /** Text y margin */
  private static final int TEXT_Y = 25;

  /** The title of this decisionPanel, to paint at the top */
  private String title;

  /** The decision to display on this DecisionPanel */
  private Decision decision;

  /** The drawing height of a Decision */
  public static final int DECISION_HEIGHT = 40;

  /** The size of combatant classes on summoning decisions. */
  private static final int ICON_SIZE = 24;

  /** The drawing width of an Decision */
  public final int DECISION_WIDTH;

  /** A Cursor for this DecisionPanel */
  public final DecisionCursor cursor;

  /** The x coordinate of the top left corner of this panel */
  private int x;

  /** The y coordinate of the top left corner of this panel */
  private int y;

  /** The player this decision is for */
  public final Player player;

  /** True iff this should layout vertically, false for horizontally. */
  final boolean verticalLayout;

  public DecisionPanel(
      GameController g,
      Player p,
      int elmsToShow,
      String title,
      Decision decision,
      boolean verticalLayout) {
    super(g, verticalLayout ? 1 : elmsToShow, verticalLayout ? elmsToShow : 1, 0, 0, 0, 0);
    this.player = p;
    this.decision = decision;
    this.title = title;
    this.verticalLayout = verticalLayout;

    // Determine width of panel based on all text
    int maxWidth = controller.frame.getTextWidth(TEXT_FONT, title);
    for (Choice c : decision) {
      maxWidth = Math.max(maxWidth, controller.frame.getTextWidth(TEXT_FONT, c.getMessage()));
    }
    DECISION_WIDTH = maxWidth + TEXT_X * 2 + ICON_SIZE; // Add margins for either side

    cursor = new DecisionCursor(this);
    cursor.moved();
  }

  /** Sets the x position of this DecisionPanel */
  public void setXPosition(int xPos) {
    x = xPos;
  }

  /** Sets the y position of this DecisionPanel */
  public void setYPosition(int yPos) {
    y = yPos;
  }

  /**
   * If vertical, The width of this Panel is the width of a single decision. Otherwise it is the
   * width of the number of decisions.
   */
  @Override
  public int getWidth() {
    if (verticalLayout) {
      return getElementWidth();
    } else {
      return super.getShowedCols() * getElementWidth();
    }
  }

  /**
   * If vertical, The height of this Panel is the height of all the decisions and the title.
   * Otherwise it is the height of the title and a single decision.
   */
  @Override
  public int getHeight() {
    if (verticalLayout) {
      return getElementHeight()
          * (Math.min(super.getShowedRows(), decision.size()) + (title.isEmpty() ? 0 : 1));
    } else {
      return getElementHeight() * (title.isEmpty() ? 1 : 2);
    }
  }

  /** Overrides super version by adding the x coordinate of this panel to super's result */
  @Override
  public int getXPosition(Choice elm) {
    return super.getXPosition(elm) + x;
  }

  /**
   * Overrides super version by adding the y coordinate of this panel to super's result. Adds height
   * for the title if it is non-empty.
   */
  @Override
  public int getYPosition(Choice elm) {
    return super.getYPosition(elm) + y + (title.isEmpty() ? 0 : DECISION_HEIGHT);
  }

  /** Returns the decision the cursor is currently hovering * */
  public Choice getElm() {
    return cursor.getElm();
  }

  /** Returns the decision message of the decision that is currently selected */
  public String getSelectedDecisionMessage() {
    return cursor.getElm().getMessage();
  }

  /** Draws this DecisionPanel */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(BACKGROUND);
    g2d.fillRect(x, y, getWidth(), getHeight());
    g2d.setStroke(new BasicStroke(BORDER_WIDTH));
    g2d.setColor(BORDER);
    g2d.drawRect(
        x + BORDER_WIDTH / 2,
        y + BORDER_WIDTH / 2,
        getWidth() - BORDER_WIDTH,
        getHeight() - BORDER_WIDTH);

    g2d.setFont(TEXT_FONT);
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

    if (!title.isEmpty()) {
      // Title section
      g2d.setColor(BACKGROUND.brighter());
      g2d.fillRect(x, y, getWidth(), DECISION_HEIGHT);

      // Title
      g2d.setColor(TEXT_COLOR);
      g2d.drawString(title, TEXT_X + x, TEXT_Y + y);
    }
    int titleHeight = title.isEmpty() ? 0 : DECISION_HEIGHT;

    if (verticalLayout) {
      for (int r = scrollY; r < scrollY + Math.min(super.getShowedRows(), getMatrixHeight()); r++) {
        if (decision.get(r).isSelectable()) g2d.setColor(TEXT_COLOR);
        else g2d.setColor(TEXT_COLOR.darker());
        int textX = TEXT_X + x;
        int textY = TEXT_Y + y + (r - scrollY) * DECISION_HEIGHT + titleHeight;
        g2d.drawString(decision.get(r).getMessage(), textX, textY);
        if (decision.get(r).getVal() instanceof Combatant
            && ((Combatant) decision.get(r).getVal()).owner == null) {
          paintCombatantClasses(
              g2d, x + DECISION_WIDTH - TEXT_X * 2, textY, (Combatant) decision.get(r).getVal());
        } else if (decision.get(r).getVal() instanceof Ability) {
          paintAbilityType(
              g2d, x + DECISION_WIDTH - TEXT_X * 2, textY, (Ability) decision.get(r).getVal());
        } else if (decision.get(r).getVal() instanceof ModifierDescription) {
          paintModifier(
              g2d,
              x + DECISION_WIDTH - TEXT_X * 2,
              textY,
              (ModifierDescription) decision.get(r).getVal());
        }
      }
    } else {
      for (int c = scrollX; c < scrollX + Math.min(super.getShowedCols(), getMatrixWidth()); c++) {
        if (decision.get(c).isSelectable()) g2d.setColor(TEXT_COLOR);
        else g2d.setColor(TEXT_COLOR.darker());
        int textX = TEXT_X + x + (c - scrollX) * DECISION_WIDTH;
        int textY = TEXT_Y + y + titleHeight;
        g2d.drawString(decision.get(c).getMessage(), textX, textY);
        if (decision.get(c).getVal() instanceof Combatant
            && ((Combatant) decision.get(c).getVal()).owner == null) {
          paintCombatantClasses(
              g2d, x + DECISION_WIDTH - TEXT_X * 2, textY, (Combatant) decision.get(c).getVal());
        } else if (decision.get(c).getVal() instanceof Ability) {
          paintAbilityType(
              g2d, x + DECISION_WIDTH - TEXT_X * 2, textY, (Ability) decision.get(c).getVal());
        } else if (decision.get(c).getVal() instanceof ModifierDescription) {
          paintModifier(
              g2d,
              x + DECISION_WIDTH - TEXT_X * 2,
              textY,
              (ModifierDescription) decision.get(c).getVal());
        }
      }
    }

    // Draw cursor
    cursor.paintComponent(g);
  }

  /** Draws a combatant class (or multiple) when hovering a summon decision. */
  private void paintCombatantClasses(Graphics2D g2d, int x, int y, Combatant combatant) {
    int spacing = (int) (ICON_SIZE * 1.1);
    for (Combatant.CombatantClass combatantClass : combatant.combatantClasses) {
      g2d.drawImage(
          ImageIndex.imageForCombatantClass(combatantClass), x, y - 18, ICON_SIZE, ICON_SIZE, null);
      x -= spacing;
    }
  }

  /** Draws a ability symbol when hovering a cast decision. */
  private void paintAbilityType(Graphics2D g2d, int x, int y, Ability ability) {
    g2d.drawImage(ImageIndex.imageForAbility(ability), x, y - 18, ICON_SIZE, ICON_SIZE, null);
  }

  /** Draws the modifier symbol when hovering a modifier. */
  private void paintModifier(
      Graphics2D g2d, int x, int y, ModifierDescription modifierDescription) {
    g2d.drawImage(
        ImageIndex.imageForModifierDescription(modifierDescription),
        x,
        y - 18,
        ICON_SIZE,
        ICON_SIZE,
        null);
  }

  /** If vertical, the width is 1. Otherwise it is the number of decisions. */
  @Override
  public int getMatrixWidth() {
    if (verticalLayout) {
      return 1;
    } else {
      return decision.size();
    }
  }

  /** If vertical the height is the number of decisions, otheriwse it is 1. */
  @Override
  public int getMatrixHeight() {
    if (verticalLayout) {
      return decision.size();
    } else {
      return 1;
    }
  }

  /** Returns the x component of the top-left of where this DecisionPanel is drawn. */
  public int getDrawingX() {
    return x;
  }

  /** Returns the y component of the top-left of where this DecisionPanel is drawn. */
  public int getDrawingY() {
    return y;
  }

  /** Returns the action at the given index. Col must be 0 */
  @Override
  public Choice getElmAt(int row, int col) throws IllegalArgumentException {
    if (verticalLayout) {
      if (col != 0) throw new IllegalArgumentException("Can't get decision at col != 0");
      try {
        return decision.get(row);
      } catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Can't get decision at row " + row + ": OOB");
      }
    } else {
      if (row != 0) throw new IllegalArgumentException("Can't get decision at row != 0");
      try {
        return decision.get(col);
      } catch (IndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Can't get decision at col " + col + ": OOB");
      }
    }
  }

  /**
   * Like getElmAt, but includes scrolling. Thus returns the effective visual nth element instead of
   * the real one.
   */
  public Choice getElmAtWithScrolling(int row, int col) throws IllegalArgumentException {
    return getElmAt(row + scrollY, col + scrollX);
  }

  /** Returns DecisionPanel.DECISION_HEIGHT */
  @Override
  public int getElementHeight() {
    return DECISION_HEIGHT;
  }

  /** Returns DecisionPanel.DECISION_WIDTH */
  @Override
  public int getElementWidth() {
    return DECISION_WIDTH;
  }

  @Override
  public String toString() {
    String s = "";
    for (Choice d : decision) {
      s += d.toString() + ", ";
    }
    return s.substring(0, s.length() - 2);
  }
}
