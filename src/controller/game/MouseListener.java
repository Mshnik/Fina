package controller.game;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

import view.gui.Frame;

/**
 * A controller that allows the player to control the game with the mouse.
 */
public final class MouseListener implements MouseInputListener {

  /**
   * Instance attached to frames. Only one created to prevent having two different mouse listeners
   * listening.
   */
  private static final MouseListener instance = new MouseListener();

  /**
   * Attach a MouseListener to the given frame.
   */
  public static void attachToFrame(Frame frame) {
    frame.addMouseListener(instance);
    frame.addMouseMotionListener(instance);
  }

  /**
   * Detaches the MouseListener from the given frame.
   */
  public static void detachFromFrame(Frame frame) {
    frame.removeMouseListener(instance);
    frame.removeMouseMotionListener(instance);
  }

  /**
   * Prevent instantiation.
   */
  private MouseListener() {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    InputController.handleConfirmOrCancel(e.getButton() == MouseEvent.BUTTON1);
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    InputController.handleMouseHover(e.getX(), e.getY());
  }
}
