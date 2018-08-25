package view.gui;

import controller.game.BoardReader;
import controller.game.GameController;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import model.game.Player;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combat;
import view.gui.panel.GamePanel;
import view.gui.panel.HeaderPanel;
import view.gui.panel.InfoPanel;

/** The visual frame that manages showing the model.game */
public final class Frame extends JFrame {

  /** */
  private static final long serialVersionUID = 1L;

  // Some nice fonts to consider:
  // Apple Chancery
  // Ayuthaya
  // Damascus
  // Herculanum
  // Kokonar
  // Libian Sc
  // Monotype corsiva
  // Papyrus
  /** The font to use for all text */
  public static final String FONTNAME = "Damascus";

  /** Restart game menu item. */
  private JMenuItem restartGameMenuItem;

  /** The headerPanel this Frame is drawing, if any */
  private HeaderPanel headerPanel;

  /** The gamePanel this Frame is drawing, if any */
  private GamePanel gamePanel;

  /** The infoPanel this Frame is drawing, if any */
  private InfoPanel infoPanel;

  /** The animator for this Frame */
  private Animator animator;

  /** The controller for this game */
  private GameController controller;

  /** The number of rows of tiles to show when this was created. May be zoomed in/out. */
  private final int originalRows;

  /** The number of colos of tiles to show when this was created. May be zoomed in/out. */
  private final int originalCols;

  /** Increment of zoom, in multiplication/division. */
  private static final double[] ZOOM = {0.25, 0.5, 0.75, 1, 1.5, 2};

  /** The current zoom of this frame. */
  private int zoomIndex;

  /** True iff debugging output/painting should be shown */
  public boolean DEBUG = false;

  /** The current active cursor */
  @SuppressWarnings("rawtypes")
  private Cursor activeCursor;

