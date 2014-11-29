package gui;

import java.awt.Color;

/** A default cursor implementation for when no special cursor actions are necessary */
public class DecisionCursor extends Cursor<Decision, DecisionPanel> {

	/** DecisionCursor Constructor. Starts at index (0,0).
	 * @param panel			- the panel this is on
	 */
	public DecisionCursor(DecisionPanel panel) {
		super(panel, panel.getElmAt(0, 0));
	}

	/** DecisionCursors have custom yellow color */
	@Override
	public Color getColor(){
		return new Color(255, 240, 28);
	}
	
	/** Moves are always oked. Returns true */
	@Override
	protected boolean willMoveTo(Decision destination) {
		return true;
	}
	
	/** Needs to have the gamePanel repaint */
	@Override
	protected void moved(){
		super.moved();
		panel.getFrame().repaint();
	}

}
