package gui;

import game.MatrixElement;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/** Panel on the bottom of the frame that shows text as necessary for
 * decisions
 * @author MPatashnik
 *
 */
public class TextPanel extends MatrixPanel {

	public TextPanel(int maxX, int maxY, int scrollX, int scrollY) {
		super(maxX, maxY, scrollX, scrollY);
	}


	/** True if this TextPanel currently is showing a decision for the user,
	 * false if it's just showing information
	 */
	private boolean active;
	
	/***/
	private static final long serialVersionUID = 1L;

	
	/** Draws this TextPanel */
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		if(getHeight() == 0) return;
		for(int i = 0; i <= getWidth(); i+= getHeight()){
			g2d.drawImage(ImageIndex.SANDSTONE, i, 0, getHeight(), getHeight(), null);
		}
	}


	@Override
	public int getMatrixWidth() {
		return 0;
	}


	@Override
	public int getMatrixHeight() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public MatrixElement getElmAt(int row, int col)
			throws IllegalArgumentException {
		return null;
	}


	@Override
	public int getElementHeight() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getElementWidth() {
		// TODO Auto-generated method stub
		return 0;
	}
}
