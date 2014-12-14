package gui;

import gui.decision.PathSelector;
import gui.panel.GamePanel;
import board.Direction;
import board.Tile;

/** An instance represents the cursor on the GUI */
public class BoardCursor extends Cursor<Tile, GamePanel>{

	/** Constructs a new BoardCursor
	 * @param b - the Board this cursor is used for
	 */
	public BoardCursor(GamePanel bp){
		super(bp, bp.game.board.getTileAt(0, 0));
	}

	/** Returns true - selection criteria depends on the application */
	public boolean canSelect(){
		return true;
	}

	/** Called internally whenever the cursor will be moved
	 * Validate if there is a path selector or other selector. 
	 * If in some cloud and the move is to wrap around, return false but do the move anyways.
	 * @return true iff the move is ok (allow move)
	 */
	@Override
	protected boolean willMoveTo(Direction d, Tile destination){
		if(getPanel().getLocationSelector() == null){
			return destination != null;
		}
		LocationSelector ls = getPanel().getLocationSelector();
		boolean cloudOK = destination != null && ls.getPossibleMovementsCloud().contains(destination);
		if(ls instanceof PathSelector){
			PathSelector ps = (PathSelector) ls;
			return cloudOK || ps.getPath().contains(destination);
		}
		else{
			if(cloudOK) return true;
			else{
				int i = ls.cloud.indexOf(getElm());
				if(d.equals(Direction.LEFT) && i > 0){
					setElm(ls.cloud.get(i - 1));
					moved();
				} else if(d.equals(Direction.RIGHT) && i < ls.cloud.size() - 1){
					setElm(ls.cloud.get(i+1));
					moved();
				} else if(d.equals(Direction.UP)){
					Tile t2 = null;
					for(Tile t : ls.cloud){
						if(t.col == getElm().col && t.row < getElm().row 
								&& (t2 == null || t2.row < t.row))
							t2 = t;
					}
					if(t2 != null){
						setElm(t2);
						moved();
					}
				} else if(d.equals(Direction.DOWN)){
					Tile t2 = null;
					for(Tile t : ls.cloud){
						if(t.col == getElm().col && t.row > getElm().row 
								&& (t2 == null || t2.row > t.row))
							t2 = t;
					}
					if(t2 != null){
						setElm(t2);
						moved();
					}
				}
				return false;
			}
		}
	}


	/** Called internally whenever the cursor is moved
	 * works with pathSelector */
	@Override
	protected void moved(){
		super.moved();
		if(getPanel().getLocationSelector() != null && getPanel().getLocationSelector() instanceof PathSelector){
			PathSelector ps = (PathSelector) getPanel().getLocationSelector();
			ps.addToPath(getElm());
		}
		if(getElm().isOccupied() && panel.game.getCurrentPlayer().canSee(getElm()))
			panel.getFrame().infoPanel.setUnit(getElm().getOccupyingUnit());
		else
			panel.getFrame().infoPanel.setUnit(null);
	}
}
