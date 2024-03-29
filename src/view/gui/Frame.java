package view.gui;

import controller.audio.AudioController;
import controller.game.CreatePlayerOptions;
import controller.game.GameController;
import controller.game.MouseListener;
import model.game.Player;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combat;
import model.unit.modifier.Modifiers.ModifierDescription;
import view.gui.NewGameSelector.NewGameOptions;
import view.gui.animation.Animator;
import view.gui.panel.GamePanel;
import view.gui.panel.HeaderPanel;
import view.gui.panel.InfoPanel;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The visual frame that manages showing the model.game
 */
public final class Frame extends JFrame {

  /**
   *
   */
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
  /**
   * The font to use for all text
   */
  public static final String FONTNAME = "Damascus";

  /**
   * The JMenu at the top of the frame.
   */
  private final JMenuBar menu;

  /**
   * Restart game menu item.
   */
  private final JMenuItem restartGameMenuItem;

  /**
   * The headerPanel this Frame is drawing, if any
   */
  private HeaderPanel headerPanel;

  /**
   * The gamePanel this Frame is drawing, if any
   */
  private GamePanel gamePanel;

  /**
   * The infoPanel this Frame is drawing, if any
   */
  private InfoPanel infoPanel;

  /**
   * The animator for this Frame
   */
  private final Animator animator;

  /**
   * The controller for this game
   */
  private GameController controller;

  /**
   * Increment of zoom, in multiplication/division.
   */
  private static final double[] ZOOM = {0.25, 0.5, 0.75, 1, 1.5, 2};

  /**
   * The current zoom of this frame.
   */
  private int zoomIndex;

  /**
   * True iff debugging output/painting should be shown
   */
  public boolean DEBUG = false;

  /**
   * The current active cursor
   */
  @SuppressWarnings("rawtypes")
  private Cursor activeCursor;

  /**
   * A map from player index to ViewOptions.
   */
  private final Map<Integer, ViewOptions> viewOptionsMap;

