package view.gui.panel;

import controller.decision.Decision;
import controller.game.GameController;
import controller.selector.AttackSelector;
import controller.selector.CastSelector;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import model.board.Terrain;
import model.board.Tile;
import model.game.Game;
import model.game.Player;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.util.ExpandableCloud;
import view.gui.Frame;
import view.gui.ImageIndex;
import view.gui.ImageIndex.DrawingBarSegment;
import view.gui.MatrixPanel;
import view.gui.Paintable;
import view.gui.decision.DecisionPanel;

/** Drawable wrapper for a model.board object */
public final class GamePanel extends MatrixPanel<Tile> implements Paintable {

  /** */
  private static final long serialVersionUID = 1L;

  /** Pixels (size) for each square tile. */
  private static final int BASE_CELL_SIZE = 64;

  /** Shading for fog of war - translucent black */
  private static final Color FOG_OF_WAR = new Color(0, 0, 0, 0.75f);

  /** Stroke for drawing effect radii */
  private static final Stroke RADIUS_STROKE = new BasicStroke(3);

  /** Color of attack radii */
  private static final Color ATTACK_COLOR = Color.red;

  /** Color of summon/build radii */
  private static final Color SUMMON_COLOR = Color.cyan;

  /** Color of cast radii */
  private static final Color CAST_BORDER_COLOR = new Color(244, 231, 35);

  /** Color of cast radii */
  private static final Color CAST_FILL_COLOR = new Color(0.96f, 0.89f, 0.11f, 0.5f);

  /** The BoardCursor for this GamePanel */
  public final BoardCursor boardCursor;

  /** The DecisionPanel that is currently active. Null if none */
  private DecisionPanel decisionPanel;

  /**
   * Constructor for GamePanel
   *
   * @param f
   * @param maxRows - the maximum number of rows of tiles to show at a time
   * @param maxCols - the maximum number of cols of tiles to show at a time
   */
  public GamePanel(Frame f, int maxRows, int maxCols) {
    super(
        f.getController(),
        maxCols,
        maxRows,
        0,
        0,
        Math.max(0, maxCols - f.getController().game.board.getWidth()),
        Math.max(0, maxRows - f.getController().game.board.getHeight()));
    boardCursor = new BoardCursor(this);
    setPreferredSize(new Dimension(getShowedCols() * cellSize(), getShowedRows() * cellSize()));
  }

  /** Sets the decisionPanel */
  public void setDecisionPanel(DecisionPanel d) {
    decisionPanel = d;
  }

  /** Returns the current active decision panel, if any */
  public DecisionPanel getDecisionPanel() {
    return decisionPanel;
  }

  /**
   * Creates a decisionPanel with the given decisionArray. Fixes the location of the open
   * decisionPanel for the location of the boardCursor, sets active toggle and active cursor, and
   * repaints.
   */
  public void fixDecisionPanel(String title, Player p, Decision decision, boolean verticalLayout) {
    decisionPanel =
        new DecisionPanel(
            controller, p, Math.min(4, decision.size()), title, decision, verticalLayout);
    getFrame().setActiveCursor(decisionPanel.cursor);
  }

  /** Moves the decision panel to accomidate the current location of the boardCursor */
  public void moveDecisionPanel() {
    Tile t = boardCursor.getElm();
    int x = getXPosition(t);
    if (x < getWidth() / 2) {
      x += cellSize() + 5;
    } else {
      x -= (decisionPanel.DECISION_WIDTH + 5);
    }
    int y = getYPosition(t);
    if (y > getHeight() / 2) {
      y = getYPosition(t) - decisionPanel.getHeight() + cellSize();
    }
    decisionPanel.setXPosition(x);
    decisionPanel.setYPosition(y);
    repaint();
  }

  /** Moves the decision panel to the center of the screen */
  public void centerDecisionPanel() {
    int w = decisionPanel.getWidth();
    int h = decisionPanel.getHeight();

    decisionPanel.setXPosition((getWidth() - w) / 2);
    decisionPanel.setYPosition((getHeight() - h) / 2);
    repaint();
  }

