package gui;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** An instance represents the cursor on the GUI */
public class BoardCursor implements KeyListener{
	
	/** Color for Cursor Drawing */
	protected static final Color COLOR = Color.red;
	
	/** Thickness of lines in Cursor Drawing */
	protected static final int THICKNESS = 3;
	
	/** The row in the Board the cursor is currently on */
	private int row;
	
	/** The column in the Board the cursor is currently on */
	private int col;
	
	/** The board this cursor is used for */
	public final BoardPanel boardPanel;
	
	/** Constructs a new BoardCursor
	 * @param b - the Board this cursor is used for
	 */
	public BoardCursor(BoardPanel bp){
		boardPanel = bp;
		row = 0;
		col = 0;
	}
	
	/** Returns the row in the board this BoardCursor is currently on */
	public int getRow(){
		return row;
	}
	
	/** Returns the col in the board this BoardCursor is currently on */
	public int getCol(){
		return col;
	}
	
	/** Called internally whenever the cursor is moved
	 * Cause the cursor to be updated graphically. */
	private void moved(){
		boardPanel.repaint();
	}
	
	/** Moves the cursor left one square, if possible */
	public void moveLeft(){
		if(col > 0){
			col--;
			if(col < boardPanel.scrollX)
				boardPanel.scrollX--;
			moved();
		}
	}
	
	/** Moves the cursor right one square, if possible */
	public void moveRight(){
		if(col < boardPanel.boardState.getWidth() - 1){
			col++;
			if(col > boardPanel.scrollX + boardPanel.maxX - 1)
				boardPanel.scrollX++;
			moved();
		}
	}
	
	/** Moves the cursor up one square, if possible */
	public void moveUp(){
		if(row > 0){
			row--;
			if(row < boardPanel.scrollY)
				boardPanel.scrollY--;
			moved();
		}
	}
	
	/** Moves the cursor down one square, if possible */
	public void moveDown(){
		if(row < boardPanel.boardState.getHeight() - 1){
			row++;
			if(row > boardPanel.scrollY + boardPanel.maxY - 1)
				boardPanel.scrollY++;
			moved();
		}
	}

	/** Required for KeyListener. Unused - see keyPressed */
	@Override
	public void keyTyped(KeyEvent e) {}

	/** Handle a keyPress. If one of the arrow keys, do the corresponding movement.
	 * Otherwise, do nothing.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
	    switch(keyCode) { 
	        case KeyEvent.VK_UP:
	        	moveUp();
	            break;
	        case KeyEvent.VK_DOWN:
	            moveDown();
	            break;
	        case KeyEvent.VK_LEFT:
	            moveLeft();
	            break;
	        case KeyEvent.VK_RIGHT :
	            moveRight();
	            break;
	     }
	}

	/** Required for KeyListener. Unused - see keyPressed */
	@Override
	public void keyReleased(KeyEvent e) {}
	
}
