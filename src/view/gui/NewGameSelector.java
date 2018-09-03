package view.gui;

import ai.dummy.FullRandomAIController;
import controller.game.BoardReader;
import model.board.Board;
import model.board.Terrain;
import model.game.Game.FogOfWar;
import model.game.HumanPlayer;
import model.unit.commander.Commander;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Blocking top-level selector that allows the user to create a new game. */
final class NewGameSelector {

  /** NewGameOptions returned by a NewGameSelector. */
  static final class NewGameOptions {
    final boolean cancelled;
    final String boardFilepath;
    final List<String> playerTypes;
    final FogOfWar fogOfWar;
    final int startingCommanderLevel;

    private NewGameOptions() {
      cancelled = true;
      playerTypes = null;
      boardFilepath = null;
      fogOfWar = FogOfWar.NONE;
      startingCommanderLevel = -1;
    }

    private NewGameOptions(
        String boardFilepath,
        List<String> playerTypes,
        FogOfWar fogOfWar,
        int startingCommanderLevel) {
      cancelled = false;
      this.playerTypes = playerTypes;
      this.boardFilepath = boardFilepath;
      this.fogOfWar = fogOfWar;
      this.startingCommanderLevel = startingCommanderLevel;
    }
  }

  /** Visual panel that displays the options for creating a new game. */
  private static final class NewGamePanel extends JPanel {
    private final JComboBox<String> boardSelector;
    private final JComboBox<Integer> numPlayersSelector;
    private final JComboBox<Integer> startingCommanderLevelSelector;
    private final JComboBox<FogOfWar> fogOfWarSelector;
    private final Map<String, Board> boardsMap;

    private final BoardPreviewPanel boardPreviewPanel;
    private final JPanel playerPanel;

    private static final String[] PLAYER_TYPE_OPTIONS = {
      HumanPlayer.HUMAN_PLAYER_TYPE,
      // Uncomment if these are needed for testing.
      // DoNothingAIController.DO_NOTHING_AI_TYPE,
      // MoveCommanderRandomlyAIController.MOVE_COMMANDER_RANDOMLY_AI_TYPE,
      FullRandomAIController.FULL_RANDOM_AI_TYPE
    };
    private final List<JComboBox<String>> playerTypeSelectorsList;

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
      numPlayersSelector.addActionListener(
          e -> {
            if (numPlayersSelector.getSelectedItem() != null) {
              updateNumPlayers();
              Window window = SwingUtilities.getWindowAncestor(this);
              if (window != null) {
                window.pack();
                repaint();
              }
            }
          });

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

      startingCommanderLevelSelector =
          new JComboBox<>(
              Stream.iterate(1, i -> i + 1)
                  .limit(Commander.MAX_LEVEL - 1)
                  .collect(Collectors.toList())
                  .toArray(new Integer[Commander.MAX_LEVEL - 1]));

      add(boardSelector, BorderLayout.NORTH);

      boardPreviewPanel.setBoard(getSelectedBoard());
      JPanel centeringPanel = new JPanel();
      centeringPanel.setLayout(new GridBagLayout());
      centeringPanel.add(boardPreviewPanel);
      add(centeringPanel, BorderLayout.CENTER);

      JPanel playerHeaderPanel = new JPanel();
      playerHeaderPanel.setLayout(new BoxLayout(playerHeaderPanel, BoxLayout.X_AXIS));
      playerHeaderPanel.add(new JLabel("Players: "), BorderLayout.WEST);
      playerHeaderPanel.add(numPlayersSelector, BorderLayout.CENTER);

      playerPanel = new JPanel();
      playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
      playerTypeSelectorsList = new ArrayList<>();

      JPanel commanderStartLevelPanel = new JPanel();
      commanderStartLevelPanel.setLayout(new BoxLayout(commanderStartLevelPanel, BoxLayout.X_AXIS));
      commanderStartLevelPanel.add(new JLabel("Starting Commander Level: "));
      commanderStartLevelPanel.add(startingCommanderLevelSelector);

