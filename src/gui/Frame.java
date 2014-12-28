package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

import unit.Unit;
import unit.ability.Ability;
import unit.commander.Bhen;
import unit.dummy.*;
import game.*;
import gui.panel.GamePanel;
import gui.panel.GamePanel.Toggle;
import gui.panel.HeaderPanel;
import gui.panel.InfoPanel;
import board.*;


/** The visual frame that manages showing the game */
public class Frame extends JFrame {

	/***/
	private static final long serialVersionUID = 1L;

	//Some nice fonts to consider:
	//Apple Chancery
	//Ayuthaya
	//Damascus
	//Herculanum
	//Kokonar
	//Libian Sc
	//Monotype corsiva
	//Papyrus
	/** The font to use for all text */
	public static final String FONTNAME = "Damascus";
	
	/** The headerPanel this Frame is drawing, if any */
	protected HeaderPanel headerPanel;
	
	/** The gamePanel this Frame is drawing, if any */
	protected GamePanel gamePanel;
	
	/** The infoPanel this Frame is drawing, if any */
	protected InfoPanel infoPanel;
	
	/** The animator for this Frame */
	protected Animator animator;
	
	/** The current active cursor */
	@SuppressWarnings("rawtypes")
	private Cursor activeCursor;
	
	public Frame(){
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);
		setLocation(100, 100);
		animator = new Animator();
		KeyboardListener.setFrame(this);
	}
	
	/** Sets this Frame to show GamePanel bp. Removes previous gamePanel if any.
	 * Also triggers a packing and repainting.
	 * @param g - the game to show.
	 * @param rows - the number of rows to draw of g (height of window).
	 * @param cols - the number of cols to draw of g (width of window).
	 */
	public void setGame(Game g, int rows, int cols){
		
		//Removal
		if(gamePanel != null){
			remove(gamePanel);
			remove(headerPanel);
			animator.removeAnimatable(gamePanel.boardCursor);
		}
		
		//New Adding
		g.setFrame(this);
	    GamePanel gp = new GamePanel(g, rows, cols);
		add(gp, BorderLayout.CENTER);
		gamePanel = gp;
		activeCursor = gamePanel.boardCursor;
		animator.addAnimatable(gamePanel.boardCursor);
		HeaderPanel hp = new HeaderPanel(g, gp);
		add(hp, BorderLayout.NORTH);
		InfoPanel ip = new InfoPanel(g, gp);
		add(ip, BorderLayout.SOUTH);
		headerPanel = hp;
		infoPanel = ip;
		pack();
		repaint();
		setVisible(true);
	}
	
	/** Returns the currently active toggle */
	public Toggle getToggle(){
		return gamePanel.getToggle();
	}
	
	/** Starts the turn for player p, making graphic updates as necessary */
	public void startTurnFor(Player p){
		gamePanel.boardCursor.setElm(p.getCommander().getLocation());
	}
	
	/** Updates the info panel to show the given unit *
	 * 
	 */
	public void showUnitStats(Unit u){
		infoPanel.setUnit(u);
	}
	
	/** Updates the info panel to show the given ability *
	 * 
	 */
	public void showAbilityStats(Ability a){
		infoPanel.setAbility(a);
	}
	
	/** Tells the gamepanel to start a abilitydecision for the given player.
	 * This happens when the commander levelsup
	 */
	public void startAbilityDecision(Player p){
		gamePanel.startNewAbilityDecision(p.getCommander());
	}
	
	/** Returns the current active cursor that is moved by arrow keys.
	 * Because cursor has many different implementations, casting is needed */
	@SuppressWarnings("rawtypes")
	public Cursor getActiveCursor(){
		return activeCursor;
	}
	
	/** Returns the current active cursor that is moved by arrow keys.
	 * Because cursor has many different implementations, casting is needed */
	@SuppressWarnings("rawtypes")
	public void setActiveCursor(Cursor c){
		activeCursor = c;
		animator.addAnimatable(c);
	}
	
	/** Returns the object responsible for animating things for this frame */
	public Animator getAnimator(){
		return animator;
	}
	
	/** Simple main method to test out Frame features */
	public static void main(String[] args){
	    Frame f = new Frame();
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
	    Player p1 = new HumanPlayer(g, Color.green);
	    new Bhen(p1, g.board.getTileAt(0, 0));
	    Player p2 = new HumanPlayer(g, Color.magenta);
	    new DummyCommander(p2, g.board.getTileAt(1, 2));

	    f.setGame(g, 8, 15);
	    
	    Thread th = new Thread(g);
	    th.start();
	}
}
