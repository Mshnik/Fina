package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import board.Direction;
import board.Tile;

/** An instance represents the cursor on the GUI */
public class BoardCursor implements Animatable{
	
	/** Color for Cursor Drawing */
	protected static final Color COLOR = Color.red;
	
	/** Thickness of lines in Cursor Drawing */
	protected static final int THICKNESS = 3;
	
	/** The tile the boardCursor is currently on */
	private Tile tile;
	
	/** The board this cursor is used for */
	public final GamePanel gamePanel;
	
	/** The current animation state this BoardCursor is on.
	 * An interger in the range [0 ... getStateCount() - 1] */
	private int animationState;
	
	/** Constructs a new BoardCursor
	 * @param b - the Board this cursor is used for
	 */
	public BoardCursor(GamePanel bp){
		gamePanel = bp;
		tile = gamePanel.boardState.getTileAt(0, 0);
		animationState = 0;
	}
	
	/** Returns the row in the board this BoardCursor is currently on */
	public int getRow(){
		return tile.row;
	}
	
	/** Returns the col in the board this BoardCursor is currently on */
	public int getCol(){
		return tile.col;
	}
	
	/** Returns the tile this cursor is currently on */
	public Tile getTile(){
		return tile;
	}
	
	/** Sets the tile this cursor is on */
	public void setTile(Tile t){
		tile = t;
		gamePanel.repaint();
	}
	
	/** Called internally whenever the cursor will be moved
	 * Validate if there is a path selector 
	 * @return true iff the move is ok.
	 */
	private boolean willMoveTo(Tile destination){
		if(gamePanel.getPathSelector() == null) return true;
		PathSelector ps = gamePanel.getPathSelector();
		return ps.getPath().contains(destination) || ps.getPossibleMovementsCloud().contains(destination);
	}
	
	/** Call to move in the given direction, if possible */
	public void move(Direction d){
		Tile dest = gamePanel.boardState.getTileInDirection(tile, d);
		
		int extraRoom = 0;
		if(gamePanel.getPathSelector() != null) extraRoom = 1;
		
		if(dest != null && willMoveTo(dest)){
			tile  = dest;
			if(getCol() - extraRoom < gamePanel.scrollX)
				gamePanel.scrollX = Math.max(gamePanel.scrollX - 1, 0);
			if(getCol() + extraRoom > gamePanel.scrollX + gamePanel.maxX - 1)
				gamePanel.scrollX = 
				Math.min(gamePanel.scrollX + 1, 
						 gamePanel.boardState.getWidth() - gamePanel.maxX);
			if(getRow() - extraRoom < gamePanel.scrollY)
				gamePanel.scrollY = Math.max(gamePanel.scrollY - 1, 0);
			if(getRow() + extraRoom > gamePanel.scrollY + gamePanel.maxY - 1)
				gamePanel.scrollY= Math.min(gamePanel.scrollY + 1, 
						 gamePanel.boardState.getHeight() - gamePanel.maxY );
			moved();
		}
	}
	
	/** Called internally whenever the cursor is moved
	 * Cause the cursor to be updated graphically. */
	private void moved(){
		if(gamePanel.getPathSelector() != null)
			gamePanel.getPathSelector().addToPath(getTile());
		
		gamePanel.repaint();
	}

	/** Draw this Cursor as a red set of 4 corner lines */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(BoardCursor.COLOR);
		g2d.setStroke(new BasicStroke(BoardCursor.THICKNESS));
		
		//x min, y min, side length
		int x = gamePanel.getXPosition(getTile()) + BoardCursor.THICKNESS/2;
		int y = gamePanel.getYPosition(getTile()) + BoardCursor.THICKNESS/2;
		int s = GamePanel.CELL_SIZE - BoardCursor.THICKNESS;
		
		//delta - depends on animation state. when non 0, makes cursor smaller.
		int d = animationState * 2;
		
		//(x,y) coordinates for polylines. Each is 3 points, clockwise.
		int[][][] coords = {
				{
					//Top left corner
					{x + d, x + d, x + d + s/4},
					{y + d + s/4, y + d, y + d}
				},
				{
					//Top Right corner
					{x + 3*s/4 - d, x + s - d, x + s - d},
					{y + d, y + d, y + d + s/4}
				},
				{
					//Bottom Right corner
					{x + s - d, x + s - d, x + 3*s/4 - d},
					{y + 3*s/4 - d, y + s - d, y + s - d}
				},
				{
					//Bottom left corner
					{x + s/4 + d, x + d, x + d},
					{y + s - d, y + s - d, y + 3*s/4 - d}
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
		gamePanel.repaint();
	}
	
}
