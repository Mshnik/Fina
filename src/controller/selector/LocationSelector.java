package controller.selector;

import controller.game.GameController;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import model.board.Tile;
import view.gui.Paintable;

/**
 * An instance represents a selector for any location with a given criteria
 */
public abstract class LocationSelector implements Paintable {
  /**
   * The default color for shading possible selectable locations in a LocationSelector. A
   * translucent white.
   */
  public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 0.5f);

  /**
   * The default color for shading cloud locations in a LocationSelector. A
   * translucent grey.
   */
  public static final Color DEFAULT_EFFECT_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);

  /**
   * The controller this is selecting in
   */
  public final GameController controller;

  /**
   * The possible tiles the path could go to from here - possibilities cloud
   */
  protected List<Tile> cloud;

  /**
   * Color for Cloud Drawing - translucent white - can be changed by subclasses
   */
  Color cloudColor = DEFAULT_COLOR;

  /** Color for CLoud effect drawing - translucent grey - can be changed by subclasses */
  Color effectColor = DEFAULT_EFFECT_COLOR;

  /**
   * Constructor for Location Selector. Implementing classes should refresh possibilities cloud at
   * end of construction
   */
  LocationSelector(GameController gc) {
    controller = gc;
    cloud = new ArrayList<>();
  }

  /**
   * Empties and recalculated the possibilities cloud using the current path as set
   */
  protected abstract void refreshPossibilitiesCloud();

  /** Return the current effect cloud. */
  public abstract List<Tile> getEffectCloud();

  /** Empties and recalculates the effect cloud using the current path as set. */
  public abstract void refreshEffectCloud();

  /**
   * Returns the possible cloud. This is pass-by-value, so editing the returned set won't change the
   * PathSelector.
   */
  public ArrayList<Tile> getCloud() {
    return new ArrayList<>(cloud);
  }

  /**
   * Draws this cloud
   */
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

    g2d.setColor(effectColor);
    for (Tile t : getEffectCloud()) {
      int x = controller.getGamePanel().getXPosition(t);
      int y = controller.getGamePanel().getYPosition(t);
      g2d.fillRect(
          x, y, controller.getGamePanel().cellSize(), controller.getGamePanel().cellSize());
    }
  }
}
