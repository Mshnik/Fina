package view.gui.panel;

import model.board.Terrain;
import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.building.AllUnitModifierBuilding;
import model.unit.building.Building;
import model.unit.building.CommanderModifierBuilding;
import model.unit.building.PlayerModifierBuilding;
import model.unit.building.StartOfTurnEffectBuilding;
import model.unit.building.SummonerBuilding;
import model.unit.combatant.Combat;
import model.unit.combatant.Combat.CombatantClassPair;
import model.unit.combatant.Combatant;
import model.unit.combatant.Combatant.CombatantClass;
import model.unit.commander.Commander;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.Modifiers;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;
import view.gui.Frame;
import view.gui.image.ImageIndex;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.List;
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

  /** True if extended modifier info should be drawn instead of general unit info. */
  private boolean extendedModifiersInfo;

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
    setPreferredSize(new Dimension(gp.getShowedCols() * frame.getGamePanel().cellSize(), HEIGHT));
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
  public void setUnit(Unit u, boolean isMenu, boolean extendedModifiersInfo) {
    unit = u;
    this.isMenu = isMenu;
    this.extendedModifiersInfo = extendedModifiersInfo;
    ability = null;
    tile = null;
    combat = null;
    repaint();
  }

  /** Clears the extendedModifiersInfo bit. */
  public void clearExtendedModifiersInfo() {
    extendedModifiersInfo = false;
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

    if (unit != null && extendedModifiersInfo) {
      drawUnitPrefix(g2d);
      drawExtendedModifierInfo(g2d);
    } else if (unit != null) {
      drawUnitPrefix(g2d);
      drawUnit(g2d);
    } else if (ability != null) {
      drawAbility(g2d);
    } else if (tile != null) {
      drawTerrain(g2d);
    } else if (combat != null) {
      drawCombat(g2d);
    }
  }

  /** Draws the prefix for a unit listing the name, owner, level, and class. */
  private void drawUnitPrefix(Graphics2D g2d) {
    int x = XMARGIN;
    int y = YMARGIN;
    String mainLine = unit.name;
    if (unit.owner != null) {
      mainLine += " (" + unit.owner.toStringShort() + ")";
    }
    g2d.drawString(mainLine, x, YMARGIN);
    g2d.setFont(MEDIUM_FONT);
    final int infoFont = (int) (MEDIUM_FONT.getSize() * 1.25);
    y += infoFont;

    g2d.drawString(
        " Level "
            + (unit instanceof Commander ? unit.owner.getLevel() : unit.level)
            + " "
            + unit.getIdentifierString(),
        x,
        y);
    y += infoFont;
    if (unit instanceof Combatant) {
      g2d.drawString(" Classes: ", x, y);
      int iconSize = 32;
      x += iconSize;
      for (CombatantClass combatantClass : ((Combatant) unit).combatantClasses) {
        x += 40;
        g2d.drawImage(
            ImageIndex.imageForCombatantClass(combatantClass),
            x,
            y - iconSize / 2,
            iconSize,
            iconSize,
            null);
      }
      x = XMARGIN;
    }

    if (unit instanceof Building && isMenu) {
      List<Terrain> terrainList = ((Building<?>) unit).getValidTerrain();
      y += infoFont;
      g2d.setFont(SMALL_FONT);
      g2d.drawString(
          " Can be Built on "
              + terrainList.stream().map(Terrain::toString).collect(Collectors.joining(", ")),
          x,
          y);
    }
  }

  /** Draws a unit on this InfoPanel. May be in a menu or on the board. */
  private void drawUnit(Graphics2D g2d) {
    final int xInc = 225;
    int x = XMARGIN + xInc;
    int y = YMARGIN;
    final int infoFont = (int) (MEDIUM_FONT.getSize() * 1.2);

    g2d.setFont(SMALL_FONT);
    Stats stats = unit.getStats();

    // Insert health at top here
    if (!isMenu) {
      g2d.drawString("Health", x, y);
      g2d.drawString(
          String.format("%d (%d%%)", unit.getHealth(), (int) (unit.getHealthPercent() * 100)),
          x + 145,
          y);
      y += infoFont;
    }

    for (Stat s : stats.getStandardStatsList(true)) {
      drawStat(g2d, s, x, y);
      y += infoFont;
    }

    x = XMARGIN;
    y = YMARGIN + infoFont * 4;
    String modifierTitle = "Modifiers: ";
    g2d.drawString(modifierTitle, x, y);
    String modString =
        Modifiers.getModifierDescriptions(unit.getVisibleModifiers())
            .stream()
            .map(Modifiers.ModifierDescription::toStringShort)
            .collect(Collectors.joining(", "));
    g2d.drawString(modString, x + frame.getTextWidth(MEDIUM_FONT, modifierTitle) + 15, y);

    x = XMARGIN + 2 * xInc;

    if (unit instanceof MovingUnit) {
      continueDrawingMovingUnit(g2d, x, YMARGIN);
    } else if (unit instanceof Building) {
      continueDrawingBuilding(g2d, x, YMARGIN);
    }

    x += xInc + 100;
    g2d.drawImage(
        ImageIndex.imageForUnit(unit, frame.getController().game.getCurrentPlayer()),
        x + 200,
        10,
        getHeight() - 30,
        getHeight() - 30,
        null);
  }

  /** Continues drawing a unit for movable units. */
  private void continueDrawingMovingUnit(Graphics2D g2d, int x, int y) {
    final int infoFont = (int) (MEDIUM_FONT.getSize() * 1.2);
    final int xInc = 225;

    if (unit.getMaxAttack() > 0) {
      g2d.drawString("Attack", x, y);
      g2d.drawString(unit.getMinAttackScaled() + "-" + unit.getMaxAttackScaled(), x + 145, y);
      y += infoFont;
    }
    if (unit.getMaxAttackRange() > 0) {
      g2d.drawString("Attack Range", x, y);
      g2d.drawString(unit.getMinAttackRange() + "-" + unit.getMaxAttackRange(), x + 145, y);
      y += infoFont;
    }

    if (unit instanceof Combatant) {
      int oldX = x;
      int iconSize = 24;
      y = YMARGIN + (int) (infoFont * 2.5);
      x -= iconSize;
      for (CombatantClass combatantClass : CombatantClass.values()) {
        int bonusLevel =
            Combatant.CombatantClass.getBonusLevel(
                ((Combatant) unit).combatantClasses, Collections.singletonList(combatantClass));
        g2d.drawImage(
            ImageIndex.imageForCombatantClass(combatantClass),
            x,
            y - iconSize / 2,
            iconSize,
            iconSize,
            null);
        g2d.drawString((bonusLevel > 0 ? "+" : "") + bonusLevel, x + iconSize, y);
        x += iconSize * 2;
      }
      x = oldX;
    }

    x += xInc;
    y = YMARGIN;

    if (!isMenu) {
      // Insert movement at top here
      g2d.drawString("Remaining Move", x, y);

      String movement = "0";
      if (unit instanceof MovingUnit) {
        movement = ((MovingUnit) unit).getMovement() + "";
      }
      g2d.drawString(movement, x + 145, y);
      y += infoFont;
    }
    g2d.drawString("Base Move", x, y);
    g2d.drawString(((MovingUnit) unit).getMovementCap() + "", x + 145, y);
    y += infoFont;

    g2d.drawString("Move Costs", x, y);
    g2d.drawString(
        unit.getStats()
            .getMovementCostStatsList(false)
            .stream()
            .map(
                s ->
                    s.name == StatType.MOUNTAIN_COST && (int) s.val > 100
                        ? INF_CHAR + ""
                        : s.val.toString())
            .collect(Collectors.joining("/")),
        x + 145,
        y);
  }

  /** Continues drawing a unit for buildings. */
  private void continueDrawingBuilding(Graphics2D g2d, int x, int y) {
    final int infoFont = (int) (MEDIUM_FONT.getSize() * 1.2);
    final int xInc = 225;

    Building<?> building = (Building<?>) unit;
    if (isMenu) {
      Object nonAncientGroundEffect = building.getPossibleEffectsList().get(0);
      Object ancientGroundEffect = building.getPossibleEffectsList().get(1);
      final int effectSpace = infoFont * 3;

      g2d.setFont(MEDIUM_FONT);
      g2d.drawString("Non-Ancient Ground", x, y);
      y += effectSpace;
      g2d.drawString("Ancient Ground", x, y);

      x += xInc;
      y = YMARGIN;

      g2d.setFont(SMALL_FONT);
      drawBuildingEffect(g2d, building, nonAncientGroundEffect, x, y);

      y += effectSpace;
      drawBuildingEffect(g2d, building, ancientGroundEffect, x, y);
    } else {
      drawBuildingEffect(g2d, building, building.getEffect(), x, y);
    }
  }

  /** Draws extended modifier info for a unit. */
  private void drawExtendedModifierInfo(Graphics2D g2d) {
    g2d.setFont(SMALL_FONT);
    final int infoFont = MEDIUM_FONT.getSize();
    final int xInc = 250;
    int x = XMARGIN + xInc - 50;
    int y = YMARGIN - 5;
    int count = 0;
    for (Modifiers.ModifierDescription description :
        Modifiers.getModifierDescriptions(unit.getVisibleModifiers())) {
      g2d.drawString(description.toString(), x, y);
      y += infoFont;
      count++;
      if (count == 6) {
        x += xInc * 2;
        y = YMARGIN - 5;
      }
    }
  }

  /** Draws a building's effect at the given x,y. */
  private void drawBuildingEffect(
      Graphics2D g2d, Building<?> building, Object effect, int x, int y) {
    if (effect == null) {
      g2d.drawString("None", x, y);
      return;
    }

    if (building instanceof PlayerModifierBuilding
        || building instanceof StartOfTurnEffectBuilding
        || building instanceof SummonerBuilding) {
      g2d.drawString(effect.toString(), x, y);
    } else if (building instanceof AllUnitModifierBuilding) {
      ModifierBundle bundle = (ModifierBundle) effect;
      g2d.drawString("All your Units get " + bundle.toStatString(), x, y);
    } else if (building instanceof CommanderModifierBuilding) {
      ModifierBundle bundle = (ModifierBundle) effect;
      g2d.drawString("Your commander gets " + bundle.toStatString(), x, y);
    }
  }

  /** Draws an ability on this InfoPanel. */
  private void drawAbility(Graphics2D g2d) {
    int x = XMARGIN;
    int y = YMARGIN;
    final int xInc = 225;

    g2d.drawString(ability.name, x, y);
    final int fontSize = (int) (MEDIUM_FONT.getSize() * 1.25);
    g2d.setFont(MEDIUM_FONT);

    y += fontSize;
    g2d.drawString("Costs " + ability.manaCost + " Mana Per Use", x, y);

    String affects = "";
    g2d.setFont(SMALL_FONT);
    if (ability.appliesToAllied) affects += "Allied";
    if (ability.appliesToAllied && ability.appliesToFoe) affects += " and ";
    if (ability.appliesToFoe) affects += "Enemy";

    y += fontSize;
    g2d.drawString(
        "Affects "
            + affects
            + " "
            + ability
                .affectedUnitTypes
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ")),
        x,
        y);

    x += xInc;
    y = YMARGIN;
    g2d.drawString("Effects: " + ability.description, x, y);
  }

  /** Draw the terrain for an empty tile on this info panel. */
  private void drawTerrain(Graphics2D g2d) {
    int x = XMARGIN;
    int y = YMARGIN;

    // Check for hiding ancient ground
    if (frame.getController().game.getFogOfWar().hideAncientGround
        && tile.terrain == Terrain.ANCIENT_GROUND
        && !frame.getController().game.isVisible(tile)) {
      g2d.drawString("Terrain: " + Terrain.GRASS.toString(), x, y);
    } else {
      g2d.drawString("Terrain: " + tile.terrain.toString(), x, y);
    }
  }

  /** Draws combat on this info panel. */
  private void drawCombat(Graphics2D g2d) {
    int x = XMARGIN;
    int y = YMARGIN;
    final int xInc = 225;
    g2d.drawString("Combat", x, y);

    final int smallFontSizeWithMargin = SMALL_FONT.getSize() + 3;

    x += g2d.getFontMetrics().stringWidth("Combat") + XMARGIN;
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

    int oldX = x;
    x += 50;
    int iconSize = 28;
    int iconSizePlusSpace = iconSize + 16;
    g2d.setFont(BIG_FONT);
    for (CombatantClassPair pair : combat.getRelevantClassPairs()) {
      g2d.drawImage(
          ImageIndex.imageForCombatantClass(pair.first),
          x,
          y - iconSize - 2,
          iconSize,
          iconSize,
          null);
      x += iconSizePlusSpace;
      g2d.drawString(pair.firstBeatsSecond() ? ">" : "<", x - 16, y - iconSize / 2 + 2);
      g2d.drawImage(
          ImageIndex.imageForCombatantClass(pair.second),
          x,
          y - iconSize - 2,
          iconSize,
          iconSize,
          null);
      x += iconSizePlusSpace;
    }
    x = oldX;
    g2d.setFont(SMALL_FONT);

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

  private void drawStat(Graphics2D g2d, Stat s, int x, int y) {
    if (s.name == StatType.ACTIONS_PER_TURN) {
      g2d.drawString("Actions per Turn", x, y);
    } else {
      g2d.drawString(s.name.toString(), x, y);
    }
    g2d.drawString(s.val.toString(), x + 145, y);
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
