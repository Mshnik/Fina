package controller.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import view.gui.Frame;
import view.gui.panel.GamePanel;

import model.board.Tile;
import model.game.Game;
import model.game.Player;
import model.unit.Combatant;
import model.unit.Commander;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;

/** Overall controlling class that unites all classes.
 * Should be run in its own thread, because some methods may cause arbitrary
 * waiting, hopefully not to clog the EVT
 * 
 * @author MPatashnik
 *	/** The game that this controller wraps
	public final Game game;
 */
public class GameController {
	/** Text for ending a turn */
	public static final String END_TURN = "Yes";
	/** Text for canceling a decision */
	public static final String CANCEL = "No";
	/** Text representing moving */
	public static final String MOVE = "Move";
	/** Text representing fighting */
	public static final String FIGHT = "Attack";
	/** Text representing summoning */
	public static final String SUMMON = "Summon";
	/** Text representing building (building summoning) */
	public static final String BUILD = "Build";
	/** Text representing casting (Using active abilities) */
	public static final String CAST = "Cast";
	
	/** Different possiblities for toggle options */
	public enum Toggle{
		NONE,
		DECISION,
		PATH_SELECTION,
		SUMMON_SELECTION,
		ATTACK_SELECTION
	}

	/** The layers of active toggles. Topmost is the current toggle */
	private Stack<Toggle> toggle;
	
	/** The types of decisions that can be made */
	public enum DecisionType{
		ACTION_DECISION,
		SUMMON_DECISION,
		CAST_DECISION,
		NEW_ABILITY_DECISION,
		END_OF_TURN_DECISION
	}
	
	/** The type of decision currently in progress - what kind of decision it is making. Null if none */
	private DecisionType decisionType;
	
	/** The choices for the current decision in progress. Null if none */
	private Decision[] decisionChoices;
	
	/** True iff the current decision is manditory. false if no decision is underway */
	private boolean decisionManditory;
	
	/** Different types of summoning */
	public enum SummonType{
		UNIT,
		BUILDING
	}
	
	/** The type of the most recent decision to summon. Null if none is currently in progress */
	private SummonType summonType;
	
	/** A hashmap from each player to the color to tint their units */
	private final HashMap<Player, Color> playerColors;
	
	/** The Frame that is drawing this Game */
	public final Frame frame;
	
	/** The game this is controlling */
	public final Game game;
	
	/** The active location selector, if any */
	private LocationSelector locationSelector;
	
	public GameController(Game g, Frame f){
		game = g;
		game.setGameController(this);
		frame = f;
		frame.setController(this);
		
		playerColors = new HashMap<Player, Color>();
		toggle = new Stack<Toggle>();
	}
	
	/** Returns the gamePanel located within frame */
	public GamePanel getGamePanel(){
		return frame.getGamePanel();
	}
	
	/** Starts this gameController running.
	 * Does nothing if currently running or game is already over */
	public synchronized void start(){
		if(isRunning() || game.isGameOver()) return;
		Thread th = new Thread(game);
	    th.start();
	}
	
	/** Adds this player to the model.game at the end of the player list.
	 * Throws a runtimeException if the model.game is already underway.
	 * Returns the number of players after adding p */
	public int addPlayer(Player p, Color c) throws RuntimeException{
		if(isRunning())
			throw new RuntimeException("Can't add " + p + " to " + this 
					+ " because game is already started");
		game.addPlayer(p);
		playerColors.put(p, c.darker());
		return game.getRemainingPlayers().size();
	}
	
	/** Starts a new ability decision for the given player - call during levelup
	 * 
	 */
	public void startNewAbilityDecision(Player p){
		startNewAbilityDecision(p.getCommander());
	}
	
	/** Returns iff the game is currently runnign */
	public boolean isRunning(){
		return game.isRunning();
	}
	
	/** Instructs the Frame to repaint */
	public void repaint(){
		frame.repaint();
	}
	
	/** Gets the color for the given player */
	public Color getColorFor(Player p){
		return playerColors.get(p);
	}
	
	/** Called when the model wants to begin the turn for player p */
	public void startTurnFor(Player p){
		frame.startTurnFor(p);
	}
	
	/** Returns the current Toggle setting.
	 * Returns Toggle.NONE if there are no current toggles open */
	public Toggle getToggle(){
		try{
			return toggle.peek();
		}catch(EmptyStackException e){
			return Toggle.NONE;
		}
	}

