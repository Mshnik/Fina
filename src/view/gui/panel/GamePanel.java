package view.gui.panel;

import controller.decision.Decision;
import controller.game.GameController;
import controller.selector.AttackSelector;
import controller.selector.CastSelector;
import controller.selector.LocationSelector;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Timer;
import model.board.Terrain;
import model.board.Tile;
import model.game.Game;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.building.StartOfTurnEffectBuilding;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.util.ExpandableCloud;
import view.gui.Frame;
import view.gui.MatrixPanel;
import view.gui.Paintable;
import view.gui.ViewOptions;
import view.gui.ViewOptions.ModifierViewType;
import view.gui.animation.CombatAnimation;
import view.gui.animation.MovementAnimation;
import view.gui.animation.UnitAnimation;
import view.gui.decision.DecisionPanel;
import view.gui.image.ImageIndex;
import view.gui.image.ImageIndex.DrawingBarSegment;
import view.gui.modifier.ModifierIcon;

/** Drawable wrapper for a model.board object */
public final class GamePanel extends MatrixPanel<Tile> implements Paintable, ComponentListener {

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

  /** Map of Unit -> ModifierIcon drawing for that unit. */
  private final Map<Unit, ModifierIcon> unitToModifierIconMap;

  /** The currently moving units, if any. Empty if none. */
  private final Map<Unit, UnitAnimation> unitMovementAnimationMap;

  /** The DecisionPanel that is currently active. Null if none */
  private DecisionPanel decisionPanel;

