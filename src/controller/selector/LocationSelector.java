package controller.selector;

import controller.game.GameController;
import model.board.Tile;
import view.gui.Paintable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** An instance represents a selector for any location with a given criteria */
public abstract class LocationSelector implements Paintable {
  /** The controller this is selecting in */
  public final GameController controller;

  /** The possible tiles the path could go to from here - possibilities cloud */
  protected List<Tile> cloud;

  /** Color for Cloud Drawing - translucent white - can be changed by subclasses */
  Color cloudColor = new Color(1, 1, 1, 0.5f);

  /**
   * Constructor for Location Selector. Implementing classes should refresh possibilities cloud at end of
   * construction
   */
  LocationSelector(GameController gc) {
    controller = gc;
    cloud = new ArrayList<>();
  }

  /** Empties and recalculated the possibilities cloud using the current path as set */
  protected abstract void refreshPossibilitiesCloud();

  /**
   * Returns the possible cloud. This is pass-by-value, so editing the returned set won't
   * change the PathSelector.
   */
  public ArrayList<Tile> getCloud() {
    return new ArrayList<>(cloud);
  }

  /** Draws this cloud */
  @Override
  public void paintComponent(Graphics g) {
    // Draw the possible movement cloud
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(cloudColor);

    for (Tile t : cloud) {
      int x = controller.getGamePanel().getXPosition(t);
      int y = controller.getGamePanel().getYPosition(t);
      g2d.fillRect(
          x, y, controller.getGamePanel().cellSize(), controller.getGamePanel().cellSize());
    }
  }
}