	/** Sets the current Toggle setting by adding it to the top of the stack */
	protected void addToggle(Toggle t){
		toggle.push(t);
	}

	/** Removes the top-most Toggle setting. 
	 * Returns the removed setting for checking purposes */
	protected Toggle removeTopToggle(){
		return toggle.pop();
	}
	
	/** Cancels the currently selected decision for any decision
	 * Throws a runtimeException if this was a bad time to cancel because decision wasn't happening.
	 * This should be called *after* all necessary information is stored from the decision variables */
	public void cancelDecision() throws RuntimeException{
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.DECISION))
			throw new RuntimeException("Can't cancel decision, currently toggling " + getToggle());
		frame.getAnimator().removeAnimatable(getGamePanel().getDecisionPanel().cursor);
		decisionType = null;
		decisionChoices = null;
		decisionManditory = false;
		frame.getGamePanel().setDecisionPanel(null);
		frame.setActiveCursor(getGamePanel().boardCursor);
		repaint();
	}
	
	/** Returns the currently active location selector, if any */
	public LocationSelector getLocationSelector(){
		return locationSelector;
	}
	
	/** Returns the type of the current decision, if any */
	public DecisionType getDecisionType(){
		return decisionType;
	}
	
	/** Returns iff the current decision is manditory */
	public boolean isDecisionManditory(){
		return decisionManditory;
	}
	
	/** Creates a new Decision Panel for doing things with a model.unit, 
	 * shown to the side of the current tile (side depending
	 * on location of current tile)
	 * If there is no model.unit on the given tile or the model.unit doesn't belong to the current player, does nothing.
	 * If the model.unit on the tile, populates getGamePanel().getDecisionPanel() with all the possible decisions
	 */
	public void startActionDecision(){
		Tile t = getGamePanel().boardCursor.getElm();
		if(! getGamePanel().boardCursor.canSelect()) return;
		if(t.getOccupyingUnit() == null || t.getOccupyingUnit().owner != game.getCurrentPlayer()){
			return;
		} 
		Unit u = t.getOccupyingUnit();

		//Add choices based on the model.unit on this tile
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		int i = 0;
		if(u instanceof MovingUnit){
			decisions.add(new Decision(i++, u.canMove(), MOVE));
		}
		if(u instanceof Combatant){
			decisions.add(new Decision(i++, u.canFight() && ((Combatant) u).hasFightableTarget(), FIGHT));
		}
		if(u instanceof Summoner){
			decisions.add(new Decision(i++, ((Summoner) u).hasSummonSpace(), SUMMON));
			decisions.add(new Decision(i++, ((Summoner) u).hasBuildSpace(), BUILD));
		}
		if(u instanceof Commander){
			decisions.add(new Decision(i++, ((Commander) u).canCast(), CAST));
		}

		//If there are no applicable choices, do nothing
		if(decisions.isEmpty()) return;

		//Otherwise, convert to array, create panel, set correct location on screen.
		decisionType = DecisionType.ACTION_DECISION;
		decisionManditory = false;
		decisionChoices = decisions.toArray(new Decision[decisions.size()]);
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("Action", game.getCurrentPlayer(), decisionChoices);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes the currently selected Actiondecision.
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processActionDecision(Decision d) throws RuntimeException{
		String choice = d.getMessage();
		cancelDecision();
		switch(choice){
		case MOVE:
			startPathSelection();
			break;
		case SUMMON:
			startSummonCombatantDecision();
			break;
		case BUILD:
			startSummonBuildingDecision();
			break;
		case FIGHT:
			startAttackSelection();
			break;
		case CAST:
			startCastDecision();
			break;
		default:
			break;
		}
	}

	/** Starts a getGamePanel().getDecisionPanel() for ending the current player's turn */
	public void startEndTurnDecision(){
		decisionChoices = new Decision[]{new Decision(0, GameController.CANCEL), new Decision(1, GameController.END_TURN)};
		decisionType = DecisionType.END_OF_TURN_DECISION;
		decisionManditory = false;
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("End Turn?", game.getCurrentPlayer(), decisionChoices);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes an endOfTurn Decision */
	public void processEndTurnDecision(Decision d){
		String m = d.getMessage();
		cancelDecision();
		switch(m){
		case GameController.END_TURN:
			game.getCurrentPlayer().turnEnd();
			break;
		}
	}


	/** Starts  a levelup ability selection decision.
	 * Assumes c has leveled up but hasn't chosen an ability yet */
	public void startNewAbilityDecision(Commander c) throws RuntimeException{
		if(c.getAbility(c.getLevel()) != null)
			throw new RuntimeException("Can't pick a new ability for " + c 
					+", already has " + c.getAbility(c.getLevel()));

		Ability[] a = c.getPossibleAbilities(c.getLevel());
		if(a != null){
			decisionChoices = new Decision[a.length];
			decisionType = DecisionType.NEW_ABILITY_DECISION;
			decisionManditory = true;
			for(int i = 0; i < a.length; i++){
				decisionChoices[i] = new Decision(i, a[i].name);
			}
			addToggle(Toggle.DECISION);
			getGamePanel().fixDecisionPanel("Choose a New Ability", c.owner, decisionChoices);
			getGamePanel().centerDecisionPanel();
		} 
		//else do nothing
	}

	/** Processes the levelup new ability decision */
	public void processNewAbilityDecision(Decision d){
		Commander c = getGamePanel().getDecisionPanel().player.getCommander();
		int index = getGamePanel().getDecisionPanel().getElm().getIndex();
		cancelDecision();
		c.chooseAbility(index);
	}

	/** Returns the type of the current summon decision under way, null otherwise */
	public SummonType getSummonType(){
		return summonType;
	}

	/** Creates a getGamePanel().getDecisionPanel() for creating either units or buildings */
	private void startSummonDecision(Commander c, Map<String, ? extends Unit> creatables){
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		ArrayList<Unit> units = Unit.sortedList(creatables.values());
		int i = 0;
		for(Unit u : units){
			String name = u.name;
			boolean ok = u.manaCost <= c.getMana();
			decisions.add(new Decision(i++, ok, name + Decision.SEPERATOR +"(" + u.manaCost + ")"));
		}
		decisionChoices = decisions.toArray(new Decision[decisions.size()]);
		decisionType = DecisionType.SUMMON_DECISION;
		decisionManditory = false;
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel(
				"Action > " + (summonType == SummonType.UNIT ? "Summon" : "Build")
				, c.owner, decisionChoices);
		getGamePanel().moveDecisionPanel();
	}

	/** Creates a getGamePanel().getDecisionPanel() for summoning new units */
	public void startSummonCombatantDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		summonType = SummonType.UNIT;
		startSummonDecision(c, c.getSummonables());
	}

	/** Creates a getGamePanel().getDecisionPanel() for summoning new buildings */
	public void startSummonBuildingDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		summonType = SummonType.BUILDING;
		startSummonDecision(c, c.getBuildables());
	}

	/** Creates a getGamePanel().getDecisionPanel() for choosing a spell to cast */
	public void startCastDecision(){
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		Commander c = (Commander) getGamePanel().boardCursor.getElm().getOccupyingUnit();
		LinkedList<Ability> abilities = c.getActiveAbilities();
		int i = 0;
		for(Ability a : abilities){
			String name = a.name;
			boolean ok = a.manaCost <= c.getMana();
			decisions.add(new Decision(i++, ok, name + Decision.SEPERATOR +"(" + a.manaCost + ")"));
		}
		decisionChoices = decisions.toArray(new Decision[decisions.size()]);
		decisionType = DecisionType.CAST_DECISION;
		decisionManditory = false;
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("Action > Cast", game.getCurrentPlayer(), decisionChoices);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes a casting decision */
	public void processCastDecision(Decision d){
		throw new UnsupportedOperationException();
	}

	/** Creates a new summon selector at the current getGamePanel().boardCursor position.
	 * 
	 */
	public void startSummonSelection(Decision decision){
		if(! decision.isSelectable()){
			return;
		}
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canSummon()){
			return;
		}
		String name = decision.getMessage().substring(0, decision.getMessage().indexOf(Decision.SEPERATOR));
		cancelDecision();
		Commander commander = t.getOccupyingUnit().owner.getCommander();
		Unit toSummon = commander.getUnitByName(name);
		locationSelector =new SummonSelector(this, t.getOccupyingUnit(), toSummon);
		ArrayList<Tile> cloud = locationSelector.getPossibleMovementsCloud();
		if(cloud.isEmpty()){
			locationSelector =null;
			return; //No possible summoning locations for this model.unit
		}
		Tile t2 = cloud.get(0);
		getGamePanel().boardCursor.setElm(t2);
		getGamePanel().fixScrollToShow(t2.getRow(), t2.getCol());
		addToggle(Toggle.SUMMON_SELECTION);
	}

	/** Cancels the summon selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because summonSelection wasn't happening. */
	public void cancelSummonSelection() throws RuntimeException{
		Toggle t =removeTopToggle();
		if(! t.equals(Toggle.SUMMON_SELECTION))
			throw new RuntimeException("Can't cancel summon selection, currently toggling " + getToggle());
		if(locationSelector != null){
			SummonSelector ss = (SummonSelector) locationSelector;
			getGamePanel().boardCursor.setElm(ss.summoner.getLocation());
		}
		summonType = null;
		locationSelector =null;
	}

	/** Processes the path selection - if ok, deletes it.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in path selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processSummonSelection(Tile loc) throws RuntimeException{
		SummonSelector summonSelector = (SummonSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.SUMMON_SELECTION))
			throw new RuntimeException("Can't cancel summon selection, currently toggling " + getToggle());
		summonSelector.toSummon.clone(summonSelector.summoner.owner, loc);
		summonType = null;
		locationSelector =null;
		getGamePanel().boardCursor.setElm(loc); //Cause info update
		repaint();
	}

	/** Creates a new pathSelector at the current getGamePanel().boardCursor position.
	 * Does nothing if the current tile is unoccupied or the model.unit has already moved. */
	public void startPathSelection(){
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canMove()) return;

		locationSelector =new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
		addToggle(Toggle.PATH_SELECTION);
	}

	/** Cancels the path selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because pathSelection wasn't happening. */
	public void cancelPathSelection() throws RuntimeException{
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getToggle());
		if(locationSelector != null){
			PathSelector ps = (PathSelector) locationSelector;
			getGamePanel().boardCursor.setElm(ps.getPath().getFirst());
		}
		locationSelector =null;
	}

	/** Processes the path selection - if ok, deletes it.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in path selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processPathSelection(Tile loc) throws RuntimeException{
		PathSelector pathSelector = (PathSelector) locationSelector;
		if(loc.isOccupied())
			return;
		if(pathSelector.getPath().size() < 2) return;
		if(! pathSelector.getPath().getLast().equals(loc))
			throw new RuntimeException("Can't do path to loc, incongruency");
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getToggle());
		pathSelector.unit.move(pathSelector.getPath());
		getGamePanel().boardCursor.setElm(loc);
		locationSelector =null;
	}

	/** Starts an attack selection - selects from units within range.
	 * Assumes model.unit the model.board cursor is currently on is the attacking Combatatant */
	public void startAttackSelection(){
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canFight()) return;
		Combatant attacker = (Combatant)t.getOccupyingUnit();
		locationSelector =new AttackSelector(this, attacker);
		if(locationSelector.getPossibleMovementsCloud().isEmpty()){
			locationSelector =null;
			return;
		}
		getGamePanel().boardCursor.setElm(locationSelector.getPossibleMovementsCloud().get(0));
		addToggle(Toggle.ATTACK_SELECTION);
	}

	/** Cancels the attack selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because attackSelection wasn't happening. */
	public void cancelAttackSelection() throws RuntimeException{
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.ATTACK_SELECTION))
			throw new RuntimeException("Can't cancel attack selection, currently toggling " + getToggle());
		if(locationSelector != null){
			AttackSelector as = (AttackSelector) locationSelector;
			getGamePanel().boardCursor.setElm(as.attacker.getLocation());
		}
		locationSelector =null;
	}

	/** Processes the attack selection - if ok, deletes it, do fight.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in attack selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processAttackSelection(Tile loc) throws RuntimeException{
		if(! loc.isOccupied()) return;
		AttackSelector attackSelector = (AttackSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.ATTACK_SELECTION))
			throw new RuntimeException("Can't cancel attack selection, currently toggling " + getToggle());
		Unit defender = loc.getOccupyingUnit();
		attackSelector.attacker.fight(defender);
		locationSelector =null;
		if(getGamePanel().getDecisionPanel() == null){
			startActionDecision();
			getGamePanel().boardCursor.setElm(attackSelector.attacker.getLocation());
		}
	}
	
}
