package gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import board.*;

/** Drawable wrapper for a board object */
public class BoardPanel extends JPanel{

	/***/
	private static final long serialVersionUID = 1L;
	
	/** Pixels (size) for each square tile. */
	private static final int CELL_SIZE = 30; 
	
	/** The BoardState this BoardPanel is responsible for drawing */
	public final Board boardstate;
	
	public BoardPanel(Board bs){
		boardstate = bs;
		setPreferredSize(new Dimension(bs.getWidth() * CELL_SIZE, bs.getHeight() * CELL_SIZE));
	}
	
	@Override
	/** Paints this boardpanel, for use in the frame it is in. */
	public void paintComponent(Graphics g){
		
		//Paint the board itself
		for(Tile t : boardstate){
			g.drawImage(ImageIndex.imageForTerrain(t.terrain), 
					    t.col * CELL_SIZE, t.row * CELL_SIZE,
					    CELL_SIZE, CELL_SIZE, 
					    null);
		}
		
	}
	
}
