package controller.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import controller.decision.Decision;
import controller.decision.Choice;

import view.gui.BoardCursor;
import view.gui.Cursor;
import view.gui.Frame;
import view.gui.decision.DecisionCursor;

import model.board.Direction;
import model.board.Tile;


/** Manager for all keyboard input. Only singleton instance is allowed. */
public class KeyboardListener implements KeyListener{

	/** The up key code */
	public static final int UP = KeyEvent.VK_UP;
	/** The left key code */
	public static final int LEFT = KeyEvent.VK_LEFT;
	/** The down key code */
	public static final int DOWN = KeyEvent.VK_DOWN;
	/** The right key code */
	public static final int RIGHT = KeyEvent.VK_RIGHT;
	/** The "a" (main button / weak confirm button) key code */
	public static final int A = KeyEvent.VK_Z;
	/** The "b" (secondary button / weak decline button) key code */
	public static final int B = KeyEvent.VK_X;
	/** The "start" (tertiary / strong confirm button) key code */
	public static final int START = KeyEvent.VK_ENTER;
	/** The "esc" (tertiary / strong decline button) key code */
	public static final int ESC = KeyEvent.VK_ESCAPE;

	/** The singleton instance of keyboardListener to be used by all frames */
	protected static final KeyboardListener instance = new KeyboardListener();

	/** Constructor for KeyboardListener */
	private KeyboardListener(){}
	
	/** The frame this KeyboardListener is listening to */
	private Frame frame;

	/** Set the Frame the KeyboardListener is listening to */
	public static void setFrame(Frame f){
		instance.frame = f;
		instance.frame.addKeyListener(instance);
	}

	/** Unused */
	@Override
	public void keyTyped(KeyEvent e) {}

	/** Handle key presses */
	@Override
	public void keyPressed(KeyEvent e) {
		GameController gc = frame.getController();
		int keyCode = e.getKeyCode();

		//Cursors - respond to arrow keys
		Direction d = Direction.fromKeyCode(keyCode);
		if(frame.getActiveCursor() != null && d != null){
			@SuppressWarnings("rawtypes")
			Cursor c = frame.getActiveCursor();
			c.move(d);
		}

		//Check different toggles - if non-none, handle
		if((keyCode == A || keyCode == B)){
			switch(gc.getToggle()){
				//No toggle currently open. Maybe open one.
			case NONE:
				if(keyCode == A){
					gc.startActionDecision();
				} else{
					gc.startEndTurnDecision();
				}
				break;
				// Process the decision.
			case DECISION:
				if(keyCode == A){
					if(! gc.frame.getActiveCursor().canSelect()) return;
					Choice decision = ((DecisionCursor) gc.frame.getActiveCursor()).getElm();
					switch(gc.getDecisionType()){
					case ACTION_DECISION:
						gc.processActionDecision(decision);
						break;
					case END_OF_TURN_DECISION:
						gc.processEndTurnDecision(decision);
						break;
					case SUMMON_DECISION:
						gc.startSummonSelection(decision);
						break;
					case CAST_DECISION:
						gc.startCastSelection(decision);
						break;
					case NEW_ABILITY_DECISION:
						gc.processNewAbilityDecision(decision);
						break;
					default:
						break;
					}
				} else{
					if(! gc.isDecisionManditory()){
						boolean resetDecision = 
								gc.getDecisionType() == Decision.DecisionType.SUMMON_DECISION
								|| gc.getDecisionType() == Decision.DecisionType.CAST_DECISION;
						gc.cancelDecision();
						if(resetDecision) gc.startActionDecision();
					} else{
						//TODO - tried to cancel manditory decision.
					}
				}
				break;
			case SUMMON_SELECTION:
				if(keyCode == A) {
					if(! gc.frame.getActiveCursor().canSelect()) return;
					Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
					gc.processSummonSelection(loc);
				}
				else{
					boolean summoningUnit = gc.getSummonType().equals(GameController.SummonType.UNIT);
					gc.cancelSummonSelection();
					if(summoningUnit) gc.startSummonCombatantDecision();
					else gc.startSummonBuildingDecision();
				}
				break;
			case CAST_SELECTION:
				if(keyCode == A) {
					if(! gc.frame.getActiveCursor().canSelect()) return;
					Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
					gc.processCastSelection(loc);
				}
				else{
					gc.cancelCastSelection();
					gc.startCastDecision();
				}
				break;
			case ATTACK_SELECTION:
				if(keyCode == A) {
					if(! gc.frame.getActiveCursor().canSelect()) return;
					Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
					gc.processAttackSelection(loc);
				}
				else{
					gc.cancelAttackSelection();
					gc.startActionDecision();
				}
				break;
				//Path selection - should only be the case after pathSelection already started
			case PATH_SELECTION:
				if(keyCode == A) {
					if(! gc.frame.getActiveCursor().canSelect()) return;
					Tile loc = ((BoardCursor) gc.frame.getActiveCursor()).getElm();
					gc.processPathSelection(loc);
				}
				else{
					gc.cancelPathSelection();
					gc.startActionDecision();
				}
				break;
			default:
				break;

			}




		}
	}

	/** Unused */
	@Override
	public void keyReleased(KeyEvent e) {};

}
