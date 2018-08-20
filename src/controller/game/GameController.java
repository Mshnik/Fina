package controller.game;

import java.awt.Color;
import java.util.*;

import controller.decision.*;
import controller.decision.Decision.DecisionType;
import controller.selector.*;

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
public final class GameController {
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
		CAST_SELECTION,
		ATTACK_SELECTION
	}

	/** The layers of active toggles. Topmost is the current toggle */
	private Stack<Toggle> toggle;

	/** Different types of summoning */
	public enum SummonType{
		UNIT,
		BUILDING
	}

	/** The type of the most recent decision to summon. Null if none is currently in progress */
	private SummonType summonType;

	/** Current decision that is underway. Null if none */
	private Decision decision;

	/** A hashmap from each player to the color to tint their units */
	private final HashMap<Player, Color> playerColors;

	/** The Frame that is drawing this Game */
	public final Frame frame;

	/** The game this is controlling */
	public final Game game;

	/** The random instance used for generating combat damage. */
	private final Random random;

	/** The active location selector, if any */
	private LocationSelector locationSelector;

	public GameController(Game g, Frame f){
		game = g;
		game.setGameController(this);
		frame = f;
		frame.setController(this);
		random = new Random();

		playerColors = new HashMap<>();
		toggle = new Stack<>();
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
	private void addToggle(Toggle t){
		toggle.push(t);
	}

	/** Removes the top-most Toggle setting. 
	 * Returns the removed setting for checking purposes */
	private Toggle removeTopToggle(){
		return toggle.pop();
	}

	/** Cancels the currently selected decision for any decision
	 * Throws a runtimeException if this was a bad time to cancel because decision wasn't happening.
	 * This should be called *after* all necessary information is stored from the decision variables */
	void cancelDecision() throws RuntimeException{
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.DECISION))
			throw new RuntimeException("Can't cancel decision, currently toggling " + getToggle());
		frame.getAnimator().removeAnimatable(getGamePanel().getDecisionPanel().cursor);
		decision = null;
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
		return decision.getType();
	}

	/** Returns iff the current decision is manditory */
	public boolean isDecisionManditory(){
		return decision.isManditory();
	}

	/** Creates a new Decision Panel for doing things with a model.unit, 
	 * shown to the side of the current tile (side depending
	 * on location of current tile)
	 * If there is no model.unit on the given tile or the model.unit doesn't belong to the current player, does nothing.
	 * If the model.unit on the tile, populates getGamePanel().getDecisionPanel() with all the possible decisions
	 */
	void startActionDecision(){
		Tile t = getGamePanel().boardCursor.getElm();
		if(! getGamePanel().boardCursor.canSelect()) return;
		if(t.getOccupyingUnit() == null || t.getOccupyingUnit().owner != game.getCurrentPlayer()){
			return;
		} 
		Unit u = t.getOccupyingUnit();

		//Add choices based on the model.unit on this tile
		LinkedList<Choice> choices = new LinkedList<Choice>();
		if(u instanceof MovingUnit){
			choices.add(new Choice(u.canMove(), MOVE));
		}
		if(u instanceof Combatant){
			choices.add(new Choice(u.canFight() && ((Combatant) u).hasFightableTarget(), FIGHT));
		}
		if(u instanceof Summoner){
			choices.add(new Choice(((Summoner) u).hasSummonSpace(), SUMMON));
			choices.add(new Choice(((Summoner) u).hasBuildSpace(), BUILD));
		}
		if(u instanceof Commander){
			choices.add(new Choice(((Commander) u).canCast(), CAST));
		}

		//If there are no applicable choices, do nothing
		if(choices.isEmpty()) return;

		//Otherwise, convert to array, create panel, set correct location on screen.
		decision = new Decision(DecisionType.ACTION_DECISION, false, choices);
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("Action", game.getCurrentPlayer(), decision);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes the currently selected Actiondecision.
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	void processActionDecision(Choice c) throws RuntimeException{
		String choice = c.getMessage();
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
	void startEndTurnDecision(){
		decision = new Decision(DecisionType.END_OF_TURN_DECISION, false, 
				new Choice(true, GameController.CANCEL), new Choice(true, GameController.END_TURN));
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("End Turn?", game.getCurrentPlayer(), decision);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes an endOfTurn Decision */
	void processEndTurnDecision(Choice c){
		String m = c.getMessage();
		cancelDecision();
		switch(m){
		case GameController.END_TURN:
			game.getCurrentPlayer().turnEnd();
			break;
		}
	}


	/** Starts  a levelup ability selection decision.
	 * Assumes c has leveled up but hasn't chosen an ability yet */
	private void startNewAbilityDecision(Commander c) throws RuntimeException{
		if(c.getAbility(c.getLevel()) != null)
			throw new RuntimeException("Can't pick a new ability for " + c 
					+", already has " + c.getAbility(c.getLevel()));

		Ability[] a = c.getPossibleAbilities(c.getLevel());
		if(a != null){
			decision = new Decision(DecisionType.NEW_ABILITY_DECISION, true);
			for(Ability ab : a){
				decision.add(new Choice(true, ab.name, ab));
			}
			addToggle(Toggle.DECISION);
			getGamePanel().fixDecisionPanel("Choose a New Ability", c.owner, decision);
			getGamePanel().centerDecisionPanel();
		} 
		//else do nothing
	}

	/** Processes the levelup new ability decision */
	void processNewAbilityDecision(Choice c){
		Commander com = getGamePanel().getDecisionPanel().player.getCommander();
		int index = c.getIndex();
		cancelDecision();
		com.chooseAbility(index);
	}

	/** Returns the type of the current summon decision under way, null otherwise */
	public SummonType getSummonType(){
		return summonType;
	}

	/** Creates a getGamePanel().getDecisionPanel() for creating either units or buildings */
	private void startSummonDecision(Commander c, Map<String, ? extends Unit> creatables){
		LinkedList<Choice> choices = new LinkedList<Choice>();
		ArrayList<Unit> units = Unit.sortedList(creatables.values());
		for(Unit u : units){
			choices.add(new Choice(u.manaCost <= c.getMana(), 
					u.name + Choice.SEPERATOR +"(" + u.manaCost + ")", u));
		}
		decision = new Decision(DecisionType.SUMMON_DECISION, false, choices);
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel(
				"Action > " + (summonType == SummonType.UNIT ? "Summon" : "Build")
				, c.owner, decision);
		getGamePanel().moveDecisionPanel();
	}

	/** Creates a getGamePanel().getDecisionPanel() for summoning new units */
	void startSummonCombatantDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		summonType = SummonType.UNIT;
		startSummonDecision(c, c.getSummonables());
	}

	/** Creates a getGamePanel().getDecisionPanel() for summoning new buildings */
	void startSummonBuildingDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		summonType = SummonType.BUILDING;
		startSummonDecision(c, c.getBuildables());
	}

	/** Creates a new summon selector at the current getGamePanel().boardCursor position.
	 * 
	 */
	void startSummonSelection(Choice choice){
		if(! choice.isSelectable()){
			return;
		}
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canSummon()){
			return;
		}
		String name = choice.getShortMessage();
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
	void cancelSummonSelection() throws RuntimeException{
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
	void processSummonSelection(Tile loc) throws RuntimeException{
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

	/** Creates a getGamePanel().getDecisionPanel() for choosing a spell to cast */
	void startCastDecision(){
		LinkedList<Choice> choices = new LinkedList<Choice>();
		Commander c = (Commander) getGamePanel().boardCursor.getElm().getOccupyingUnit();
		LinkedList<Ability> abilities = c.getActiveAbilities();
		for(Ability a : abilities){
			choices.add(new Choice(a.manaCost <= c.getMana(), 
					a.name + Choice.SEPERATOR +"(" + a.manaCost + ")", a));
		}
		decision = new Decision(DecisionType.CAST_DECISION, false, choices);
		addToggle(Toggle.DECISION);
		getGamePanel().fixDecisionPanel("Action > Cast", game.getCurrentPlayer(), decision);
		getGamePanel().moveDecisionPanel();
	}

	/** Processes a casting decision */
	void startCastSelection(Choice choice){
		if(! choice.isSelectable()){
			return;
		}
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! (t.getOccupyingUnit() instanceof Commander)){
			return;
		}
		String name = choice.getShortMessage();
		cancelDecision();
		Commander c = (Commander)t.getOccupyingUnit();
		Ability a = c.getAbilityByName(name);
		if(a != null && c != null){
			locationSelector = new CastSelector(this, c, a);
			ArrayList<Tile> cloud = locationSelector.getPossibleMovementsCloud();
			if(cloud.isEmpty()){
				locationSelector =null;
				return; //No possible summoning locations for this model.unit
			}
			addToggle(Toggle.CAST_SELECTION);
			if(a.castDist == 0){
				processCastSelection(t);
			} else{
				Tile t2 = cloud.get(0);
				getGamePanel().boardCursor.setElm(t2);
				getGamePanel().fixScrollToShow(t2.getRow(), t2.getCol());
			}
		}
	}
	
	/** Cancels the summon selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because summonSelection wasn't happening. */
	public void cancelCastSelection() throws RuntimeException{
		Toggle t =removeTopToggle();
		if(! t.equals(Toggle.CAST_SELECTION))
			throw new RuntimeException("Can't cancel cast selection, currently toggling " + getToggle());
		if(locationSelector != null){
			CastSelector cs = (CastSelector) locationSelector;
			getGamePanel().boardCursor.setElm(cs.caster.getLocation());
		}
		locationSelector =null;
	}

	void processCastSelection(Tile loc){
		CastSelector castSelector = (CastSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.CAST_SELECTION))
			throw new RuntimeException("Can't process cast selection, currently toggling " + getToggle());
		castSelector.toCast.cast(loc);
		locationSelector =null;
		getGamePanel().boardCursor.setElm(castSelector.caster.getLocation()); //Cause info update
		repaint();
	}

	/** Creates a new pathSelector at the current getGamePanel().boardCursor position.
	 * Does nothing if the current tile is unoccupied or the model.unit has already moved. */
	void startPathSelection(){
		Tile t = getGamePanel().boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canMove()) return;

		locationSelector =new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
		addToggle(Toggle.PATH_SELECTION);
	}

	/** Cancels the path selection - deletes it but does nothing.
	 * Throws a runtimeException if this was a bad time to cancel because pathSelection wasn't happening. */
	void cancelPathSelection() throws RuntimeException{
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
	void processPathSelection(Tile loc) throws RuntimeException{
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
	void startAttackSelection(){
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
	void cancelAttackSelection() throws RuntimeException{
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
	void processAttackSelection(Tile loc) throws RuntimeException{
		if(! loc.isOccupied()) return;
		AttackSelector attackSelector = (AttackSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.ATTACK_SELECTION))
			throw new RuntimeException("Can't cancel attack selection, currently toggling " + getToggle());
		Unit defender = loc.getOccupyingUnit();
		attackSelector.attacker.fight(defender, random);
		locationSelector =null;
		if(getGamePanel().getDecisionPanel() == null){
			startActionDecision();
			getGamePanel().boardCursor.setElm(attackSelector.attacker.getLocation());
		}
	}

}
