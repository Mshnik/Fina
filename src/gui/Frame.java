package gui;
import java.awt.BorderLayout;

import javax.swing.JFrame;

import unit.DummyUnit;
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
	    Terrain[][] t = new Terrain[100][150];
	    for(int i = 0; i < t.length; i++){
	    	for(int j = 0; j < t[i].length; j++){
	    		double d = Math.random();
	    		if(d <= 0.15) t[i][j] = Terrain.MOUNTAIN;
	    		else if (d <= 0.55) t[i][j] = Terrain.WOODS;
	    		else t[i][j] = Terrain.GRASS;
	    	}
	    }
	    
	    GamePanel g = new GamePanel(new Board(t), 10, 20);
	    new DummyUnit(null, g.boardState, g.boardState.getTileAt(0, 0));
	    f.setBoard(g);
	}
}
