package view.gui.panel;

import model.board.Terrain;
import model.board.Tile;
import model.unit.Combatant;
import model.unit.Commander;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.ability.EffectAbility;
import model.unit.ability.ModifierAbility;
import model.unit.combatant.Combat;
import model.unit.modifier.Modifier;
import model.unit.modifier.ModifierBundle;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
import view.gui.Frame;
import view.gui.ImageIndex;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

public final class InfoPanel extends JPanel {
  /** */
  private static final long serialVersionUID = 1L;

  /** The Height of the InfoPanel */
  protected static final int HEIGHT = 125;

  /** The color of the border surrounding the headerPanel */
  protected static final Color BORDER_COLOR = new Color(74, 47, 12);

  /** Distance (in pixels) between the left of the InfoPanel and the left of the bars */
  private static final int XMARGIN = 25;

  /** Distance (in pixels) between the top of the InfoPanel and the top of the bars */
  private static final int YMARGIN = 32;

  /** Font for drawing title text */
  private static final Font BIG_FONT = new Font(Frame.FONTNAME, Font.BOLD, 20);

  /** Font for drawing standard text */
  private static final Font MEDIUM_FONT = new Font(Frame.FONTNAME, Font.BOLD, 16);

  /** Font for drawing small text */
  private static final Font SMALL_FONT = new Font(Frame.FONTNAME, Font.BOLD, 13);

  /** Character for the infinity character */
  private static final char INF_CHAR = '\u221E';

  /** The frame this belongs to */
  public final Frame frame;

  /** The Unit (if any) this InfoPanel is currently drawing info for */
  private Unit unit;

  /** The Ability (if any) this InfoPanel is currently drawing info for */
  private Ability ability;

  /** The Tile (if any) this InfoPanel is currently drawing info for the terrain */
  private Tile tile;

  /** The Combat (if any) this InfoPanel is currently drawing info for */
  private Combat combat;

  /** True iff the player is currently looking at a menu, false otherwise (looking at board). */
  private boolean isMenu;

  public InfoPanel(Frame f) {
    frame = f;
    GamePanel gp = f.getGamePanel();
    setPreferredSize(new Dimension(gp.getShowedCols() * GamePanel.CELL_SIZE, HEIGHT));
  }

  /** Returns the tile this InfoPanel is currently drawing info for */
  public Unit getUnit() {
    return unit;
  }

  /** Returns the Ability this InfoPanel is currently drawing info for */
  public Ability getAbility() {
    return ability;
  }

  /** Sets the unit this InfoPanel is to draw info for, and causes a repaint */
  public void setUnit(Unit u, boolean isMenu) {
    unit = u;
    this.isMenu = isMenu;
    ability = null;
    tile = null;
    combat = null;
    repaint();
  }

  /** Sets the ability this InfoPanel is to draw info for, and causes a repaint */
  public void setAbility(Ability a) {
    unit = null;
    ability = a;
    isMenu = true;
    tile = null;
    combat = null;
    repaint();
  }

  /** Sets the tile this InfoPanel is to draw for and causes a repaint */
  public void setTile(Tile t) {
    unit = null;
    ability = null;
    tile = t;
    combat = null;
    isMenu = false;
    repaint();
  }

  public void setCombat(Combat c) {
    unit = null;
    ability = null;
    tile = null;
    combat = c;
    isMenu = true;
    repaint();
  }

  /** Paints this InfoPanel, the info for the currently selected model.unit */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    // Background painting
    if (getHeight() == 0) return;
    for (int i = 0; i <= getWidth(); i += getHeight()) {
      g2d.drawImage(ImageIndex.SANDSTONE, i, 0, getHeight(), getHeight(), null);
    }
    g2d.setColor(BORDER_COLOR);
    int width = 7;
    g2d.setStroke(new BasicStroke(width));
    g2d.drawRect(width / 2, width / 2, getWidth() - width, getHeight() - width);

    g2d.setColor(Color.black);
    g2d.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
    g2d.setFont(BIG_FONT);

