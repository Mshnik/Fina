package view.gui.modifier;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import model.board.Tile;
import model.unit.Unit;
import model.unit.modifier.Modifiers.ModifierDescription;
import view.gui.image.ImageIndex;
import view.gui.panel.GamePanel;

/**
 * ModifierIcon that stacks the modifiers into a horizontal row below the unit. Doesn't need any
 * animation.
 */
public final class RowModifierIcon extends ModifierIcon {

  /**
   * Constructs a new RowModifierIcon.
   */
  public RowModifierIcon(GamePanel gamePanel, Unit unit) {
    super(gamePanel, unit);
  }

  @Override
  public int getStateCount() {
    return 1;
  }

  @Override
  public int getState() {
    return 0;
  }

  @Override
  public void advanceState() {
  }

  @Override
  public void setState(int state) {
  }

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  public void paintComponent(Graphics g) {
    if (hasModifiers()) {
      Graphics2D g2d = (Graphics2D) g;
      Tile tile = unit.getLocation();
      int iconSize = gamePanel.cellSize() / 4;
      int margin = iconSize / 6;
      int iconSizeWithDoubleMargin = iconSize + margin * 2;
      int xPosition =
          gamePanel.getXPosition(tile)
              + gamePanel.cellSize() / 2
              - (int) ((double) modifierDescriptions.size() / 2 * iconSizeWithDoubleMargin);
      int yPosition = gamePanel.getYPosition(tile) - iconSizeWithDoubleMargin;

      g2d.setColor(BACKGROUND_COLOR);
      g2d.fill(
          new RoundRectangle2D.Float(
              xPosition,
              yPosition,
              iconSizeWithDoubleMargin * modifierDescriptions.size(),
              iconSizeWithDoubleMargin,
              margin * 2,
              margin * 2));

      for (ModifierDescription modifierDescription : modifierDescriptions) {
        g2d.drawImage(
            ImageIndex.imageForModifierDescription(modifierDescription),
            xPosition + margin,
            yPosition + margin,
            iconSize,
            iconSize,
            null);
        xPosition += iconSizeWithDoubleMargin;
      }
    }
  }
}
