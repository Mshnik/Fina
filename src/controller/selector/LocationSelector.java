package controller.selector;

import controller.game.GameController;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import model.board.Tile;
import view.gui.Paintable;
import view.gui.image.Colors;
import view.gui.image.ImageIndex;

/**
 * An instance represents a selector for any location with a given criteria
 */
public abstract class LocationSelector implements Paintable {
  private static final Stroke RADIUS_STROKE = new BasicStroke(3);


  /**
   * The controller this is selecting in
   */
  public final GameController controller;

  /**
   * The possible tiles the path could go to from here - possibilities cloud
   */
  protected List<Tile> cloud;

  /**
   * Color for Cloud Fill Drawing - translucent white - can be changed by subclasses
   */
  Color cloudFillColor = Colors.DEFAULT_CLOUD_FILL_COLOR;

  /**
   * Color for Cloud Trace Drawing - translucent white - can be changed by subclasses
   */
  Color cloudTraceColor = Colors.DEFAULT_CLOUD_TRACE_COLOR;

  /**
   * Color for CLoud effect fill - translucent grey - can be changed by subclasses
   */
  Color effectFillColor = Colors.DEFAULT_EFFECT_FILL_COLOR;

  /**
   * Color for Cloud effect trace - translucent grey - can be changed by subclasses
   */
  Color effectTraceColor = Colors.DEFAULT_EFFECT_TRACE_COLOR;

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

  /**
   * Return the current effect cloud.
   */
  public abstract List<Tile> getEffectCloud();

  /**
   * Empties and recalculates the effect cloud using the current path as set.
   */
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

    g2d.setStroke(RADIUS_STROKE);
    g2d.setColor(cloudFillColor);
    ImageIndex.fill(cloud, controller.getGamePanel(), g2d);
    g2d.setColor(cloudTraceColor);
    ImageIndex.trace(cloud, controller.getGamePanel(), g2d);
    g2d.setColor(effectFillColor);
    ImageIndex.fill(getEffectCloud(), controller.getGamePanel(), g2d);
    g2d.setColor(effectTraceColor);
    ImageIndex.trace(getEffectCloud(), controller.getGamePanel(), g2d);
  }
}
