package view.gui.decision;


import java.awt.Color;

import controller.decision.Choice;

import view.gui.Cursor;

import model.board.Direction;
import model.unit.Unit;
import model.unit.ability.Ability;

/** A default cursor implementation for when no special cursor actions are necessary */
public final class DecisionCursor extends Cursor<Choice, DecisionPanel> {

	/** DecisionCursor Constructor. Starts at index (0,0).
	 * @param panel			- the panel this is on
	 */
	DecisionCursor(DecisionPanel panel) {
		super(panel, panel.getElmAt(0, 0));
	}

	/** Can only select if the decision it is on is selectable */
	public boolean canSelect(){
		return getElm().isSelectable();
	}
	
	/** DecisionCursors have custom yellow color */
	@Override
	public Color getColor(){
		return new Color(255, 240, 28);
	}
	
	/** Moves are always oked. Returns true, so long as destination isn't null */
	@Override
	protected boolean willMoveTo(Direction d, Choice destination) {
		return destination != null;
	}
	
	/** Needs to have the gamePanel repaint */
	@Override
	protected void moved(){
		super.moved();
		
		//Check for having linked object. If so, inspect it
		Object obj = getElm().getVal();
		if(obj != null){
			if(obj instanceof Unit) panel.getFrame().showUnitStats((Unit)obj);
			else if(obj instanceof Ability) panel.getFrame().showAbilityStats((Ability) obj);
		}
		
		panel.getFrame().repaint();
	}

}
