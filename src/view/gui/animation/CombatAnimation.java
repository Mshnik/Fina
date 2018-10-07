package view.gui.animation;

import model.board.Tile;
import model.game.Game;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import view.gui.panel.GamePanel;

import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * An animation for a single unit attacking another unit.
 *
 * @author Mshnik
 */
public final class CombatAnimation extends UnitAnimation {
  private final double scaledY;
  private final double scaledX;

  public CombatAnimation(GamePanel gamePanel, Combatant combatant, Tile target) {
    super(gamePanel, combatant);
    Tile location = getUnit().getLocation();
    int dRow = target.row - location.row;
    int dCol = target.col - location.col;
    double hypotenuse = Math.hypot(dRow * dRow, dCol * dCol);

    scaledY = dRow * (1 / hypotenuse);
    scaledX = dCol * (1 / hypotenuse);
  }

  @Override
  public int getStateCount() {
    return 8;
  }

  @Override
  public void paintComponent(Graphics g) {
    Tile location = getUnit().getLocation();
    int state = getState() < getStateCount() / 2 ? getState() : getStateCount() - getState();
    int x =
        gamePanel.getXPosition(location)
            + (int) (gamePanel.getElementWidth() * scaledX * state / getStateCount());
    int y =
        gamePanel.getYPosition(location)
            + (int) (gamePanel.getElementHeight() * scaledY * state / getStateCount());
    gamePanel.drawUnit((Graphics2D) g, getUnit(), x, y);
  }

  @Override
  public boolean isVisible(Game game) {
    return game.isVisibleToMostRecentHumanPlayer(getUnit().getLocation());
  }
}
