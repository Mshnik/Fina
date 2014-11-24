package gui;
import java.awt.BorderLayout;

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
		pack();
		validate();
		setVisible(true);
	}
	
	/** Sets this Frame to show BoardPanel bp. Removes previous boardPanel if any.
	 * Also triggers a packing and repainting. */
	public void setBoard(BoardPanel bp){
		if(boardPanel != null) remove(boardPanel);
		add(bp, BorderLayout.CENTER);
		boardPanel = bp;
		pack();
		repaint();
	}
	
	/** Simple main method to test out Frame features */
	public static void main(String[] args){
	    Frame f = new Frame();
	    Terrain[][] t = 
	    	{
	    		{Terrain.GRASS, Terrain.GRASS, Terrain.GRASS, Terrain.GRASS},
	    		{Terrain.GRASS, Terrain.MOUNTAIN, Terrain.WOODS, Terrain.MOUNTAIN},
	    		{Terrain.GRASS, Terrain.MOUNTAIN, Terrain.MOUNTAIN, Terrain.MOUNTAIN}
	    	};
	    f.setBoard(new BoardPanel(new Board(t)));
	}
}
