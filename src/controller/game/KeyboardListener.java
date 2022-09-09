package controller.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;

import model.board.Direction;
import view.gui.Frame;

/**
 * Manager for all keyboard input. Only singleton instance is allowed.
 */
public final class KeyboardListener implements KeyListener {

  /**
   * The up key code
   */
  public static final int UP = KeyEvent.VK_UP;
  /**
   * The left key code
   */
  public static final int LEFT = KeyEvent.VK_LEFT;
  /**
   * The down key code
   */
  public static final int DOWN = KeyEvent.VK_DOWN;
  /**
   * The right key code
   */
  public static final int RIGHT = KeyEvent.VK_RIGHT;
  /**
   * The "a" (main button / weak confirm button) key code
   */
  public static final int A = KeyEvent.VK_Z;
  /**
   * The "b" (secondary button / weak decline button) key code
   */
  public static final int B = KeyEvent.VK_X;
  /**
   * The "start" (tertiary / strong confirm button) key code
   */
  public static final int START = KeyEvent.VK_ENTER;
  /**
   * The "esc" (tertiary / strong decline button) key code
   */
  public static final int ESC = KeyEvent.VK_ESCAPE;
  /**
   * The unit cycling key for cycling through actionable units the player owns.
   */
  public static final int UNIT_CYCLE = KeyEvent.VK_D;

  /**
   * Keys that should be listend to for events. Others will be ignored.
   */
  private static final List<Integer> LISTENED_KEYS = Arrays.asList(A, B, START, ESC);

  /**
   * Creates a new keyboard listener and attaches it to the given frame.
   */
  static void attachToFrame(Frame f) {
    f.addKeyListener(new KeyboardListener());
  }

  /**
   * Constructor for KeyboardListener
   */
  private KeyboardListener() {
  }

  /**
   * Unused
   */
  @Override
  public void keyTyped(KeyEvent e) {
  }

  /**
   * Handle key presses
   */
  @Override
  public void keyPressed(KeyEvent e) {
    int keyCode = e.getKeyCode();

    // Cursors - respond to arrow keys
    Direction d = Direction.fromKeyCode(keyCode);
    if (d != null) {
      InputController.handleDirection(d);
    } else if (keyCode == UNIT_CYCLE) {
      InputController.handleUnitCycle();
    } else if (LISTENED_KEYS.contains(e.getKeyCode())) {
      InputController.handleConfirmOrCancel(e.getKeyCode() == A || e.getKeyCode() == START);
    }
  }

  /**
   * Unused
   */
  @Override
  public void keyReleased(KeyEvent e) {
  }

}
