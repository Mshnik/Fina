package gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;

import unit.dummy.*;
import game.*;
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
	protected static final String FONTNAME = "Damascus";
	
	/** The headerPanel this Frame is drawing, if any */
	protected HeaderPanel headerPanel;
	
	/** The gamePanel this Frame is drawing, if any */
	protected GamePanel gamePanel;
	
	/** The animator for this Frame */
	protected Animator animator;
	
	/** True iff the board cursor is active and should respond to keyboard input */
	protected boolean boardCursorActive;
	
	/** True iff pressing the A or B buttons can change path selection */
	protected boolean canTogglePathSelection;
	
	public Frame(){
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);
		setLocation(100, 100);
		animator = new Animator();
		boardCursorActive = true;
		canTogglePathSelection = true;
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
		animator.addAnimatable(gamePanel.boardCursor);
		HeaderPanel hp = new HeaderPanel(g, gp);
		add(hp, BorderLayout.NORTH);
		headerPanel = hp;
		pack();
		repaint();
		setVisible(true);
	}
	
	/** Simple main method to test out Frame features */
	public static void main(String[] args){
	    Frame f = new Frame();
	    Terrain[][] t = new Terrain[20][20];
	    for(int i = 0; i < t.length; i++){
	    	for(int j = 0; j < t[i].length; j++){
	    		double d = Math.random();
	    		if(d <= 0.15) t[i][j] = Terrain.MOUNTAIN;
	    		else if (d <= 0.55) t[i][j] = Terrain.WOODS;
	    		else t[i][j] = Terrain.GRASS;
	    	}
	    }
	    Game g = new Game(new Board(t), true);
	    Player p1 = new HumanPlayer(g);
	    new DummyCommander(p1, g.board.getTileAt(0, 0));

	    f.setGame(g, 8, 15);
	    
	    Thread th = new Thread(g);
	    th.start();
	}
}
