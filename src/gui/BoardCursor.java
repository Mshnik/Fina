package gui;

import board.Tile;

/** An instance represents the cursor on the GUI */
public class BoardCursor extends Cursor<Tile, GamePanel>{
	
	/** Constructs a new BoardCursor
	 * @param b - the Board this cursor is used for
	 */
	public BoardCursor(GamePanel bp){
		super(bp, bp.game.board.getTileAt(0, 0));
	}
	
	/** Called internally whenever the cursor will be moved
	 * Validate if there is a path selector 
	 * @return true iff the move is ok.
	 */
	@Override
	protected boolean willMoveTo(Tile destination){
		if(getPanel().getPathSelector() == null) return true;
		PathSelector ps = getPanel().getPathSelector();
		return ps.getPath().contains(destination) || ps.getPossibleMovementsCloud().contains(destination);
	}
	
	/** Called internally whenever the cursor is moved
	 * works with pathSelector */
	@Override
	protected void moved(){
		super.moved();
		if(getPanel().getPathSelector() != null)
			getPanel().getPathSelector().addToPath(getElm());
	}
}