  /** Creates a new frame. */
  public Frame(int rows, int cols) {
    // Set frame defaults.
    setLayout(new BorderLayout());
    setResizable(false);
    setLocation(100, 100);
    animator = new Animator();
    this.originalRows = rows;
    this.originalCols = cols;
    zoomIndex = 2; // Use original number of rows and cols.

    // Set up menu
    JMenuBar menu = new JMenuBar();

    // Game menu - game-related functions.
    JMenu gameMenu = new JMenu("Game");
    menu.add(gameMenu);

    JMenuItem newGameMenuItem = new JMenuItem("New Game...");
    newGameMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));
    newGameMenuItem.addActionListener(
        e -> {
          animator.paused = true;
          String[] boardChoices = Paths.get(BoardReader.BOARDS_ROOT_FILEPATH).toFile().list();
          String input =
              (String)
                  JOptionPane.showInputDialog(
                      Frame.this,
                      "Choose Board to load.",
                      "New Game - Choose Board",
                      JOptionPane.QUESTION_MESSAGE,
                      null,
                      boardChoices,
                      boardChoices[0]);
          controller.loadAndKillThis(BoardReader.BOARDS_ROOT_FILEPATH + input);
        });
    gameMenu.add(newGameMenuItem);
    restartGameMenuItem = new JMenuItem("Restart Game");
    restartGameMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_DOWN_MASK));
    restartGameMenuItem.addActionListener(e -> controller.restart());
    restartGameMenuItem.setEnabled(false);
    gameMenu.add(restartGameMenuItem);
    JMenuItem quitGameMenuItem = new JMenuItem("Quit");
    quitGameMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.META_DOWN_MASK));
    quitGameMenuItem.addActionListener(e -> System.exit(0));
    gameMenu.add(quitGameMenuItem);

    // Window menu - visual functionality.
    JMenu windowMenu = new JMenu("Window");
    menu.add(windowMenu);

    JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
    zoomInMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.META_DOWN_MASK));
    zoomInMenuItem.addActionListener(
        e -> {
          zoomIndex += 1;
          zoomIndex = Math.min(zoomIndex, ZOOM.length - 1);
          createGamePanel();
          repaint();
          pack();
        });
    windowMenu.add(zoomInMenuItem);
    JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
    zoomOutMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_DOWN_MASK));
    zoomOutMenuItem.addActionListener(
        e -> {
          zoomIndex -= 1;
          zoomIndex = Math.max(zoomIndex, 0);
          createGamePanel();
          repaint();
          pack();
        });
    windowMenu.add(zoomOutMenuItem);
    JMenuItem drawDebugInfoMenuItem = new JCheckBoxMenuItem("Draw Debug Info");
    drawDebugInfoMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.META_DOWN_MASK));
    drawDebugInfoMenuItem.addActionListener(e -> {
      DEBUG = !DEBUG;
    });
    windowMenu.add(drawDebugInfoMenuItem);

    setJMenuBar(menu);
  }

  /**
   * Sets this Frame to show GamePanel bp. Removes previous gamePanel if any. Also triggers a
   * packing and repainting.
   */
  public void setController(GameController c) {
    // Removal - if this is called twice, should dispose of all old stuff.
    if (gamePanel != null) {
      remove(gamePanel);
      remove(headerPanel);
      remove(infoPanel);
      animator.clearAnimatables();
    }
    controller = c;
    // New Adding
    createGamePanel();
    HeaderPanel hp = new HeaderPanel(this);
    headerPanel = hp;
    add(hp, BorderLayout.NORTH);
    InfoPanel ip = new InfoPanel(this);
    add(ip, BorderLayout.SOUTH);
    infoPanel = ip;
    pack();
    repaint();
    setVisible(true);
    restartGameMenuItem.setEnabled(true);
  }

  /** Returns the current zoom. */
  public double getZoom() {
    return ZOOM[zoomIndex];
  }

  /** Helper to create the game panel for this. If old game panel existed, dispose first. */
  private void createGamePanel() {
    if (gamePanel != null) {
      animator.removeAnimatable(gamePanel.boardCursor);
      remove(gamePanel);
    }
    GamePanel gp =
        new GamePanel(this, (int) (originalRows / getZoom()), (int) (originalCols / getZoom()));
    add(gp, BorderLayout.CENTER);
    gamePanel = gp;
    activeCursor = gamePanel.boardCursor;
    animator.addAnimatable(gamePanel.boardCursor);
  }

  /** @return the headerPanel */
  public HeaderPanel getHeaderPanel() {
    return headerPanel;
  }

  /** @return the gamePanel */
  public GamePanel getGamePanel() {
    return gamePanel;
  }

  /** @return the infoPanel */
  public InfoPanel getInfoPanel() {
    return infoPanel;
  }

  /** Returns the controller currently controlling this frame */
  public GameController getController() {
    return controller;
  }

  /** Shows an alert saying that the game is over. */
  public void showGameOverAlert(Player winner) {
    JOptionPane.showMessageDialog(this, "Player " + winner.index + " wins!");
  }

  /**
   * Shows an alert asking the given player to confirm before starting turn. Only needed when fog of
   * war is on.
   */
  public void showPlayerChangeAlert(Player p) {
    gamePanel.boardCursor.hide = true;
    JOptionPane.showMessageDialog(this, "Player " + p.index + "'s Turn.");
  }

  /** Starts the turn for player p, making graphic updates as necessary */
  public void startTurnFor(Player p) {
    gamePanel.boardCursor.hide = false;
    gamePanel.boardCursor.setElm(p.getCommander().getLocation());
    gamePanel.boardCursor.moved();
  }

  /** Updates the info panel to show the given model.unit * */
  public void showUnitStats(Unit u) {
    infoPanel.setUnit(u, true);
  }

  /** Updates the info panel to show the given ability * */
  public void showAbilityStats(Ability a) {
    infoPanel.setAbility(a);
  }

  /** Updates the info panel to show the given Combat. */
  public void showCombatStats(Combat c) {
    infoPanel.setCombat(c);
  }

  /**
   * Returns the current active cursor that is moved by arrow keys. Because cursor has many
   * different implementations, casting is needed
   */
  @SuppressWarnings("rawtypes")
  public Cursor getActiveCursor() {
    return activeCursor;
  }

  /**
   * Returns the current active cursor that is moved by arrow keys. Because cursor has many
   * different implementations, casting is needed
   */
  @SuppressWarnings("rawtypes")
  public void setActiveCursor(Cursor c) {
    activeCursor = c;
    animator.addAnimatable(c);
  }

  /** Returns the object responsible for animating things for this frame */
  public Animator getAnimator() {
    return animator;
  }

  /** Returns the width (in pixels) of the given text using the given font */
  public int getTextWidth(Font f, String s) {
    FontMetrics m = getGraphics().getFontMetrics(f);
    int i = m.stringWidth(s);
    return i;
  }
}
