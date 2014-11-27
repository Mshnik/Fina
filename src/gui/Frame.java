package gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;

import unit.dummy.DummyCommander;
import unit.dummy.DummyPawn;
import game.*;
import board.*;


/** The visual frame that manages showing the game */
public class Frame extends JFrame {

	/***/
	private static final long serialVersionUID = 1L;

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
		
		pack();
		this.setLocation(100, 100);
		
		animator = new Animator();
		
		boardCursorActive = true;
		canTogglePathSelection = true;
		
		KeyboardListener.setFrame(this);
		
		validate();
		setVisible(true);
	}
	
	/** Sets this Frame to show GamePanel bp. Removes previous gamePanel if any.
	 * Also triggers a packing and repainting. */
	public void setBoard(GamePanel bp){
		if(gamePanel != null){
			remove(gamePanel);
			animator.removeAnimatable(gamePanel.boardCursor);
		}
		add(bp, BorderLayout.CENTER);
		gamePanel = bp;
		animator.addAnimatable(gamePanel.boardCursor);

		pack();
		repaint();
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
	    
	    GamePanel gp = new GamePanel(new Game(new Board(t)), 10, 20);
	    Player p1 = new HumanPlayer(gp.game);
	    new DummyCommander(p1, gp.game.board.getTileAt(0, 0));
	    new DummyPawn(p1, gp.game.board.getTileAt(0, 1));

	    f.setBoard(gp);
	    
	    Thread th = new Thread(gp.game);
	    th.start();
	}
}
