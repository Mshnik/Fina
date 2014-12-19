package gui.panel;

import game.Game;
import game.Player;
import gui.BoardCursor;
import gui.Frame;
import gui.ImageIndex;
import gui.LocationSelector;
import gui.MatrixPanel;
import gui.Paintable;
import gui.decision.AttackSelector;
import gui.decision.Decision;
import gui.decision.DecisionPanel;
import gui.decision.PathSelector;
import gui.decision.SummonSelector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import board.*;
import unit.*;
import unit.buildings.Baracks;

/** Drawable wrapper for a board object */
public class GamePanel extends MatrixPanel<Tile> implements Paintable{

	/***/
	private static final long serialVersionUID = 1L;

	/** Pixels (size) for each square tile. */
	public static final int CELL_SIZE = 64; 

	/** Shading for fog of war - translucent black */
	protected static final Color FOG_OF_WAR = new Color(0,0,0,0.75f);

	/** The BoardCursor for this GamePanel */
	public final BoardCursor boardCursor;

	/** The DecisionPanel that is currently active. Null if none */
	private DecisionPanel decisionPanel;

	/** The LocationSelector that is currently active. Null if none */
	private LocationSelector locationSelector;

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

	/** Constructor for GamePanel
	 * @param g - the game to display using this panel
	 * @param maxRows - the maximum number of rows of tiles to show at a time
	 * @param maxCols - the maximum number of cols of tiles to show at a time
	 */
	public GamePanel(Game g, int maxRows, int maxCols){
		super(g, maxCols, maxRows, 0, 0);		
		toggle = new Stack<Toggle>();
		boardCursor = new BoardCursor(this);
		setPreferredSize(new Dimension(getShowedCols() * CELL_SIZE, getShowedRows() * CELL_SIZE));
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

	/** Returns the current active decision panel, if any */
	public DecisionPanel getDecisionPanel(){
		return decisionPanel;
	}

	/** Cancels the currently selected decision for any decision
	 * Throws a runtimeException if this was a bad time to cancel because decision wasn't happening. */
	public void cancelDecision() throws RuntimeException{
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.DECISION))
			throw new RuntimeException("Can't cancel decision, currently toggling " + getToggle());
		getFrame().getAnimator().removeAnimatable(decisionPanel.cursor);
		decisionPanel = null;
		getFrame().setActiveCursor(boardCursor);
		repaint();
	}

	/** Creates a decisionPanel with the given decisionArray. 
	 * Fixes the location of the open decisionPanel for the location of the boardCursor,
	 * sets active toggle and active cursor, and repaints.
	 */
	private void fixDecisionPanel(DecisionPanel.Type type, Decision[] decisionsArr){
		decisionPanel = new DecisionPanel(game, type, Math.min(3, decisionsArr.length), decisionsArr);
		moveDecisionPanel();
		addToggle(Toggle.DECISION);
		getFrame().setActiveCursor(decisionPanel.cursor);
		repaint();
	}

	/** Moves the decision panel to accomidate the current location of the boardCursor *?
	 *
	 */
	private void moveDecisionPanel(){
		Tile t = boardCursor.getElm();
		int x = getXPosition(t);
		if(x < getWidth() / 2){
			x += GamePanel.CELL_SIZE + 5;
		} else{
			x -= (DecisionPanel.DECISION_WIDTH + 5);
		}
		int y = getYPosition(t);
		if(y > getHeight() / 2){
			y = getYPosition(t) - decisionPanel.getHeight() + GamePanel.CELL_SIZE;
		}
		decisionPanel.setXPosition(x);
		decisionPanel.setYPosition(y);
	}

	/** Creates a new Decision Panel for doing things with a unit, 
	 * shown to the side of the current tile (side depending
	 * on location of current tile)
	 * If there is no unit on the given tile or the unit doesn't belong to the current player, does nothing.
	 * If the unit on the tile, populates decisionPanel with all the possible decisions
	 */
	public void startActionDecision(){
		Tile t = boardCursor.getElm();
		if(! boardCursor.canSelect()) return;
		if(t.getOccupyingUnit() == null || t.getOccupyingUnit().owner != game.getCurrentPlayer()){
			return;
		} 
		Unit u = t.getOccupyingUnit();

		//Add choices based on the unit on this tile
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		int i = 0;
		if(u instanceof MovingUnit){
			decisions.add(new Decision(i++, u.canMove(), Unit.MOVE));
		}
		if(u instanceof Combatant){
			decisions.add(new Decision(i++, u.canFight() && ((Combatant) u).hasFightableTarget(), Unit.FIGHT));
		}
		if(u instanceof Summoner){
			decisions.add(new Decision(i++, ((Summoner) u).hasSummonSpace(), Unit.SUMMON));
			decisions.add(new Decision(i++, ((Summoner) u).hasBuildSpace(), Unit.BUILD));
		}

		//If there are no applicable choices, do nothing
		if(decisions.isEmpty()) return;

		//Otherwise, convert to array, create panel, set correct location on screen.
		Decision[] decisionsArr = decisions.toArray(new Decision[decisions.size()]);
		fixDecisionPanel(DecisionPanel.Type.ACTION_DECISION, decisionsArr);
	}

	/** Processes the currently selected Actiondecision.
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processActionDecision() throws RuntimeException{
		if(! decisionPanel.cursor.canSelect()) return;
		String choice = decisionPanel.getSelectedDecisionMessage();
		cancelDecision();
		switch(choice){
		case Unit.MOVE:
			startPathSelection();
			break;
		case Unit.SUMMON:
			startSummonCombatantDecision();
			break;
		case Unit.BUILD:
			startSummonBuildingDecision();
			break;
		case Unit.FIGHT:
			startAttackSelection();
			break;
		default:
			break;
		}
	}

	/** Starts a decisionPanel for ending the current player's turn */
	public void startEndTurnDecision(){
		Decision[] decisionsArr = {new Decision(0, Game.CANCEL), new Decision(1, Game.END_TURN)};
		fixDecisionPanel(DecisionPanel.Type.END_OF_TURN_DECISION, decisionsArr);
	}

	/** Processes an endOfTurn Decision */
	public void processEndTurnDecision(){
		if(! decisionPanel.cursor.canSelect()) return;
		String m = decisionPanel.getSelectedDecisionMessage();
		cancelDecision();
		switch(m){
		case Game.END_TURN:
			game.getCurrentPlayer().turnEnd();
			break;
		}
	}

	/** Creates a decisionPanel for creating either units or buildings */
	private void startSummonDecision(Commander c, Map<String, ? extends Unit> creatables){
		LinkedList<Decision> decisions = new LinkedList<Decision>();
		int i = 0;
		for(Entry<String, ? extends Unit> e : creatables.entrySet()){
			Unit u = e.getValue();
			String name = e.getKey();
			boolean ok = u.manaCost <= c.getMana();
			decisions.add(new Decision(i++, ok, name + Decision.SEPERATOR +"(" + u.manaCost + ")"));
		}
		Decision[] decisionsArr = decisions.toArray(new Decision[decisions.size()]);
		fixDecisionPanel(DecisionPanel.Type.SUMMON_DECISION, decisionsArr);
	}

	/** Creates a decisionPanel for summoning new units */
	public void startSummonCombatantDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		startSummonDecision(c, c.getSummonables());
	}

	/** Creates a decisionPanel for summoning new buildings */
	public void startSummonBuildingDecision(){
		Player p = game.getCurrentPlayer();
		Commander c = p.getCommander();
		startSummonDecision(c, c.getBuildables());
	}

	/** Creates a new summon selector at the current boardCursor position.
	 * 
	 */
	public void startSummonSelection(Decision decision){
		if(! decision.isSelectable()){
			return;
		}
		Tile t = boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canSummon()){
			return;
		}
		cancelDecision();
		String name = decision.getMessage().substring(0, decision.getMessage().indexOf(Decision.SEPERATOR));
		Commander commander = t.getOccupyingUnit().owner.getCommander();
		Unit toSummon = commander.getUnitByName(name);
		locationSelector = new SummonSelector(this, t.getOccupyingUnit(), toSummon);
		ArrayList<Tile> cloud = locationSelector.getPossibleMovementsCloud();
		if(cloud.isEmpty()){
			locationSelector = null;
			return; //No possible summoning locations for this unit
		}
		Tile t2 = cloud.get(0);
		boardCursor.setElm(t2);
		fixScrollToShow(t2.getRow(), t2.getCol());
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
			boardCursor.setElm(ss.summoner.getLocation());
		}
		locationSelector = null;
	}

	/** Processes the path selection - if ok, deletes it.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in path selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processSummonSelection() throws RuntimeException{
		if(! boardCursor.canSelect()) return;
		SummonSelector summonSelector = (SummonSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.SUMMON_SELECTION))
			throw new RuntimeException("Can't cancel summon selection, currently toggling " + getToggle());
		summonSelector.toSummon.clone(summonSelector.summoner.owner, boardCursor.getElm());
		locationSelector = null;
		boardCursor.setElm(boardCursor.getElm()); //Cause info update
		repaint();
	}

	/** Creates a new pathSelector at the current boardCursor position.
	 * Does nothing if the current tile is unoccupied or the unit has already moved. */
	public void startPathSelection(){
		Tile t = boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canMove()) return;

		locationSelector = new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
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
			boardCursor.setElm(ps.getPath().getFirst());
		}
		locationSelector = null;
	}

	/** Processes the path selection - if ok, deletes it.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in path selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processPathSelection() throws RuntimeException{
		if(! boardCursor.canSelect()) return;
		PathSelector pathSelector = (PathSelector) locationSelector;
		if(boardCursor.getElm().isOccupied() && 
				boardCursor.getElm().getOccupyingUnit().owner == pathSelector.unit.owner)
			return;
		if(pathSelector.getPath().size() < 2) return;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.PATH_SELECTION))
			throw new RuntimeException("Can't cancel path selection, currently toggling " + getToggle());
		Tile loc = pathSelector.unit.move(pathSelector.getPath());
		boardCursor.setElm(loc);
		locationSelector = null;
	}

	/** Starts an attack selection - selects from units within range.
	 * Assumes unit the board cursor is currently on is the attacking Combatatant */
	public void startAttackSelection(){
		Tile t = boardCursor.getElm();
		if(! t.isOccupied() || ! t.getOccupyingUnit().canFight()) return;
		Combatant attacker = (Combatant)t.getOccupyingUnit();
		locationSelector = new AttackSelector(this, attacker);
		if(locationSelector.getPossibleMovementsCloud().isEmpty()){
			locationSelector = null;
			return;
		}
		boardCursor.setElm(locationSelector.getPossibleMovementsCloud().get(0));
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
			boardCursor.setElm(as.attacker.getLocation());
		}
		locationSelector = null;
	}

	/** Processes the attack selection - if ok, deletes it, do fight.
	 * Do nothing if the path is empty (or length 1 - no movement) - stay in attack selection mode.
	 * Otherwise makes err noise or something. 
	 * Throws a runtimeException if this was a bad time to process because pathSelection wasn't happening. */
	public void processAttackSelection() throws RuntimeException{
		if(! boardCursor.canSelect()) return;
		if(! boardCursor.getElm().isOccupied()) return;
		AttackSelector attackSelector = (AttackSelector) locationSelector;
		Toggle t = removeTopToggle();
		if(! t.equals(Toggle.ATTACK_SELECTION))
			throw new RuntimeException("Can't cancel attack selection, currently toggling " + getToggle());
		Unit defender = boardCursor.getElm().getOccupyingUnit();

		attackSelector.attacker.fight(defender);
		game.getFrame().repaint();
		boardCursor.setElm(attackSelector.attacker.getLocation());
		locationSelector = null;
		startActionDecision();
	}

	/** Returns the current locationSelector, if any */
	public LocationSelector getLocationSelector(){
		return locationSelector;
	}

	/** Returns the frame this is drawn within */
	public Frame getFrame(){
		return game.getFrame();
	}

	/** Returns true if the given unit is visible to the current player, false otherwise.
	 * Helper for painting fog of war and hiding units */
	private boolean isVisible(Tile t){
		return ! game.isFogOfWar() || 
				(game.getCurrentPlayer() != null && game.getCurrentPlayer().canSee(t));
	}

	@Override
	/** Paints this boardpanel, for use in the frame it is in. */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;

		if(game.getCurrentPlayer() != null){
			g2d.setColor(FOG_OF_WAR);
		}
		//Paint the board itself, painting the portion within
		//[scrollY ... scrollY + maxY - 1], 
		//[scrollX ... scrollX + maxX - 1]
		for(int row = scrollY; row < scrollY + getShowedRows(); row ++){
			for(int col = scrollX; col < scrollX + getShowedCols(); col ++){
				Tile t = game.board.getTileAt(row, col);
				int x = getXPosition(t);
				int y = getYPosition(t);
				//Draw terrain
				g.drawImage(ImageIndex.imageForTile(t), x, y,
						CELL_SIZE, CELL_SIZE, null);

				//If the player can't see this tile, shade darkly.
				if(! isVisible(t)){
					g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
				}

				//Draw a unit if necessary - only if player can see it.
				if(t.isOccupied() && isVisible(t)){
					BufferedImage unitImg =ImageIndex.imageForUnit(t.getOccupyingUnit());
					g2d.drawImage(ImageIndex.tint(unitImg, game.getColorFor(t.getOccupyingUnit().owner)), x, y, 
							CELL_SIZE, CELL_SIZE, null);
				}
			}
		}

		//Draw the locationSelector, if there is one
		if(locationSelector != null){			
			locationSelector.paintComponent(g);
		}

		//Depending on the decision panel, draw ranges as applicable.
		//This happens when the action menu is up
		if(decisionPanel != null && decisionPanel.type == DecisionPanel.Type.ACTION_DECISION){
			g2d.setStroke(new BasicStroke(3));
			switch(decisionPanel.cursor.getElm().getMessage()){
			case Unit.FIGHT:
				g2d.setColor(Color.red);
				ImageIndex.drawRadial(boardCursor.getElm(), 
						boardCursor.getElm().getOccupyingUnit().getAttackRange() + 1, this, g2d);
				break;
			case Unit.BUILD:
			case Unit.SUMMON:
				g2d.setColor(Color.cyan);
				ImageIndex.drawRadial(boardCursor.getElm(), 
						boardCursor.getElm().getOccupyingUnit().getSummonRange(), this, g2d);
				break;

				
			}
		}
		
		//Draw the cursor
		boardCursor.paintComponent(g);

		//Draw the decisionPanel
		if(decisionPanel != null){
			decisionPanel.paintComponent(g);
		}
	}

	/** Returns the tile at the given row and col. Ignores scrolling for this. */
	@Override
	public Tile getElmAt(int row, int col) throws IllegalArgumentException {
		return game.board.getTileAt(row, col);
	}

	/** Returns the width of the board's matrix */
	@Override
	public int getMatrixWidth() {
		return game.board.getWidth();
	}

	/** Returns the height of the board's matrix */
	@Override
	public int getMatrixHeight() {
		return game.board.getHeight();
	}

	/** Returns GamePanel.CELL_SIZE */
	@Override
	public int getElementHeight() {
		return GamePanel.CELL_SIZE;
	}

	/** Returns GamePanel.CELL_SIZE */
	@Override
	public int getElementWidth() {
		return GamePanel.CELL_SIZE;
	}

}
