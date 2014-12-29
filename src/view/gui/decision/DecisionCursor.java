package view.gui.decision;


import java.awt.Color;

import controller.game.Decision;

import view.gui.Cursor;

import controller.game.*;

import model.board.Direction;
import model.unit.Commander;
import model.unit.Unit;
import model.unit.ability.Ability;



/** A default cursor implementation for when no special cursor actions are necessary */
public class DecisionCursor extends Cursor<Decision, DecisionPanel> {

	/** DecisionCursor Constructor. Starts at index (0,0).
	 * @param panel			- the panel this is on
	 */
	public DecisionCursor(DecisionPanel panel) {
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
	protected boolean willMoveTo(Direction d, Decision destination) {
		return destination != null;
	}
	
	/** Needs to have the gamePanel repaint */
	@Override
	protected void moved(){
		super.moved();
		
		//Check to be in summon decision - if so, update info on model.unit examining
		if(panel.controller.getDecisionType() == GameController.DecisionType.SUMMON_DECISION){
			String unitName = getElm().getMessage();
			unitName = unitName.substring(0, unitName.indexOf(Decision.SEPERATOR));
			Commander c = panel.player.getCommander();
			Unit u = c.getUnitByName(unitName);
			panel.getFrame().showUnitStats(u);
		} else if(panel.controller.getDecisionType() == GameController.DecisionType.NEW_ABILITY_DECISION){
			String abilityName = getElm().getMessage();
			Commander c = panel.player.getCommander();
			Ability a = c.getAbilityByName(abilityName);
			panel.getFrame().showAbilityStats(a);
		}
		
		panel.getFrame().repaint();
	}

}
