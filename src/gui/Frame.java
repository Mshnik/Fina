package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import board.*;


/** The visual frame that manages showing the game */
public class Frame extends JFrame {

	/***/
	private static final long serialVersionUID = 1L;

	/** The boardPanel this Frame is drawing, if any */
	private BoardPanel boardPanel;
	
	public Frame(){
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);
		
		setMinimumSize(new Dimension(750, 500));
		setMaximumSize(new Dimension(750, 500));
		pack();
		this.setLocation(100, 100);
		
		validate();
		setVisible(true);
	}
	
	/** Sets this Frame to show BoardPanel bp. Removes previous boardPanel if any.
	 * Also triggers a packing and repainting. */
	public void setBoard(BoardPanel bp){
		if(boardPanel != null){
			removeKeyListener(boardPanel.boardCursor);
			remove(boardPanel);
		}
		add(bp, BorderLayout.CENTER);
		boardPanel = bp;
		addKeyListener(boardPanel.boardCursor);

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
	    f.setBoard(new BoardPanel(new Board(t), 20, 20));
	}
}
