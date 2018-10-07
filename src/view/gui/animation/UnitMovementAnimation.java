package view.gui.animation;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.board.Tile;
import model.unit.MovingUnit;
import model.unit.Unit;
import view.gui.panel.GamePanel;

/**
 * An animation representing a unit moving along a given path. Doesn't loop - active is set to false
 * after completing once.
 */
public final class UnitMovementAnimation implements Animatable {

  /**
   * The ratio of a frame to how far along a path (in tiles) the unit should move. Should be in the
   * range (0,1]. The higher the value the faster the animation.
   */
  private static final double TILE_MOVEMENT_PER_FRAME_RATIO = 0.5;

  private final GamePanel gamePanel;
  private final MovingUnit movingUnit;
  private final List<Tile> movementPath;

  /** The current state this is on. */
  private int state;

  /** True if this is active, false once the animation has been completed. */
  private boolean active;

  /** Creates a new animation for the given args. */
  public UnitMovementAnimation(
      GamePanel gamePanel, MovingUnit movingUnit, List<Tile> movementPath) {
    this.gamePanel = gamePanel;
    this.movingUnit = movingUnit;
    this.movementPath = movementPath;
    state = 0;
    active = true;
  }

  public Unit getUnit() {
    return movingUnit;
  }

  @Override
  public int getStateCount() {
    return (int) ((movementPath.size() - 1) / TILE_MOVEMENT_PER_FRAME_RATIO) + 1;
  }

  @Override
  public int getState() {
    return state;
  }

  @Override
  public void advanceState() {
    state = (state + 1) % getStateCount();
  }

  @Override
  public void setState(int state) {
    this.state = state;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  /** Returns the tiles this is currently on, either size 1 or 2. */
  public List<Tile> getCurrentTiles() {
    double pathState = state * TILE_MOVEMENT_PER_FRAME_RATIO;
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
      double pathState = state * TILE_MOVEMENT_PER_FRAME_RATIO;
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
    gamePanel.drawUnit((Graphics2D) g, movingUnit, x, y);
  }

  @Override
  public void animationCompleted() {
    gamePanel.getFrame().getAnimator().removeAnimatable(this);
    state = getStateCount() - 1;
    active = false;
  }
}
