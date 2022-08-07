package view.gui;

import ai.AIController;
import ai.delegating.DelegatingAIControllers;
import ai.dummy.FullRandomAIController;
import controller.game.BoardReader;
import model.board.Board;
import model.board.Terrain;
import model.game.Game.FogOfWar;
import model.game.HumanPlayer;
import model.game.Player;
import model.unit.commander.Commander;
import util.TextIO;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Blocking top-level selector that allows the user to create a new game. */
final class NewGameSelector {

  /** NewGameOptions returned by a NewGameSelector. */
  static final class NewGameOptions {
    final boolean cancelled;
    final String boardFilepath;
    final List<String> playerTypes;
    final List<LoadAIOptions> loadAIOptions;
    final FogOfWar fogOfWar;
    final int startingCommanderLevel;

    private NewGameOptions() {
      cancelled = true;
      playerTypes = null;
      loadAIOptions = null;
      boardFilepath = null;
      fogOfWar = FogOfWar.NONE;
      startingCommanderLevel = -1;
    }

    private NewGameOptions(
        String boardFilepath,
        List<String> playerTypes,
        List<LoadAIOptions> loadAIOptions,
        FogOfWar fogOfWar,
        int startingCommanderLevel) {
      cancelled = false;
      this.playerTypes = playerTypes;
      this.loadAIOptions = loadAIOptions;
      this.boardFilepath = boardFilepath;
      this.fogOfWar = fogOfWar;
      this.startingCommanderLevel = startingCommanderLevel;
    }
  }

  /** Options for loading an AI with predefined weights. */
  static final class LoadAIOptions {
    final String filename;
    final int row;

    private LoadAIOptions(String filename, int row) {
      this.filename = filename;
      this.row = row;
    }

    private static LoadAIOptions none() {
      return new LoadAIOptions(null, -1);
    }
  }

  /** Visual panel that displays the options for creating a new game. */
  private static final class NewGamePanel extends JPanel {
    private final JButton okButton;
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
      FullRandomAIController.FULL_RANDOM_AI_TYPE,
      // DelegatingAIControllers.DELEGATING_DEFAULT_AI_TYPE,
      DelegatingAIControllers.DELEGATING_RANDOM_AI_TYPE,
      AIController.PROVIDED_AI_TYPE
    };
    private final List<JComboBox<String>> playerTypeSelectorsList;
    private final Map<Integer, JTextField> loadAiTextFields;
    private final Map<Integer, JComboBox<Integer>> loadAiRowSelectors;

