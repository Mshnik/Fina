package view.gui.animation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.board.Tile;
import model.game.Game;
import model.unit.MovingUnit;
import model.unit.Unit;
import view.gui.panel.GamePanel;

/** An animation representing a unit moving along a given path. */
public final class MovementAnimation extends UnitAnimation {

  /**
   * The ratio of a frame to how far along a path (in tiles) the unit should move. Should be in the
   * range (0,1]. The higher the value the faster the animation.
   */
  private static final double TILE_MOVEMENT_PER_FRAME_RATIO = 0.5;

  /** The path to animate the unit moving along. */
  private final List<Tile> movementPath;

  /** Creates a new animation for the given args. */
  public MovementAnimation(GamePanel gamePanel, MovingUnit movingUnit, List<Tile> movementPath) {
    super(gamePanel, movingUnit);
    this.movementPath = movementPath;
  }

  @Override
  public int getStateCount() {
    return (int) ((movementPath.size() - 1) / TILE_MOVEMENT_PER_FRAME_RATIO) + 1;
  }

  /** Returns the tiles this is currently on, either size 1 or 2. */
  private List<Tile> getCurrentTiles() {
    double pathState = getState() * TILE_MOVEMENT_PER_FRAME_RATIO;
    int prevTileState = (int) Math.floor(pathState);
    int nextTileState = (int) Math.ceil(pathState);
    // If this state is exactly on a tile, return that single tile. Otherwise
    // return the two tiles this is between, previous then next.
    if (prevTileState == nextTileState) {
      return Collections.singletonList(movementPath.get(prevTileState));
    } else {
      return Arrays.asList(movementPath.get(prevTileState), movementPath.get(nextTileState));
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    int x;
    int y;
    List<Tile> currentTiles = getCurrentTiles();
    // If this state is exactly on a tile, set that position.
    // Otherwise, linearly interpolate between the two tiles this is between.
    if (currentTiles.size() == 1) {
      x = gamePanel.getXPosition(currentTiles.get(0));
      y = gamePanel.getYPosition(currentTiles.get(0));
    } else {
      double pathState = getState() * TILE_MOVEMENT_PER_FRAME_RATIO;
      int prevTileState = (int) Math.floor(pathState);
      Tile prevTile = currentTiles.get(0);
      Tile nextTile = currentTiles.get(1);
      double towardNextTilePercent = pathState - prevTileState;
      x =
          (int)
              (gamePanel.getXPosition(nextTile) * towardNextTilePercent
                  + gamePanel.getXPosition(prevTile) * (1 - towardNextTilePercent));
      y =
          (int)
              (gamePanel.getYPosition(nextTile) * towardNextTilePercent
                  + gamePanel.getYPosition(prevTile) * (1 - towardNextTilePercent));
    }
    gamePanel.drawUnit((Graphics2D) g, getUnit(), x, y);
  }

  @Override
  public boolean isVisible(Game game) {
    List<Tile> currentTiles = getCurrentTiles();
    return game.isVisibleToMostRecentHumanPlayer(currentTiles.get(0))
        || (currentTiles.size() == 2 && game.isVisibleToMostRecentHumanPlayer(currentTiles.get(1)));
  }
}
