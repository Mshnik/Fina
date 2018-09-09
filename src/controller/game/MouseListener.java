package controller.game;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import view.gui.Frame;

/** A controller that allows the player to control the game with the mouse. */
final class MouseListener implements MouseInputListener {

  /** Attach a MouseListener to the given frame. */
  static void attachToFrame(Frame frame) {
    MouseListener mouseListener = new MouseListener();
    frame.addMouseListener(mouseListener);
    frame.addMouseMotionListener(mouseListener);
  }

  /** Prevent instantiation. */
  private MouseListener() {}

  @Override
  public void mouseClicked(MouseEvent e) {
    InputController.handleConfirmOrCancel(e.getButton() == MouseEvent.BUTTON1);
  }

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {}

  @Override
  public void mouseMoved(MouseEvent e) {
    InputController.handleMouseHover(e.getX(), e.getY());
  }
}
