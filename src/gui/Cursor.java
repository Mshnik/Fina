package gui;

import game.MatrixElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import board.Direction;

/** An abstract cursor implementation that is able to select things
 * in any matrix
 * @param <T> - the type of the elements in the matrix this is choosing from. */
public abstract class Cursor<T extends MatrixElement, M extends MatrixPanel<T>> implements Animatable{
	
	/** Color for Cursor Drawing */
	protected static final Color COLOR = Color.red;
	
	/** Thickness of lines in Cursor Drawing */
	protected static final int THICKNESS = 3;
	
	/** The current animation state this BoardCursor is on.
	 * An interger in the range [0 ... getStateCount() - 1] */
	private int animationState;
	
	/** The elm the cursor is currently on */
	private T elm;
	
	/** The Panel this cursor is drawn on. Reference kept to call updates to it */
	private M panel;
	
	public Cursor(M panel, T startingElm){
		this.panel = panel;
		elm = startingElm;
		animationState = 0;
	}
	
	/** Returns the element this cursor is currently on */
	public T getElm(){
		return elm;
	}
	
	/** Sets the tile this cursor is on */
	public void setElm(T t){
		elm = t;
		panel.repaint();
	}
	
	/** Returns the row in the matrix this Cursor is currently on */
	public int getRow(){
		return elm.getRow();
	}
	
	/** Returns the col in the matrix this Cursor is currently on */
	public int getCol(){
		return elm.getCol();
	}
	
	/** Returns the matrix panel this is drawing for */
	public M getPanel(){
		return panel;
	}
	
	/** Called internally whenever a move would occur.
	 * Do validation, return true if move is ok, false otherwise
	 */
	protected abstract boolean willMoveTo(T destination);
	
	/** Call to move in the given direction, if possible */
	public void move(Direction d){
		T dest = getPanel().getElmInDirection(getElm(), d);
		
		if(dest != null && willMoveTo(dest)){
			setElm(dest);
			if(getCol() < getPanel().scrollX)
				getPanel().scrollX = Math.max(getPanel().scrollX - 1, 0);
			if(getCol() > getPanel().scrollX + getPanel().maxX - 1)
				getPanel().scrollX = 
				Math.min(getPanel().scrollX + 1, 
						getPanel().getWidth() - getPanel().maxX);
			if(getRow() < getPanel().scrollY)
				getPanel().scrollY = Math.max(getPanel().scrollY - 1, 0);
			if(getRow() > getPanel().scrollY + getPanel().maxY - 1)
				getPanel().scrollY= Math.min(getPanel().scrollY + 1, 
						getPanel().getHeight() - getPanel().maxY );
			moved();
		}
	}
	
	/** Called after a move occurs to do painting and the like. Can
	 * be overriden, but this method should be called first before adding new behavior */
	protected void moved(){
		panel.repaint();
	}
	
	/** Draw this Cursor as a red set of 4 corner lines */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(BoardCursor.COLOR);
		g2d.setStroke(new BasicStroke(BoardCursor.THICKNESS));
		
		//x min, y min, side length
		int x = getPanel().getXPosition(getElm()) + BoardCursor.THICKNESS/2;
		int y = getPanel().getYPosition(getElm()) + BoardCursor.THICKNESS/2;
		int h = getPanel().getElementHeight();
		int w = getPanel().getElementWidth();
		
		//delta - depends on animation state. when non 0, makes cursor smaller.
		int d = animationState * 2;
		
		//(x,y) coordinates for polylines. Each is 3 points, clockwise.
		int[][][] coords = {
				{
					//Top left corner
					{x + d, x + d, x + d + w/4},
					{y + d + h/4, y + d, y + d}
				},
				{
					//Top Right corner
					{x + 3*w/4 - d, x + w - d, x + w - d},
					{y + d, y + d, y + d + h/4}
				},
				{
					//Bottom Right corner
					{x + w - d, x + w - d, x + 3*w/4 - d},
					{y + 3*h/4 - d, y + h - d, y + h - d}
				},
				{
					//Bottom left corner
					{x + w/4 + d, x + d, x + d},
					{y + h - d, y + h - d, y + 3*h/4 - d}
				}
		};
		
		for(int i = 0; i < coords.length; i++){
			g2d.drawPolyline(coords[i][0], coords[i][1], coords[i][0].length);
		}
	}
	
	/** Cursors have a cycle length of some fraction of a second. */
	@Override
	public int getStateLength() {
		return 350;
	}

	/** Cursors have 2 states */
	@Override
	public int getStateCount() {
		return 2;
	}

	/** Returns the animation state this cursor is on */
	@Override
	public int getState() {
		return animationState;
	}

	/** Increases the animation state by 1, and causes a repaint */
	@Override
	public void advanceState() {
		animationState = (animationState + 1) % getStateCount();
		getPanel().repaint();
	}
}