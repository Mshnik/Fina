package view.gui;

import java.awt.*;

/**
 * Implementing classes are able to be painted though perhaps as part of a containing classes'
 * painting method.
 *
 * @author MPatashnik
 */
public interface Paintable {

  /** Paint this Paintable using g */
  public void paintComponent(Graphics g);
}
