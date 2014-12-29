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
	    Frame f = new Frame(8, 15);
	    Terrain[][] t = {
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 	
	    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
	    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
	    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS
	    		},
	    		{ Terrain.GRASS, Terrain.MOUNTAIN, Terrain.GRASS, Terrain.GRASS, 	
		    		  Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
		    		  Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
		    		  Terrain.GRASS, Terrain.MOUNTAIN, Terrain.GRASS
		    	},
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.WOODS, Terrain.WOODS, 	
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.WOODS, 
    				Terrain.WOODS, Terrain.GRASS, Terrain.GRASS
	    		},
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.ANCIENT_GROUND, 	
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.ANCIENT_GROUND, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS
	    		},
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.ANCIENT_GROUND, 	
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.ANCIENT_GROUND, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS
	    		},
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.WOODS, Terrain.WOODS, 	
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.WOODS, 
    				Terrain.WOODS, Terrain.GRASS, Terrain.GRASS
	    		},
	    		{ Terrain.GRASS, Terrain.MOUNTAIN, Terrain.GRASS, Terrain.GRASS, 	
		    		  Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
		    		  Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
		    		  Terrain.GRASS, Terrain.MOUNTAIN, Terrain.GRASS
		    	},
	    		{ Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 	
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, 
    				Terrain.GRASS, Terrain.GRASS, Terrain.GRASS
	    		}
	    };
	    
	    
	    Game g = new Game(new Board(t), false);
	    GameController gc = new GameController(g, f);

	    Player p1 = new HumanPlayer(g, Color.green);
	    new Bhen(p1, g.board.getTileAt(0, 0));
	    Player p2 = new HumanPlayer(g, Color.magenta);
	    new DummyCommander(p2, g.board.getTileAt(1, 2));

	    gc.start();
	}
}