  /** Timer to trigger a resize to nearest square event, if one is running. */
  private Timer resizeTimer;

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
    unitToModifierIconMap = Collections.synchronizedMap(new HashMap<>());
    unitMovementAnimationMap = Collections.synchronizedMap(new HashMap<>());
    resizeTimer = null;
    setPreferredSize(new Dimension(getShowedCols() * cellSize(), getShowedRows() * cellSize()));
    addComponentListener(this);
  }

  /** Sets the decisionPanel */
  public void setDecisionPanel(DecisionPanel d) {
    decisionPanel = d;
  }

  /** Returns the current active decision panel, if any */
  public DecisionPanel getDecisionPanel() {
    return decisionPanel;
  }

  /** Adds a new MovementAnimation for the given movingUnit along the given path. */
  public void addUnitMovementAnimation(MovingUnit movingUnit, List<Tile> movementPath) {
    synchronized (unitMovementAnimationMap) {
      MovementAnimation animation = new MovementAnimation(this, movingUnit, movementPath);
      if (unitToModifierIconMap.containsKey(movingUnit)) {
        getFrame().getAnimator().removeAnimatable(unitMovementAnimationMap.get(movingUnit));
      }
      unitMovementAnimationMap.put(movingUnit, animation);
      getFrame().getAnimator().addAnimatable(animation);
    }
  }

  /** Adds a new MovementAnimation for the given combatant targeting the given target. */
  public void addCombatAnimation(Combatant combatant, Tile target) {
    synchronized (unitMovementAnimationMap) {
      CombatAnimation animation = new CombatAnimation(this, combatant, target);
      if (unitToModifierIconMap.containsKey(combatant)) {
        getFrame().getAnimator().removeAnimatable(unitMovementAnimationMap.get(combatant));
      }
      unitMovementAnimationMap.put(combatant, animation);
      getFrame().getAnimator().addAnimatable(animation);
    }
  }

  /** Cleans up unit animations, removing each that is no longer active. */
  private void cleanupUnitAnimations() {
    synchronized (unitMovementAnimationMap) {
      unitMovementAnimationMap
          .entrySet()
          .stream()
          .filter(e -> !e.getValue().isActive())
          .collect(Collectors.toSet())
          .forEach(e -> unitMovementAnimationMap.remove(e.getKey()));
    }
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

  /**
   * Recreates all modifierIcons for the given viewOptions. Should be called at the start of turn
   * and if a player changes their view options.
   */
  public void recreateModifierIconsForViewOptions(ViewOptions viewOptions) {
    synchronized (unitToModifierIconMap) {
      for (Unit u : unitToModifierIconMap.keySet()) {
        getFrame().getAnimator().removeAnimatable(unitToModifierIconMap.get(u));
        ModifierIcon modifierIcon = viewOptions.createModifierIconFor(this, u);
        getFrame().getAnimator().addAnimatable(modifierIcon);
        unitToModifierIconMap.put(u, modifierIcon);
      }
    }
  }

  /**
   * Refreshes the modifierIcon for the given unit. Does nothing if the modifierIcon doesn't exist
   * yet.
   */
  public void refreshModifierIconFor(Unit u) {
    synchronized (unitToModifierIconMap) {
      if (unitToModifierIconMap.containsKey(u)) {
        unitToModifierIconMap.get(u).refreshModifiers();
      }
    }
  }

  /**
   * Cleans up modifierIcons. Given the set of units, removes all panels that relate to a unit not
   * in the set.
   */
  private void cleanupModifierIcons(Set<Unit> units) {
    synchronized (unitToModifierIconMap) {
      unitToModifierIconMap
          .keySet()
          .stream()
          .filter(u -> !units.contains(u))
          .collect(Collectors.toSet())
          .forEach(
              u -> {
                getFrame().getAnimator().removeAnimatable(unitToModifierIconMap.get(u));
                unitToModifierIconMap.remove(u);
              });
    }
  }

  /** Paints this GamePanel, for use in the frame it is in. */
  @Override
  public void paintComponent(Graphics g) {
    Game game = controller.game;
    Graphics2D g2d = (Graphics2D) g;
    drawTilesAndUnits(g2d, game);

    // Draw danger radius for most recent human player player. May be empty.
    if (game.getMostRecentHumanPlayer() != null) {
      drawDangerRadius(g2d, game);
    }

    // Draw the locationSelector, if there is one
    if (controller.getLocationSelector() != null) {
      controller.getLocationSelector().paintComponent(g);
    }

    // Draw cloud of hovered start of turn building, if any
    if (controller.getLocationSelector() == null
        && getElm().isOccupied()
        && getElm().getOccupyingUnit() instanceof StartOfTurnEffectBuilding) {
      drawStartOfTurnBuildingRadius(g2d, game);
    }

    // Depending on the decision panel, draw ranges as applicable.
    // This happens when the action menu is up
    if (decisionPanel != null
        && controller.getDecisionType() == Decision.DecisionType.ACTION_DECISION) {
      g2d.setStroke(RADIUS_STROKE);
      switch (decisionPanel.cursor.getElm().getMessage()) {
        case GameController.FIGHT:
          drawAttackCloud(g2d);
          break;
        case GameController.BUILD:
        case GameController.SUMMON:
          drawSummonOrBuildCloud(g2d);
          break;
      }
    } else if (decisionPanel != null
            && controller.getDecisionType() == Decision.DecisionType.CAST_DECISION
        || controller.getLocationSelector() != null
            && controller.getToggle() == GameController.Toggle.CAST_SELECTION) {
      drawCastCloud(g2d);
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

  /** Draws the board of tiles and units. Also creates / tears down ModifierIcons as needed. */
  private void drawTilesAndUnits(Graphics2D g2d, Game game) {
    // Paint the model.board itself, painting the portion within
    // [scrollY ... scrollY + maxY - 1],
    // [scrollX ... scrollX + maxX - 1]

    int marginRowTop = marginY / 2;
    int marginRowBottom = marginY - marginRowTop;
    int marginColLeft = marginX / 2;
    int marginColRight = marginX - marginColLeft;
    HashSet<Unit> units = new HashSet<>();
    ViewOptions viewOptions = getFrame().getViewOptionsForPlayer(game.getMostRecentHumanPlayer());

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
          synchronized (unitMovementAnimationMap) {
            if (t.isOccupied() && !unitMovementAnimationMap.containsKey(t.getOccupyingUnit())) {
              Unit unit = t.getOccupyingUnit();
              units.add(unit);
              ModifierIcon modifierIcon;
              synchronized (unitToModifierIconMap) {
                if (unitToModifierIconMap.containsKey(unit)) {
                  modifierIcon = unitToModifierIconMap.get(unit);
                  modifierIcon.setFilterType(viewOptions.getModifierIconsFilterType());
                } else {
                  modifierIcon = viewOptions.createModifierIconFor(this, unit);
                  getFrame().getAnimator().addAnimatable(modifierIcon);
                  unitToModifierIconMap.put(unit, modifierIcon);
                }
              }
              if (game.isVisibleToMostRecentHumanPlayer(t)) {
                drawUnit(
                    g2d, unit, getXPosition(unit.getLocation()), getYPosition(unit.getLocation()));
                if (viewOptions.getModifierIconsViewType() == ModifierViewType.VIEW_ALL
                    || viewOptions.getModifierIconsViewType() == ModifierViewType.CURSOR_ONLY
                        && boardCursor.getElm() == unit.getLocation()) {
                  modifierIcon.paintComponent(g2d);
                }
              }
            }
          }
          if (getFrame().DEBUG) {
            g2d.setColor(Color.RED);
            g2d.drawString(t.getPoint().toString(), getXPosition(t), getYPosition(t) + 10);
          }
        }
      }
    }
    synchronized (unitMovementAnimationMap) {
      for (UnitAnimation animation : unitMovementAnimationMap.values()) {
        if (animation.isVisible(game)) {
          animation.paintComponent(g2d);
        }
      }
    }

    cleanupModifierIcons(units);
    cleanupUnitAnimations();
  }

  /** Draws the given tile. Doesn't do any model.unit drawing. */
  private void drawTile(Graphics2D g2d, Tile t) {
    int x = getXPosition(t);
    int y = getYPosition(t);
    // Draw terrain
    if (controller.game.getFogOfWar().hideAncientGround
        && t.terrain == Terrain.ANCIENT_GROUND
        && !controller.game.isVisibleToMostRecentHumanPlayer(t)) {
      g2d.drawImage(ImageIndex.imageForTerrain(Terrain.GRASS), x, y, cellSize(), cellSize(), null);
    } else {
      g2d.drawImage(ImageIndex.imageForTile(t), x, y, cellSize(), cellSize(), null);
    }

    // If the player can't see this tile, shade darkly.
    if (!controller.game.isVisibleToMostRecentHumanPlayer(t)) {
      g2d.setColor(FOG_OF_WAR);
      g2d.fillRect(x, y, cellSize(), cellSize());
    }
  }

  /** Draws the given model.unit. Doesn't do any tile drawing. */
  public void drawUnit(Graphics2D g2d, Unit u, int x, int y) {
    // Draw model.unit if it's alive.
    if (u.isAlive()) {
      BufferedImage unitImg;
      if (u instanceof Commander) {
        unitImg =
            ImageIndex.tint(u, controller.game.getCurrentPlayer(), controller.getColorFor(u.owner));
      } else if ((u instanceof Combatant || u instanceof Summoner)
          && !u.canAct()
          && u.owner == controller.game.getCurrentPlayer()) {
        unitImg = ImageIndex.tint(u, controller.game.getCurrentPlayer(), Color.GRAY);
      } else {
        unitImg = ImageIndex.imageForUnit(u, controller.game.getCurrentPlayer());
      }
      g2d.drawImage(unitImg, x, y, cellSize(), cellSize(), null);

      // Draw health bar.
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
  }

  /**
   * Draws the danger radius for the most recent human player. Should only be called if
   * mostRecentHumanPlayer isn't null.
   */
  private void drawDangerRadius(Graphics2D g2d, Game game) {
    Set<Tile> dangerRadius =
        getFrame().getViewOptionsForPlayer(game.getMostRecentHumanPlayer()).getDangerRadius();
    if (!dangerRadius.isEmpty()) {
      g2d.setColor(ATTACK_COLOR);
      g2d.setStroke(new BasicStroke(2));
      ImageIndex.trace(dangerRadius, this, g2d);
    }
  }

  /**
   * Draw hovered start of turn building. Should only be called if there is a hovered start of turn
   * building.
   */
  private void drawStartOfTurnBuildingRadius(Graphics2D g2d, Game game) {
    StartOfTurnEffectBuilding building = (StartOfTurnEffectBuilding) getElm().getOccupyingUnit();
    List<Tile> cloud =
        building.getEffect().cloud.translate(getElm().getPoint()).toTileSet(game.board);
    g2d.setColor(SUMMON_COLOR);
    g2d.setStroke(new BasicStroke(2));
    ImageIndex.trace(cloud, this, g2d);
  }

  /** Draws the attack cloud for the hovered unit. */
  private void drawAttackCloud(Graphics2D g2d) {
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
  }

  /** Draws the summon/build cloud for the hovered unit. */
  private void drawSummonOrBuildCloud(Graphics2D g2d) {
    g2d.setColor(SUMMON_COLOR);
    ImageIndex.trace(
        controller.game.board.getRadialCloud(
            boardCursor.getElm(), boardCursor.getElm().getOccupyingUnit().getSummonRange()),
        this,
        g2d);
  }

  /** Draws the cast cloud for the hovered ability. */
  private void drawCastCloud(Graphics2D g2d) {
    g2d.setStroke(RADIUS_STROKE);
    if (decisionPanel == null) {
      // Draw active casting.
      CastSelector selector = (CastSelector) controller.getLocationSelector();
      g2d.setColor(CAST_FILL_COLOR);
      ImageIndex.fill(selector.effectCloud, this, g2d);
      g2d.setColor(CAST_BORDER_COLOR);
      ImageIndex.trace(selector.effectCloud, this, g2d);
      return;
    }
    // Hovering ability in decision panel.
    Ability a = (Ability) decisionPanel.getElm().getVal();
    Player caster = boardCursor.getElm().getOccupyingUnit().owner;

    if (a.castDist == 0) {
      List<Tile> cloud =
          a.effectCloud
              .expand(a.canBeCloudBoosted ? caster.getCastCloudBoost() : 0)
              .translate(boardCursor.getElm().getPoint())
              .toTileSet(controller.game.board);
      g2d.setColor(CAST_FILL_COLOR);
      ImageIndex.fill(cloud, this, g2d);
      g2d.setColor(CAST_BORDER_COLOR);
      ImageIndex.trace(cloud, this, g2d);
    } else {
      List<Tile> selectableTiles =
          controller.game.board.getRadialCloud(
              boardCursor.getElm(),
              a.castDist + (a.canBeCloudBoosted ? 0 : caster.getCastSelectBoost()));
      selectableTiles.remove(boardCursor.getElm());
      if (selectableTiles.size() > 0) {
        List<Tile> sampleCloud =
            a.getTranslatedEffectCloud(
                (Commander) boardCursor.getElm().getOccupyingUnit(),
                selectableTiles.get(0),
                caster.getCastCloudBoost());
        g2d.setColor(LocationSelector.DEFAULT_COLOR);
        ImageIndex.fill(selectableTiles, this, g2d);
        g2d.setColor(CAST_FILL_COLOR);
        ImageIndex.fill(sampleCloud, this, g2d);
        g2d.setColor(CAST_BORDER_COLOR);
        ImageIndex.trace(sampleCloud, this, g2d);
      }
    }
  }

  /** Returns the currently selected element */
  public Tile getElm() {
    return boardCursor.getElm();
  }

  /** Returns the tile at the given row and col. Ignores scrolling and margins for this. */
  @Override
  public Tile getElmAt(int row, int col) throws IllegalArgumentException {
    return controller.game.board.getTileAt(row, col);
  }

  /** Returns the tile at the given row and col, including scrolling and margins. */
  public Tile getElmAtWithScrollingAndMargins(int row, int col) throws IllegalArgumentException {
    return controller.game.board.getTileAt(
        row - marginY / 2 + scrollY, col - marginX / 2 + scrollX);
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

  @Override
  public void componentResized(ComponentEvent e) {
    double heightInRows = (double) getHeight() / getElementHeight();
    double widthInCols = (double) getWidth() / getElementWidth();
    int rows = (int) Math.ceil(heightInRows);
    int cols = (int) Math.ceil(widthInCols);
    scrollX = Math.min(scrollX, Math.max(0, getMatrixWidth() - cols));
    scrollY = Math.min(scrollY, Math.max(0, getMatrixHeight() - rows));
    marginX = Math.max(0, cols - getMatrixWidth());
    marginY = Math.max(0, rows - getMatrixHeight());
    setShowedRows(rows);
    setShowedCols(cols);

    // If not exactly a factor of cell size, fire a resize timer.
    if ((int) heightInRows != rows || (int) widthInCols != cols) {
      if (resizeTimer != null) {
        resizeTimer.restart();
      } else {
        resizeTimer = new Timer(1000, event -> resizeToNearestCellSizeIncrement());
        resizeTimer.start();
      }
    }
  }

  /**
   * Triggers an after the fact resizing to make the panel to the nearest increment of correct size.
   */
  private void resizeToNearestCellSizeIncrement() {
    setPreferredSize(
        new Dimension(
            Math.round((float) getWidth() / getElementWidth()) * getElementWidth(),
            Math.round((float) getHeight() / getElementHeight()) * getElementHeight()));
    getFrame().pack();
    resizeTimer = null;
  }

  @Override
  public void componentMoved(ComponentEvent e) {}

  @Override
  public void componentShown(ComponentEvent e) {}

  @Override
  public void componentHidden(ComponentEvent e) {}
}