  /** Paints this boardpanel, for use in the frame it is in. */
  @Override
  public void paintComponent(Graphics g) {
    Game game = controller.game;
    Graphics2D g2d = (Graphics2D) g;
    // Paint the model.board itself, painting the portion within
    // [scrollY ... scrollY + maxY - 1],
    // [scrollX ... scrollX + maxX - 1]

    int marginRowTop = marginY / 2;
    int marginRowBottom = marginY - marginRowTop;
    int marginColLeft = marginX / 2;
    int marginColRight = marginX - marginColLeft;

    for (int row = 0; row < getShowedRows(); row++) {
      for (int col = 0; col < getShowedCols(); col++) {
        if (row < marginRowTop
            || row >= (getShowedRows() - marginRowBottom)
            || col < marginColLeft
            || col >= (getShowedCols() - marginColRight)) {
          g2d.drawImage(
              ImageIndex.margin(),
              col * getElementWidth(),
              row * getElementHeight(),
              cellSize(),
              cellSize(),
              null);
        } else {
          Tile t =
              game.board.getTileAt(row + scrollY - marginRowTop, col + scrollX - marginColLeft);
          drawTile(g2d, t);
          if (t.isOccupied() && game.isVisible(t)) {
            drawUnit(g2d, t.getOccupyingUnit());
          }
          if (getFrame().DEBUG) {
            g2d.setColor(Color.RED);
            g2d.drawString(t.getPoint().toString(), getXPosition(t), getYPosition(t) + 10);
          }
        }
      }
    }

    // Draw the locationSelector, if there is one
    if (controller.getLocationSelector() != null) {
      controller.getLocationSelector().paintComponent(g);
    }

    // Depending on the decision panel, draw ranges as applicable.
    // This happens when the action menu is up
    if (decisionPanel != null
        && controller.getDecisionType() == Decision.DecisionType.ACTION_DECISION) {
      g2d.setStroke(RADIUS_STROKE);
      switch (decisionPanel.cursor.getElm().getMessage()) {
        case GameController.FIGHT:
          List<Tile> tiles =
              ExpandableCloud.create(
                      ExpandableCloud.ExpandableCloudType.CIRCLE,
                      boardCursor.getElm().getOccupyingUnit().getMaxAttackRange() + 1)
                  .difference(
                      ExpandableCloud.create(
                          ExpandableCloud.ExpandableCloudType.CIRCLE,
                          boardCursor.getElm().getOccupyingUnit().getMinAttackRange()))
                  .translate(boardCursor.getElm().getPoint())
                  .toTileSet(controller.game.board);
          g2d.setColor(AttackSelector.SHADE_COLOR);
          ImageIndex.fill(tiles, this, g2d);
          g2d.setColor(ATTACK_COLOR);
          ImageIndex.trace(tiles, this, g2d);
          break;
        case GameController.BUILD:
        case GameController.SUMMON:
          g2d.setColor(SUMMON_COLOR);
          ImageIndex.trace(
              controller.game.board.getRadialCloud(
                  boardCursor.getElm(), boardCursor.getElm().getOccupyingUnit().getSummonRange()),
              this,
              g2d);
          break;
      }
    } else if (decisionPanel != null
            && controller.getDecisionType() == Decision.DecisionType.CAST_DECISION
        || controller.getLocationSelector() != null
            && controller.getToggle() == GameController.Toggle.CAST_SELECTION) {
      Ability a = null;
      if (decisionPanel != null) {
        a = (Ability) decisionPanel.getElm().getVal();
      } else {
        CastSelector selector = (CastSelector) controller.getLocationSelector();
        g2d.setStroke(RADIUS_STROKE);
        g2d.setColor(CAST_FILL_COLOR);
        ImageIndex.fill(selector.effectCloud, this, g2d);
        g2d.setColor(CAST_BORDER_COLOR);
        ImageIndex.trace(selector.effectCloud, this, g2d);
      }
    }

    // Draw the cursor if it's not hidden.
    if (!boardCursor.hide) {
      boardCursor.paintComponent(g);
    }

    // Draw the decisionPanel
    if (decisionPanel != null) {
      decisionPanel.paintComponent(g);
    }
  }

  /** Draws the given tile. Doesn't do any model.unit drawing. */
  private void drawTile(Graphics2D g2d, Tile t) {
    int x = getXPosition(t);
    int y = getYPosition(t);
    // Draw terrain
    if (controller.game.getFogOfWar().hideAncientGround
        && t.terrain == Terrain.ANCIENT_GROUND
        && !controller.game.isVisible(t)) {
      g2d.drawImage(ImageIndex.imageForTerrain(Terrain.GRASS), x, y, cellSize(), cellSize(), null);
    } else {
      g2d.drawImage(ImageIndex.imageForTerrain(t.terrain), x, y, cellSize(), cellSize(), null);
    }

    // If the player can't see this tile, shade darkly.
    if (!controller.game.isVisible(t)) {
      g2d.setColor(FOG_OF_WAR);
      g2d.fillRect(x, y, cellSize(), cellSize());
    }
  }

  /** Draws the given model.unit. Doesn't do any tile drawing. */
  private void drawUnit(Graphics2D g2d, Unit u) {
    int x = getXPosition(u.getLocation());
    int y = getYPosition(u.getLocation());

    // Draw model.unit
    BufferedImage unitImg = ImageIndex.imageForUnit(u, controller.game.getCurrentPlayer());
    g2d.drawImage(unitImg, x, y, cellSize(), cellSize(), null);

    // Draw health bar
    final int marginX = 4; // Room from left side of tile
    final int marginY = 4; // Room from BOTTOM side of tile
    final int barX = x + marginX;
    final int barY = y + cellSize() - marginY * 2;
    ImageIndex.drawBar(
        g2d,
        barX,
        barY,
        cellSize() - marginX * 2,
        marginY,
        null,
        null,
        0,
        u.getMaxHealth(),
        DrawingBarSegment.listOf(Color.red, u.getHealthPercent()),
        null,
        null,
        null,
        null,
        0);
  }

  /** Returns the currently selected element */
  public Tile getElm() {
    return boardCursor.getElm();
  }

  /** Returns the tile at the given row and col. Ignores scrolling for this. */
  @Override
  public Tile getElmAt(int row, int col) throws IllegalArgumentException {
    return controller.game.board.getTileAt(row, col);
  }

  /** Returns the width of the model.board's matrix */
  @Override
  public int getMatrixWidth() {
    return controller.game.board.getWidth();
  }

  /** Returns the height of the model.board's matrix */
  @Override
  public int getMatrixHeight() {
    return controller.game.board.getHeight();
  }

  /** Returns the size of cells, based on the current zoom. */
  public int cellSize() {
    return (int) (BASE_CELL_SIZE * controller.frame.getZoom());
  }

  /** Returns GamePanel.cellSize() */
  @Override
  public int getElementHeight() {
    return cellSize();
  }

  /** Returns GamePanel.cellSize() */
  @Override
  public int getElementWidth() {
    return cellSize();
  }
}
