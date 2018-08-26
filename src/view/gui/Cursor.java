package view.gui;

import controller.game.MatrixElement;
import model.board.Direction;

import java.awt.*;

/**
 * An abstract cursor implementation that is able to select things in any matrix
 *
 * @param <T> - the type of the elements in the matrix this is choosing from.
 */
public abstract class Cursor<T extends MatrixElement, M extends MatrixPanel<T>>
    implements Animatable {

  /** Thickness of lines in Cursor Drawing */
  protected static final int THICKNESS = 3;

  /** Default color of cursor. */
  public static final Color DEFAULT_COLOR = Color.red;

  /**
   * The current animation state this BoardCursor is on. An interger in the range [0 ...
   * getStateCount() - 1]
   */
  private int animationState;

  /** The elm the cursor is currently on */
  private T elm;

  /** The Panel this cursor is drawn on. Reference kept to call updates to it */
  protected final M panel;

  /** Color for this cursor. Defaults to Default color. */
  private Color color = DEFAULT_COLOR;

  public Cursor(M panel, T startingElm) {
    this.panel = panel;
    elm = startingElm;
    animationState = 0;
  }

  /** Returns the element this cursor is currently on */
  public T getElm() {
    return elm;
  }

  /** Sets the tile this cursor is on */
  public void setElm(T t) {
    elm = t;
    panel.repaint();
  }

  /** Returns the row in the matrix this Cursor is currently on */
  public int getRow() {
    return elm.getRow();
  }

  /** Returns the col in the matrix this Cursor is currently on */
  public int getCol() {
    return elm.getCol();
  }

  /** Returns the matrix panel this is drawing for */
  public M getPanel() {
    return panel;
  }

  /** Returns true if the cursor can select the current selected elm, false otehrwise */
  public abstract boolean canSelect();

  /**
   * Called internally whenever a move would occur. Do validation, return true if move is ok, false
   * otherwise
   */
  protected abstract boolean willMoveTo(Direction d, T destination);

  /** Call to move in the given direction, if possible */
  public void move(Direction d) {
    T dest = getPanel().getElmInDirection(getElm(), d);

    if (willMoveTo(d, dest)) {
      setElm(dest);
      moved();
    }
  }

  /**
   * Called after a move occurs to do painting and the like. Can be overriden, but this method
   * should be called first before adding new behavior
   */
  protected void moved() {
    panel.fixScrollToShow(getRow(), getCol());
  }

  /** Sets the color of this cursor. */
  public void setColor(Color c) {
    color = c;
  }

  /** Returns the color of the cursor. */
  public Color getColor() {
    return color;
  }

  /** Draw this Cursor as a red set of 4 corner lines */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(getColor());
    g2d.setStroke(new BasicStroke(THICKNESS));

    // x min, y min, side length
    int x = getPanel().getXPosition(getElm()) + THICKNESS / 2;
    int y = getPanel().getYPosition(getElm()) + THICKNESS / 2;
    int h = getPanel().getElementHeight();
    int w = getPanel().getElementWidth();

    // delta - depends on animation state. when non 0, makes cursor smaller.
    int d = Math.abs(animationState - (getStateCount() + 1) / 2);

    // (x,y) coordinates for polylines. Each is 3 points, clockwise.
    int[][][] coords = {
      {
        // Top left corner
        {x + d, x + d, x + d + w / 4},
        {y + d + h / 4, y + d, y + d}
      },
      {
        // Top Right corner
        {x + 3 * w / 4 - d - THICKNESS, x + w - d - THICKNESS, x + w - d - THICKNESS},
        {y + d, y + d, y + d + h / 4}
      },
      {
        // Bottom Right corner
        {x + w - d - THICKNESS, x + w - d - THICKNESS, x + 3 * w / 4 - d - THICKNESS},
        {y + 3 * h / 4 - d - THICKNESS, y + h - d - THICKNESS, y + h - d - THICKNESS}
      },
      {
        // Bottom left corner
        {x + w / 4 + d, x + d, x + d},
        {y + h - d - THICKNESS, y + h - d - THICKNESS, y + 3 * h / 4 - d - THICKNESS}
      }
    };

    for (int i = 0; i < coords.length; i++) {
      g2d.drawPolyline(coords[i][0], coords[i][1], coords[i][0].length);
    }
  }

  /** Cursors have a state length of some fraction of a second. */
  @Override
  public int getStateLength() {
    return 75;
  }

  /** Cursors have 7 states */
  @Override
  public int getStateCount() {
    return 7;
  }

  /** Sets the animation state */
  @Override
  public void setState(int state) {
    animationState = state % getStateCount();
  }

  /** Returns the animation state this cursor is on */
  @Override
  public int getState() {
    return animationState;
  }

  /** Increases the animation state by 1, and causes a repaint */
  @Override
  public void advanceState() {
    animationState = (animationState + 1) % getStateCount();
    panel.getFrame().repaint();
  }

  /** This is active so long as it's the frame's active cursor */
  @Override
  public boolean isActive() {
    return this == panel.getFrame().getActiveCursor();
  }

  /** Simple toString that works off of the toString of the selected element */
  @Override
  public String toString() {
    return "Cursor on " + elm.toString();
  }
}