    private NewGamePanel(JButton okButton) {
      setLayout(new BorderLayout());
      this.okButton = okButton;

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
              pack();
            }
          });

      fogOfWarSelector = new JComboBox<>(FogOfWar.values());

      boardSelector = new JComboBox<>(Paths.get(BoardReader.BOARDS_ROOT_FILEPATH).toFile().list());
      boardSelector.addActionListener(
          e -> {
            boardPreviewPanel.setBoard(getSelectedBoard());
            updateNumPlayersValues();
            pack();
          });

      startingCommanderLevelSelector =
          new JComboBox<>(
              Stream.iterate(1, i -> i + 1)
                  .limit(Commander.MAX_LEVEL)
                  .collect(Collectors.toList())
                  .toArray(new Integer[Commander.MAX_LEVEL]));

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
      loadAiTextFields = new HashMap<>();
      loadAiRowSelectors = new HashMap<>();

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

    /** Forces the window containing this to pack, for resizing. */
    private void pack() {
      Window window = SwingUtilities.getWindowAncestor(this);
      if (window != null) {
        window.pack();
        repaint();
      }
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
      okButton.setEnabled(true);
      int numPlayers = (Integer) numPlayersSelector.getSelectedItem();
      while (playerPanel.getComponentCount() > numPlayers) {
        JPanel panelToRemove =
            (JPanel) playerPanel.getComponent(playerPanel.getComponents().length - 1);
        playerTypeSelectorsList.remove(
            (JComboBox) ((Container) panelToRemove.getComponent(0)).getComponent(1));
        playerPanel.remove(panelToRemove);
      }
      for (int i = playerPanel.getComponentCount() + 1; i <= numPlayers; i++) {
        JPanel playerSelectorPanel = new JPanel();
        playerSelectorPanel.setOpaque(false);
        playerSelectorPanel.setLayout(new BoxLayout(playerSelectorPanel, BoxLayout.X_AXIS));
        playerSelectorPanel.add(new JLabel("   Player " + i));

        JComboBox<String> comboBox = new JComboBox<>(PLAYER_TYPE_OPTIONS);
        comboBox.addActionListener(e -> updatePlayerType(comboBox));
        playerSelectorPanel.add(comboBox);
        playerTypeSelectorsList.add(comboBox);

        PlayerConfigurationPanel wrapperPanel = new PlayerConfigurationPanel(i);
        wrapperPanel.add(playerSelectorPanel);
        playerPanel.add(wrapperPanel);
      }
    }

    /** Updates the player selected by the given type selector. */
    private void updatePlayerType(JComboBox<String> playerTypeSelector) {
      JPanel containingPanel = (JPanel) playerTypeSelector.getParent().getParent();
      String selectedType = (String) playerTypeSelector.getSelectedItem();
      boolean packNeeded = false;
      if (selectedType != null) {
        switch (selectedType) {
          case AIController.PROVIDED_AI_TYPE:
            if (containingPanel.getComponentCount() == 1) {
              createAndAddLoadAIConfigPanel(
                  containingPanel, playerTypeSelectorsList.indexOf(playerTypeSelector));
              packNeeded = true;
            }
            break;
          default:
            while (containingPanel.getComponentCount() > 1) {
              containingPanel.remove(1);
              packNeeded = true;
            }
            refreshOkButtonEnabledState();
            break;
        }
      }
      if (packNeeded) {
        pack();
      }
    }

    /** Creates and populates a panel for loading AI options. */
    private void createAndAddLoadAIConfigPanel(JPanel containingPanel, int index) {
      JPanel loadAiConfigPanel = new JPanel();
      loadAiConfigPanel.setOpaque(false);
      loadAiConfigPanel.setLayout(new BoxLayout(loadAiConfigPanel, BoxLayout.X_AXIS));
      loadAiConfigPanel.add(new JLabel("   Load"));

      JTextField loadedFileField = new JTextField("Click to Load", 20);
      loadedFileField.setEditable(false);
      JComboBox<Integer> rowSelector = new JComboBox<>();

      loadedFileField.addMouseListener(
          new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
              chooseAiFile(loadedFileField, rowSelector);
            }

            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}
          });
      loadAiConfigPanel.add(loadedFileField);
      loadAiConfigPanel.add(rowSelector);

      loadAiTextFields.put(index, loadedFileField);
      loadAiRowSelectors.put(index, rowSelector);
      okButton.setEnabled(false);

      containingPanel.add(loadAiConfigPanel);
    }

    /**
     * Pops up a file chooser to pick a file to load from. If a file is picked, populates the the
     * text field with the chosen file and the rowSelectorBox with the rows that can be picked. If
     * not, clears those two back to default.
     */
    private void chooseAiFile(JTextField source, JComboBox<Integer> rowSelectorBox) {
      JFileChooser chooser = new JFileChooser("data/aiLogs/evo");
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setFileFilter(
          new FileFilter() {
            @Override
            public boolean accept(File f) {
              return f.getName().endsWith(".csv");
            }

            @Override
            public String getDescription() {
              return "*.csv";
            }
          });
      int result = chooser.showDialog(this, "Open");
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        source.setText(file.getAbsolutePath());
        try {
          rowSelectorBox.removeAllItems();
          Pattern firstElmPattern = Pattern.compile("Round ([0-9]+),");
          TextIO.readToArray(file)
              .stream()
              .skip(1)
              .map(
                  s -> {
                    Matcher matcher = firstElmPattern.matcher(s);
                    matcher.find();
                    return matcher.group(1);
                  })
              .map(Integer::parseInt)
              .forEach(rowSelectorBox::addItem);
          refreshOkButtonEnabledState();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        source.setText("Click to Load");
        rowSelectorBox.removeAllItems();
        okButton.setEnabled(false);
      }
    }

    /** Refreshes the enabled state of the ok button. */
    private void refreshOkButtonEnabledState() {
      okButton.setEnabled(
          loadAiTextFields
              .entrySet()
              .stream()
              .filter(
                  e ->
                      e.getKey() <= playerTypeSelectorsList.size()
                          && AIController.PROVIDED_AI_TYPE.equals(
                              playerTypeSelectorsList.get(e.getKey()).getSelectedItem()))
              .map(e -> e.getValue().getText())
              .noneMatch(s -> s.equals("Click to Load")));
    }

    /** Returns a list of player types of the current selections of the player type selectors. */
    private List<String> getPlayerTypes() {
      return playerTypeSelectorsList
          .stream()
          .map(s -> (String) s.getSelectedItem())
          .collect(Collectors.toList());
    }

    /** Returns a list of load AI options. Will be none() for any non loaded AI player. */
    private List<LoadAIOptions> getLoadAIOptions() {
      return IntStream.range(0, playerTypeSelectorsList.size())
          .mapToObj(
              i ->
                  loadAiTextFields.containsKey(i)
                      ? new LoadAIOptions(
                          loadAiTextFields.get(i).getText(),
                          (int) loadAiRowSelectors.get(i).getSelectedItem())
                      : LoadAIOptions.none())
          .collect(Collectors.toList());
    }
  }

  /** A jpanel that holds options for configuring a player. */
  private static final class PlayerConfigurationPanel extends JPanel {
    private final int index;

    private static final Color BLUE = new Color(7, 118, 220);
    private static final Color RED = new Color(235, 68, 0);
    private static final Color YELLOW = new Color(246, 227, 28);
    private static final Color GREEN = new Color(16, 211, 63);
    private static final Color PURPLE = new Color(190, 12, 225);

    private PlayerConfigurationPanel(int index) {
      this.index = index;
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      Graphics2D g2d = (Graphics2D) graphics;
      Color color;
      switch (Player.PlayerColor.values()[index - 1]) {
        case BLUE:
          color = BLUE;
          break;
        case RED:
          color = RED;
          break;
        case YELLOW:
          color = YELLOW;
          break;
        case GREEN:
          color = GREEN;
          break;
        case PURPLE:
          color = PURPLE;
          break;
        default:
          color = Color.GRAY;
      }
      g2d.setColor(color);
      g2d.fillRect(0, 0, 5, getHeight());
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
        case SEA:
          return new Color(30, 81, 193);
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

  /** Finds the optionPane parent of the given arg by recursing upwards. */
  private static JOptionPane getOptionPane(JComponent parent) {
    JOptionPane pane = null;
    if (!(parent instanceof JOptionPane)) {
      pane = getOptionPane((JComponent) parent.getParent());
    } else {
      pane = (JOptionPane) parent;
    }
    return pane;
  }

  /** Closes the OptionPane with the given option. */
  private static void closeActionPaneWithResponse(ActionEvent e) {
    JOptionPane source = getOptionPane((JComponent) e.getSource());
    source.setValue(e.getSource());
  }

  /** Allows the user to create a new game - returns the options required to show a new game. */
  static NewGameOptions getNewGame(Frame frame) {
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(NewGameSelector::closeActionPaneWithResponse);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(NewGameSelector::closeActionPaneWithResponse);

    NewGamePanel panel = new NewGamePanel(okButton);
    panel.updateNumPlayersValues();
    panel.updateNumPlayers();
    int returnOption =
        JOptionPane.showOptionDialog(
            frame,
            panel,
            "New Game - Choose Board and Options",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            new Object[] {okButton, cancelButton},
            okButton);
    if (returnOption == JOptionPane.OK_OPTION) {
      return new NewGameOptions(
          BoardReader.BOARDS_ROOT_FILEPATH + panel.boardSelector.getSelectedItem(),
          panel.getPlayerTypes(),
          panel.getLoadAIOptions(),
          (FogOfWar) panel.fogOfWarSelector.getSelectedItem(),
          (int) panel.startingCommanderLevelSelector.getSelectedItem());
    } else {
      return new NewGameOptions();
    }
  }
}