  /**
   * Creates a new frame.
   */
  public Frame(int zoom) {
    // Set frame defaults.
    setLayout(new BorderLayout());
    setLocation(100, 100);
    animator = new Animator();
    zoomIndex = zoom;
    viewOptionsMap = new HashMap<>();

    // Set up menu
    menu = new JMenuBar();

    // Game menu - game-related functions.
    JMenu gameMenu = new JMenu("Game");
    menu.add(gameMenu);

    JMenuItem newGameMenuItem = new JMenuItem("New Game...");
    newGameMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));
    newGameMenuItem.addActionListener(
        e -> {
          animator.setPaused(true);
          NewGameOptions newGameOptions = NewGameSelector.getNewGame(this);
          if (!newGameOptions.cancelled) {
            controller.loadAndKillThis(
                newGameOptions.boardFilepath,
                IntStream.range(0, newGameOptions.playerTypes.size())
                    .mapToObj(
                        i ->
                            new CreatePlayerOptions(
                                newGameOptions.playerTypes.get(i),
                                newGameOptions.loadAIOptions.get(i).filename,
                                newGameOptions.loadAIOptions.get(i).row))
                    .collect(Collectors.toList()),
                newGameOptions.fogOfWar,
                newGameOptions.startingCommanderLevel);
          } else {
            animator.setPaused(false);
          }
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

    // Input menu - input control.
    JMenu inputMenu = new JMenu("Input");
    menu.add(inputMenu);

    JCheckBoxMenuItem mouseControlInputMenuItem = new JCheckBoxMenuItem("Enable Mouse Input");
    mouseControlInputMenuItem.setSelected(true);
    mouseControlInputMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.META_DOWN_MASK));
    mouseControlInputMenuItem.addActionListener(
        e -> {
          if (mouseControlInputMenuItem.isSelected()) {
            MouseListener.attachToFrame(Frame.this);
          } else {
            MouseListener.detachFromFrame(Frame.this);
          }
        });
    inputMenu.add(mouseControlInputMenuItem);

    // Window menu - visual functionality.
    JMenu windowMenu = new JMenu("Window");
    menu.add(windowMenu);

    JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
    zoomInMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.META_DOWN_MASK));
    zoomInMenuItem.addActionListener(
        e -> {
          double oldZoom = getZoom();
          zoomIndex += 1;
          zoomIndex = Math.min(zoomIndex, ZOOM.length - 1);
          double zoomRatio = getZoom() / oldZoom;
          createGamePanel(
              (int) (gamePanel.getShowedRows() / zoomRatio),
              (int) (gamePanel.getShowedCols() / zoomRatio));
          repaint();
          pack();
        });
    windowMenu.add(zoomInMenuItem);
    JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
    zoomOutMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.META_DOWN_MASK));
    zoomOutMenuItem.addActionListener(
        e -> {
          double oldZoom = getZoom();
          zoomIndex -= 1;
          zoomIndex = Math.max(zoomIndex, 0);
          double zoomRatio = getZoom() / oldZoom;
          createGamePanel(
              (int) (gamePanel.getShowedRows() / zoomRatio),
              (int) (gamePanel.getShowedCols() / zoomRatio));
          repaint();
          pack();
        });
    windowMenu.add(zoomOutMenuItem);
    JMenuItem drawDebugInfoMenuItem = new JCheckBoxMenuItem("Draw Debug Info");
    drawDebugInfoMenuItem.setAccelerator(
        KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.META_DOWN_MASK));
    drawDebugInfoMenuItem.addActionListener(
        e -> {
          DEBUG = !DEBUG;
        });
    windowMenu.add(drawDebugInfoMenuItem);

    // Sound menu
    JMenu soundMenu = new JMenu("Sound");
    menu.add(soundMenu);

    JCheckBoxMenuItem muteMenuItem = new JCheckBoxMenuItem("Mute");
    muteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.META_DOWN_MASK));
    muteMenuItem.addActionListener(e -> AudioController.setMute(muteMenuItem.getState()));
    muteMenuItem.setState(AudioController.getMute());
    soundMenu.add(muteMenuItem);

    setJMenuBar(menu);
  }

  /**
   * Sets this Frame to show GamePanel bp. Removes previous gamePanel if any. Also triggers a
   * packing and repainting.
   */
  public void setController(GameController c, int rows, int cols) {
    // Removal - if this is called twice, should dispose of all old stuff.
    if (gamePanel != null) {
      remove(gamePanel);
      remove(headerPanel);
      remove(infoPanel);
      animator.clearAnimatables();
      viewOptionsMap.clear();
    }
    controller = c;

    // New Visual setup
    createGamePanel(rows, cols);
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

  /**
   * Returns the index of the current zoom.
   */
  public int getZoomIndex() {
    return zoomIndex;
  }

  /**
   * Returns the current zoom.
   */
  public double getZoom() {
    return ZOOM[zoomIndex];
  }

  /**
   * Creates default view options for the given player.
   */
  public void createViewOptionsForPlayer(Player p) {
    viewOptionsMap.put(p.index, new ViewOptions(this, p));
  }

  /**
   * Returns the view options for the given player.
   */
  public ViewOptions getViewOptionsForPlayer(Player p) {
    return viewOptionsMap.get(p.index);
  }

  /**
   * Clears the danger radius view options for the given player.
   */
  public void clearDangerRadiusForPlayer(Player p) {
    viewOptionsMap.get(p.index).clearDangerRadiusUnits();
  }

  /**
   * Called when a unit's danger radius changes. Propagates changes to ViewOptions
   */
  public void unitDangerRadiusChanged(Unit u) {
    for (ViewOptions viewOptions : viewOptionsMap.values()) {
      viewOptions.unitDangerRadiusChanged(u);
    }
    viewOptionsMap.get(u.owner.index).unitDangerRadiusChanged();
  }

  /**
   * Helper to create the game panel for this. If old game panel existed, dispose first.
   */
  private void createGamePanel(int rows, int cols) {
    if (gamePanel != null) {
      animator.removeAnimatable(gamePanel.boardCursor);
      remove(gamePanel);
    }
    GamePanel gp = new GamePanel(this, rows, cols);
    add(gp, BorderLayout.CENTER);
    gamePanel = gp;
    activeCursor = gamePanel.boardCursor;
    animator.addAnimatable(gamePanel.boardCursor);
  }

  /**
   * @return the JMenuBar at the top of this Frame.
   */
  public JMenuBar getMenu() {
    return menu;
  }

  /**
   * @return the headerPanel
   */
  public HeaderPanel getHeaderPanel() {
    return headerPanel;
  }

  /**
   * @return the gamePanel
   */
  public GamePanel getGamePanel() {
    return gamePanel;
  }

  /**
   * @return the infoPanel
   */
  public InfoPanel getInfoPanel() {
    return infoPanel;
  }

  /**
   * Returns the controller currently controlling this frame
   */
  public GameController getController() {
    return controller;
  }

  /**
   * Shows an alert saying that the game is over. If winner is null, this game timed out.
   */
  public void showGameOverAlert(Player winner) {
    if (winner == null) {
      JOptionPane.showMessageDialog(this, "Tie due to timeout");
    } else {
      JOptionPane.showMessageDialog(this, "Player " + winner.index + " wins!");
    }
  }

  /**
   * Shows an alert asking the given player to confirm before starting turn. Only needed when fog of
   * war is on.
   */
  public void showPlayerChangeAlert(Player p) {
    gamePanel.boardCursor.hide = true;
    JOptionPane.showMessageDialog(this, "Player " + p.index + "'s Turn.");
  }

  /**
   * Starts the turn for player p, making graphic updates as necessary
   */
  public void startTurnFor(Player p) {
    gamePanel.boardCursor.hide = false;
    if (p.isLocalHumanPlayer()) {
      gamePanel.recreateModifierIconsForViewOptions(viewOptionsMap.get(p.index));
      gamePanel.boardCursor.setElm(p.getCommander().getLocation());
      gamePanel.boardCursor.moved();
    }
  }

  /**
   * Updates the info panel to show the given model.unit *
   */
  public void showUnitStats(Unit u) {
    infoPanel.setUnit(u, true);
  }

  /**
   * Updates the info panel to show the given ModifierDescription for the given unit.
   */
  public void showModifierDescription(ModifierDescription modifierDescription) {
    infoPanel.setModifierDescription(modifierDescription);
  }

  /**
   * Updates the info panel to show the given ability *
   */
  public void showAbilityStats(Ability a) {
    infoPanel.setAbility(a);
  }

  /**
   * Updates the info panel to show the given Combat.
   */
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

  /**
   * Returns the object responsible for animating things for this frame
   */
  public Animator getAnimator() {
    return animator;
  }

  /**
   * Returns the width (in pixels) of the given text using the given font
   */
  public int getTextWidth(Font f, String s) {
    FontMetrics m = getGraphics().getFontMetrics(f);
    int i = m.stringWidth(s);
    return i;
  }
}
