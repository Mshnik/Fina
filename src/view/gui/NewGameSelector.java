package view.gui;

import controller.game.BoardReader;
import java.awt.BorderLayout;
import java.nio.file.Paths;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
  static final class NewGamePanel extends JPanel {
    private final JComboBox<String> boardSelector;
    private final JComboBox<Integer> numPlayersSelector;
    private final JComboBox<FogOfWar> fogOfWarSelector;

    NewGamePanel() {
      setLayout(new BorderLayout());

      boardSelector = new JComboBox<>(Paths.get(BoardReader.BOARDS_ROOT_FILEPATH).toFile().list());
      numPlayersSelector = new JComboBox<>(new Integer[] {2});
      fogOfWarSelector = new JComboBox<>(FogOfWar.values());

      add(boardSelector, BorderLayout.NORTH);

      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      bottomPanel.add(new JLabel("Players: "));
      bottomPanel.add(numPlayersSelector);
      bottomPanel.add(new JLabel("Fog of War: "));
      bottomPanel.add(fogOfWarSelector);
      add(bottomPanel, BorderLayout.SOUTH);
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