      JPanel fogOfWarPanel = new JPanel();
      fogOfWarPanel.setLayout(new BoxLayout(fogOfWarPanel, BoxLayout.X_AXIS));
      fogOfWarPanel.add(new JLabel("Fog of War: "));
      fogOfWarPanel.add(fogOfWarSelector);

      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
      bottomPanel.add(new JSeparator());
      bottomPanel.add(playerHeaderPanel);
      bottomPanel.add(playerPanel);
      bottomPanel.add(commanderStartLevelPanel);
      bottomPanel.add(new JSeparator());
      bottomPanel.add(fogOfWarPanel);
      add(bottomPanel, BorderLayout.SOUTH);

      repaint();
    }

    /** Returns the board object that corresponds to the currently selected board filename. */
    private Board getSelectedBoard() {
      return boardsMap.get((String) boardSelector.getSelectedItem());
    }

    /** Updates the valid number of players for the newly selected board. */
    private void updateNumPlayersValues() {
      // Update selector.
      int playersSelectedIndex = numPlayersSelector.getSelectedIndex();
      int newMaxPlayers = getSelectedBoard().getMaxPlayers();
      numPlayersSelector.removeAllItems();
      for (int i = 2; i <= newMaxPlayers; i++) {
        numPlayersSelector.addItem(i);
      }
      numPlayersSelector.setSelectedIndex(Math.min(newMaxPlayers - 2, playersSelectedIndex));

      if (playersSelectedIndex != numPlayersSelector.getSelectedIndex()) {
        updateNumPlayers();
      }
    }

    /**
     * Updates the player type (human / AI) for the newly selected number of players. Removes rows
     * from end if number of players went down, adds rows to end if number of players went up.
     */
    private void updateNumPlayers() {
      int numPlayers = (Integer) numPlayersSelector.getSelectedItem();
      while (playerPanel.getComponentCount() > numPlayers) {
        JPanel panelToRemove =
            (JPanel) playerPanel.getComponent(playerPanel.getComponents().length - 1);
        playerTypeSelectorsList.remove((JComboBox) panelToRemove.getComponent(1));
        playerPanel.remove(panelToRemove);
      }
      for (int i = playerPanel.getComponentCount() + 1; i <= numPlayers; i++) {
        JPanel playerSelectorPanel = new JPanel();
        playerSelectorPanel.setLayout(new BoxLayout(playerSelectorPanel, BoxLayout.X_AXIS));
        playerSelectorPanel.add(new JLabel(" > Player " + i));

        JComboBox<String> comboBox = new JComboBox<>(PLAYER_TYPE_OPTIONS);
        playerSelectorPanel.add(comboBox);
        playerTypeSelectorsList.add(comboBox);

        playerPanel.add(playerSelectorPanel);
      }
    }

    /** Returns a list of player types of the current selections of the player type selectors. */
    private List<String> getPlayerTypes() {
      return playerTypeSelectorsList
          .stream()
          .map(s -> (String) s.getSelectedItem())
          .collect(Collectors.toList());
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
        case ANCIENT_GROUND:
          return new Color(142, 227, 58);
        case WOODS:
          return new Color(17, 134, 15);
        case MOUNTAIN:
          return new Color(188, 93, 30);
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
    panel.updateNumPlayersValues();
    panel.updateNumPlayers();
    int returnOption =
        JOptionPane.showConfirmDialog(
            frame,
            panel,
            "New Game - Choose Board and Options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null);
    if (returnOption == JOptionPane.OK_OPTION) {
      return new NewGameOptions(
          BoardReader.BOARDS_ROOT_FILEPATH + panel.boardSelector.getSelectedItem(),
          panel.getPlayerTypes(),
          (FogOfWar) panel.fogOfWarSelector.getSelectedItem(),
          (int) panel.startingCommanderLevelSelector.getSelectedItem());
    } else {
      return new NewGameOptions();
    }
  }
}
