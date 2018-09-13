package view.gui.modifier;

import java.awt.Graphics;
import java.awt.Graphics2D;
import model.board.Tile;
import model.unit.Unit;
import view.gui.image.ImageIndex;
import view.gui.panel.GamePanel;

/** ModifierIcon that cycles through icons with animation. */
public final class AnimatedModifierIcon extends ModifierIcon {

  /** The state of animation this is currently on. */
  private int animationState;

  /** Constructs a new AnimatedModifierIcon. */
  public AnimatedModifierIcon(GamePanel gamePanel, Unit unit) {
    super(gamePanel, unit);
  }

  @Override
  public int getStateCount() {
    return getModifiers().size();
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
  public void paintComponent(Graphics g) {
    if (hasModifiers()) {
      Graphics2D g2d = (Graphics2D) g;
      Tile tile = unit.getLocation();
      int tileX = gamePanel.getXPosition(tile);
      int tileY = gamePanel.getYPosition(tile);
      int iconSize = 24;

      g2d.drawImage(
          ImageIndex.imageForModifierDescription(getModifiers().get(getState())),
          tileX,
          tileY,
          iconSize,
          iconSize,
          null);
    }
  }
}
