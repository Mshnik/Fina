package view.gui;

import java.util.ArrayList;

import controller.selector.CastSelector;
import controller.selector.LocationSelector;
import controller.selector.PathSelector;
import view.gui.panel.GamePanel;
import model.board.Direction;
import model.board.Tile;

/** An instance represents the cursor on the GUI */
public final class BoardCursor extends Cursor<Tile, GamePanel>{

	/** Constructs a new BoardCursor
	 * @param bp - the Board this cursor is used for
	 */
	public BoardCursor(GamePanel bp){
		super(bp, bp.controller.game.board.getTileAt(0, 0));
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
		if(getPanel().controller.getLocationSelector() == null){
			return destination != null;
		}
		LocationSelector ls = getPanel().controller.getLocationSelector();
		boolean cloudOK = destination != null && ls.getPossibleMovementsCloud().contains(destination);
		if(ls instanceof PathSelector){
			PathSelector ps = (PathSelector) ls;
			return cloudOK || ps.getPath().contains(destination);
		}
		else{
			if(cloudOK) return true;
			else{
				ArrayList<Tile> cloud = ls.getPossibleMovementsCloud();
				int i = cloud.indexOf(getElm());
				if(d.equals(Direction.LEFT)){
					if(i > 0)
						setElm(cloud.get(i - 1));
					else
						setElm(cloud.get(cloud.size() - 1));
					moved();
				} else if(d.equals(Direction.RIGHT)){
					if(i < cloud.size() - 1)
						setElm(cloud.get(i+1));
					else
						setElm(cloud.get(0));
					moved();
				} else if(d.equals(Direction.UP)){
					Tile t2 = null;
					for(Tile t : cloud){
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
					for(Tile t : cloud){
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

	/** Called whenever the cursor is forcably moved */
	@Override
	public void setElm(Tile t){
		super.setElm(t);
		updateUnitHover();
	}

	/** Called internally whenever the cursor is moved
	 * works with pathSelector */
	@Override
	protected void moved(){
		super.moved();
		LocationSelector ls = getPanel().controller.getLocationSelector();
		if(ls != null && ls instanceof PathSelector){
			PathSelector ps = (PathSelector) ls;
			ps.addToPath(getElm());
		} else if(ls != null && ls instanceof CastSelector){
			((CastSelector) ls).refreshEffectCloud();
		}
		updateUnitHover();
	}
	
	/**
	 * Updates info of the model.unit this is on. If no unit, sets terrain instead.
	 * Call whenever moved or elm set.
	 */
	private void updateUnitHover(){
		if(getElm().isOccupied() && panel.controller.game.getCurrentPlayer().canSee(getElm())){
			panel.getFrame().getInfoPanel().setUnit(getElm().getOccupyingUnit(), false);
		}
		else {
			panel.getFrame().getInfoPanel().setTerrain(getElm().terrain);
		}
	}
}
