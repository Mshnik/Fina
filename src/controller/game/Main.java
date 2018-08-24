package controller.game;

import java.awt.Color;
import model.board.Board;
import model.game.Game;
import model.game.HumanPlayer;
import model.game.Player;
import model.unit.building.Buildings;
import model.unit.combatant.FileCombatant;
import model.unit.commander.DummyCommander;
import view.gui.Frame;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) {
    // Force unit and building loading.
    FileCombatant.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);

    // Load board file and make board.
    String boardFilename = args.length > 0 ? args[0] : "sample_board_small.csv";
    Board board = BoardReader.readBoard("game/boards/" + boardFilename);

    Frame f = new Frame(14, 28);

    Game g = new Game(board, Game.FogOfWar.HIDE_ANCIENT_GROUND);
    GameController gc = new GameController(g, f);

    Player p1 = new HumanPlayer(g, Color.green);
    new DummyCommander(p1, 3);
    Player p2 = new HumanPlayer(g, Color.magenta);
    new DummyCommander(p2, 3);

    gc.start();
  }
}
