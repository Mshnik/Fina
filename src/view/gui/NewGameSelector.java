package view.gui;

import controller.game.BoardReader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import model.board.Board;
import model.board.Terrain;
import model.game.Game.FogOfWar;

/** Blocking top-level selector that allows the user to create a new game. */
final class NewGameSelector {

  /** NewGameOptions returned by a NewGameSelector. */
  static final class NewGameOptions {
    final boolean cancelled;
    final String boardFilepath;
    final int numPlayers;
    final FogOfWar fogOfWar;

    private NewGameOptions() {
      cancelled = true;
      boardFilepath = null;
      numPlayers = -1;
      fogOfWar = FogOfWar.NONE;
    }

    private NewGameOptions(String boardFilepath, int numPlayers, FogOfWar fogOfWar) {
      cancelled = false;
      this.boardFilepath = boardFilepath;
      this.numPlayers = numPlayers;
      this.fogOfWar = fogOfWar;
    }
  }

  /** Visual panel that displays the options for creating a new game. */
  private static final class NewGamePanel extends JPanel {
    private final JComboBox<String> boardSelector;
    private final JComboBox<Integer> numPlayersSelector;
    private final JComboBox<FogOfWar> fogOfWarSelector;
    private final Map<String, Board> boardsMap;

    private final BoardPreviewPanel boardPreviewPanel;

    NewGamePanel() {
      setLayout(new BorderLayout());

      // Populate boards map - read all boards from storage.
      String[] boards = Paths.get(BoardReader.BOARDS_ROOT_FILEPATH).toFile().list();
      boardsMap = new HashMap<>();
      for (String board : boards) {
        boardsMap.put(board, BoardReader.readBoard(BoardReader.BOARDS_ROOT_FILEPATH + board));
      }
      boardPreviewPanel = new BoardPreviewPanel();

      numPlayersSelector = new JComboBox<>(new Integer[] {2});
      fogOfWarSelector = new JComboBox<>(FogOfWar.values());
      boardSelector = new JComboBox<>(Paths.get(BoardReader.BOARDS_ROOT_FILEPATH).toFile().list());
      boardSelector.addActionListener(
          e -> {
            boardPreviewPanel.setBoard(getSelectedBoard());
            updateNumPlayersValues();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
              window.pack();
              repaint();
            }
          });

      add(boardSelector, BorderLayout.NORTH);

      boardPreviewPanel.setBoard(getSelectedBoard());
      add(boardPreviewPanel, BorderLayout.CENTER);

      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      bottomPanel.add(new JLabel("Players: "));
      bottomPanel.add(numPlayersSelector);
      bottomPanel.add(new JLabel("Fog of War: "));
      bottomPanel.add(fogOfWarSelector);
      add(bottomPanel, BorderLayout.SOUTH);

      repaint();
    }

    /** Returns the board object that corresponds to the currently selected board filename. */
    private Board getSelectedBoard() {
      return boardsMap.get((String) boardSelector.getSelectedItem());
    }

    /** Updates the valid number of players for the newly selected board. */
    private void updateNumPlayersValues() {
      int playersSelectedIndex = numPlayersSelector.getSelectedIndex();
      int newMaxPlayers = getSelectedBoard().getMaxPlayers();
      numPlayersSelector.removeAllItems();
      for (int i = 2; i <= newMaxPlayers; i++) {
        numPlayersSelector.addItem(i);
      }
      numPlayersSelector.setSelectedIndex(Math.min(newMaxPlayers - 2, playersSelectedIndex));
    }
  }

  /** A jpanel that pains a preview of a board. */
  private static final class BoardPreviewPanel extends JPanel {
    private static final int CELl_SIZE = 12;

    private Board board;

    private void setBoard(Board b) {
      board = b;
      setPreferredSize(new Dimension(board.getWidth() * CELl_SIZE, board.getHeight() * CELl_SIZE));
      setMaximumSize(new Dimension(board.getWidth() * CELl_SIZE, board.getHeight() * CELl_SIZE));
      setMinimumSize(new Dimension(board.getWidth() * CELl_SIZE, board.getHeight() * CELl_SIZE));
    }

    private Color getColor(boolean isCommanderStartSpot, Terrain t) {
      if (isCommanderStartSpot) {
        return Color.BLACK;
      }
      switch (t) {
        case GRASS:
          return new Color(142, 227, 58);
        case WOODS:
          return new Color(17, 134, 15);
        case MOUNTAIN:
          return new Color(188, 93, 30);
        case ANCIENT_GROUND:
          return new Color(237, 216, 35);
        default:
          return Color.WHITE;
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;

      for (int r = 0; r < board.getHeight(); r++) {
        for (int c = 0; c < board.getWidth(); c++) {
          g2d.setColor(
              getColor(board.isCommanderStartLocation(r, c), board.getTileAt(r, c).terrain));
          g2d.fillRect(c * CELl_SIZE, r * CELl_SIZE, CELl_SIZE, CELl_SIZE);
        }
      }
    }
  }

  /** Allows the user to create a new game - returns the options required to show a new game. */
  static NewGameOptions getNewGame(Frame frame) {
    NewGamePanel panel = new NewGamePanel();
    int returnOption =
        JOptionPane.showConfirmDialog(
            frame,
            panel,
            "New Game - Choose Board and Options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
    if (returnOption == JOptionPane.OK_OPTION) {
      return new NewGameOptions(
          BoardReader.BOARDS_ROOT_FILEPATH + panel.boardSelector.getSelectedItem(),
          (int) panel.numPlayersSelector.getSelectedItem(),
          (FogOfWar) panel.fogOfWarSelector.getSelectedItem());
    } else {
      return new NewGameOptions();
    }
  }
}