    int x = XMARGIN;
    int y = YMARGIN;
    final int xInc = 225;

    // Unit painting
    if (unit != null) {
      String mainLine = unit.name;
      if (unit.owner != null) {
        mainLine += " (" + unit.owner + ")";
      }
      g2d.drawString(mainLine, x, YMARGIN);
      g2d.setFont(MEDIUM_FONT);
      final int infoFont = MEDIUM_FONT.getSize();

      String subString = " " + unit.getIdentifierString() + " ";
      if (unit instanceof Combatant) {
        subString +=
            " - "
                + ((Combatant) unit)
                    .combatantClasses
                    .stream()
                    .map(Combatant.CombatantClass::toMidString)
                    .collect(Collectors.joining(", "));
      }

      g2d.drawString(subString, x, YMARGIN + infoFont);

      g2d.setFont(SMALL_FONT);
      x += xInc;
      Stats stats = unit.getStats();

      // Insert health at top here
      if (!isMenu) {
        g2d.drawString("Health", x, y);
        g2d.drawString(unit.getHealth() + "", x + 145, y);
        y += infoFont;
      }

      for (Stat s : stats.getStandardStatsList(true)) {
        drawStat(g2d, s, x, y);
        y += infoFont;
      }

      x = XMARGIN;
      y = YMARGIN + infoFont * 5;
      String modifierTitle = "Modifiers: ";
      g2d.drawString(modifierTitle, x, y);
      String modString = "";
      for (Modifier m : unit.getModifiers()) {
        if (!(m.name.equals(Commander.LEVELUP_HEALTH_NAME)
            || m.name.equals(Commander.LEVELUP_MANA_NAME))) modString += m.name + " ";
      }
      g2d.drawString(modString, x + frame.getTextWidth(MEDIUM_FONT, modifierTitle) + 15, y);

      x = XMARGIN + 2 * xInc;
      // Draw modifier list along bottom
      y = YMARGIN;
      for (Stat s : stats.getAttackStatsList(true)) {
        drawStat(g2d, s, x, y);
        y += infoFont;
      }
      x += xInc;
      y = YMARGIN;

      if (!isMenu) {
        // Insert movement at top here
        g2d.drawString("Movement", x, y);

        String movement = "0";
        if (unit instanceof MovingUnit) {
          movement = ((MovingUnit) unit).getMovement() + "";
        }
        g2d.drawString(movement, x + 145, y);
        y += infoFont;
      }

      for (Stat s : stats.getMovementStatsList(true)) {
        drawStat(g2d, s, x, y);
        y += infoFont;
      }
    }
    // Ability painting
    else if (ability != null) {
      g2d.drawString(ability.name, x, y);
      final int fontSize = MEDIUM_FONT.getSize();
      g2d.setFont(MEDIUM_FONT);

      y += fontSize;
      if (ability.manaCost > 0) {
        g2d.drawString("Costs " + ability.manaCost + " Mana Per Use", x, y);
        if (!ability.canBeCastMultipleTimes) {
          y += fontSize;
          g2d.drawString("Can Only Be Cast Once Per Turn", x, y);
        }
      } else g2d.drawString("Passive Ability", x, y);

      y += fontSize;
      int size = ability.getEffectCloudSize();
      if (size == Integer.MAX_VALUE) g2d.drawString("Affects Whole Board", x, y);
      else g2d.drawString("Affects Up To " + size + " Units", x, y);

      String affects = "";
      if (ability.appliesToAllied) affects += "Allied";
      if (ability.appliesToAllied && ability.appliesToFoe) affects += " and ";
      if (ability.appliesToFoe) affects += "Enemy";

      y += fontSize;
      g2d.drawString("Affects " + affects + " Units", x, y);

      x += xInc;
      y = YMARGIN;
      if (ability instanceof ModifierAbility) {
        ModifierBundle mod = ((ModifierAbility) ability).getModifiers();
        String turns =
            (mod.getTurnsRemaining() == Integer.MAX_VALUE ? INF_CHAR + "" : mod.getTurnsRemaining())
                + " Turns Remaining ("
                + (mod.isStackable() ? "" : "Not ")
                + "Stackable)";
        g2d.drawString(turns, x, y);
        for (Modifier m : mod.getModifiers()) {
          y += fontSize;
          g2d.drawString(m.toStatString(), x, y);
        }
      } else if (ability instanceof EffectAbility) {
        g2d.drawString("Effects:", x, y);
        y += fontSize;
        String s = ability.toStringLong();
        g2d.drawString(s, x, y);
      }
    }
    // Terrain painting.
    else if (tile != null) {
      // Check for hiding ancient ground
      if (frame.getController().game.getFogOfWar().hideAncientGround
          && tile.terrain == Terrain.ANCIENT_GROUND
          && !frame.getController().game.isVisible(tile)) {
        g2d.drawString("Terrain: " + Terrain.GRASS.toString(), x, y);
      } else {
        g2d.drawString("Terrain: " + tile.terrain.toString(), x, y);
      }
    }
    // Combat painting.
    else if (combat != null) {
      g2d.drawString("Combat", x, y);

      final int smallFontSizeWithMargin = SMALL_FONT.getSize() + 3;

      x += g.getFontMetrics().stringWidth("Combat") + XMARGIN;
      y += BIG_FONT.getSize();

      g2d.setFont(SMALL_FONT);
      g2d.drawString("Health (%)", x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString("Attack", x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString("Modifiers", x, y);

      drawCombatColumn(g2d, combat.attacker, xInc);
      drawCombatColumn(g2d, combat.defender, xInc * 2);

      x = xInc * 3;
      y = YMARGIN;
      g2d.setFont(MEDIUM_FONT);
      g2d.drawString("Results", x, y);

      g2d.setFont(SMALL_FONT);
      y += MEDIUM_FONT.getSize();
      g2d.drawString("Class Bonus", x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString("Final Attack", x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString("Will Counterattack", x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString("Final Counterattack", x, y);

      x = xInc * 4;
      y = YMARGIN;

      y += MEDIUM_FONT.getSize();
      int classBonus = combat.getClassBonus();
      g2d.drawString(classBonus > 0 ? "+" + classBonus : "" + classBonus, x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString(String.format("%d - %d", combat.getMinAttack(), combat.getMaxAttack()), x, y);

      y += smallFontSizeWithMargin;
      String willCounterAttack;
      if (combat.defenderCouldCounterAttack()) {
        willCounterAttack = combat.getMaxAttack() >= combat.defender.getHealth() ? "Maybe" : "Yes";
      } else {
        willCounterAttack = "No";
      }
      g2d.drawString(willCounterAttack, x, y);

      y += smallFontSizeWithMargin;
      g2d.drawString(
          String.format(
              "%d - %d",
              combat.getProjectedMinCounterAttack(), combat.getProjectedMaxCounterAttack()),
          x,
          y);
    }
  }

  private void drawStat(Graphics2D g2d, Stat s, int x, int y) {
    g2d.drawString(s.name.toString(), x, y);
    String str;
    if (s.name == StatType.MOUNTAIN_COST && (Integer) s.val > 100) {
      str = INF_CHAR + "";
    } else {
      str = s.val.toString();
    }
    g2d.drawString(str, x + 145, y);
  }

  private void drawCombatColumn(Graphics2D g2d, Unit unit, int x) {
    int y = YMARGIN;
    g2d.setFont(MEDIUM_FONT);
    g2d.drawString(unit.name + " (" + unit.owner + ")", x, y);

    y += BIG_FONT.getSize();
    g2d.setFont(SMALL_FONT);
    g2d.drawString(
        String.format("%d (%d%%)", unit.getHealth(), (int) (100 * unit.getHealthPercent())), x, y);

    y += MEDIUM_FONT.getSize();
    g2d.drawString(
        String.format("%d - %d", unit.getMinAttackScaled(), unit.getMaxAttackScaled()), x, y);

    y += MEDIUM_FONT.getSize();
    // TODO
    g2d.drawString("TODO", x, y);
  }
}
