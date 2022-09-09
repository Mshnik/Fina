package view.gui.modifier;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import model.board.Tile;
import model.unit.Unit;
import view.gui.image.ImageIndex;
import view.gui.panel.GamePanel;

/**
 * ModifierIcon that cycles through icons with animation.
 */
public final class AnimatedModifierIcon extends ModifierIcon {

  /**
   * The state of animation this is currently on.
   */
  private int animationState;

  /**
   * Constructs a new AnimatedModifierIcon.
   */
  public AnimatedModifierIcon(GamePanel gamePanel, Unit unit) {
    super(gamePanel, unit);
  }

  @Override
  public int getStateCount() {
    return modifierDescriptions.size();
  }

  @Override
  public int getState() {
    return animationState;
  }

  @Override
  public void advanceState() {
    animationState = (animationState + 1) % getStateCount();
    gamePanel.repaint();
  }

  @Override
  public void setState(int state) {
    animationState = state;
    gamePanel.repaint();
  }

  @Override
  public boolean isActive() {
    return modifierDescriptions.size() > 1;
  }

  @Override
  public void paintComponent(Graphics g) {
    if (hasModifiers()) {
      Graphics2D g2d = (Graphics2D) g;
      Tile tile = unit.getLocation();
      int iconSize = gamePanel.cellSize() / 4;
      int margin = iconSize / 6;
      int xPosition = gamePanel.getXPosition(tile);
      int yPosition = gamePanel.getYPosition(tile);

      g2d.setColor(BACKGROUND_COLOR);
      g2d.fill(
          new RoundRectangle2D.Float(
              xPosition,
              yPosition,
              iconSize + margin * 2,
              iconSize + margin * 2,
              margin * 2,
              margin * 2));
      g2d.drawImage(
          ImageIndex.imageForModifierDescription(modifierDescriptions.get(getState())),
          xPosition + margin,
          yPosition + margin,
          iconSize,
          iconSize,
          null);
    }
  }
}
