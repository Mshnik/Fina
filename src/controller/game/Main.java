package controller.game;

import java.awt.Color;

import model.board.Board;
import model.board.Terrain;
import model.game.Game;
import model.game.HumanPlayer;
import model.game.Player;
import model.unit.commander.Bhen;
import model.unit.dummy.DummyCommander;
import view.gui.Frame;

public class Main {
	/** Simple main method to test out Frame features */
	public static void main(String[] args){
			String boardFilename = args.length > 0 ? args[0] : "sample_board.csv";
			Board board = BoardReader.readBoard("game/boards/" + boardFilename);

	    Frame f = new Frame(board.getHeight(), board.getWidth());
	    
	    Game g = new Game(board, false);
	    GameController gc = new GameController(g, f);

	    Player p1 = new HumanPlayer(g, Color.green);
	    new Bhen(p1, g.board.getTileAt(0, 0));
	    Player p2 = new HumanPlayer(g, Color.magenta);
	    new DummyCommander(p2, g.board.getTileAt(1, 2));

	    gc.start();
	}
}
