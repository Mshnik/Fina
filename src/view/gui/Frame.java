package view.gui;

import controller.game.GameController;
import controller.game.KeyboardListener;
import model.game.Player;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combat;
import view.gui.panel.GamePanel;
import view.gui.panel.HeaderPanel;
import view.gui.panel.InfoPanel;

import javax.swing.*;
import java.awt.*;

/** The visual frame that manages showing the model.game */
public final class Frame extends JFrame {

  /** */
  private static final long serialVersionUID = 1L;

  /** True iff debugging output/painting should be shown */
  public static final boolean DEBUG = true;

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

  /** The number of rows of tiles to show */
  private final int rows;

  /** The number of cols of tiles to show */
  private final int cols;

  /** The current active cursor */
  @SuppressWarnings("rawtypes")
  private Cursor activeCursor;

  public Frame(int rows, int cols) {
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    setResizable(false);
    setLocation(100, 100);
    animator = new Animator();
    KeyboardListener.setFrame(this);
    this.rows = rows;
    this.cols = cols;
  }

  /**
   * Sets this Frame to show GamePanel bp. Removes previous gamePanel if any. Also triggers a
   * packing and repainting.
   */
  public void setController(GameController c) {
    // Removal
    if (gamePanel != null) {
      remove(gamePanel);
      remove(headerPanel);
      animator.removeAnimatable(gamePanel.boardCursor);
    }
    controller = c;
    // New Adding
    GamePanel gp = new GamePanel(this, rows, cols);
    add(gp, BorderLayout.CENTER);
    gamePanel = gp;
    activeCursor = gamePanel.boardCursor;
    animator.addAnimatable(gamePanel.boardCursor);
    HeaderPanel hp = new HeaderPanel(this);
    headerPanel = hp;
    add(hp, BorderLayout.NORTH);
    InfoPanel ip = new InfoPanel(this);
    add(ip, BorderLayout.SOUTH);
    infoPanel = ip;
    pack();
    repaint();
    setVisible(true);
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

  /** Starts the turn for player p, making graphic updates as necessary */
  public void startTurnFor(Player p) {
    gamePanel.boardCursor.setElm(p.getCommander().getLocation());
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
