package controller.game;

import static ai.dummy.DoNothingAIController.DO_NOTHING_AI_TYPE;
import static ai.dummy.FullRandomAIController.FULL_RANDOM_AI_TYPE;
import static ai.dummy.MoveCommanderRandomlyAIController.MOVE_COMMANDER_RANDOMLY_AI_TYPE;
import static model.game.HumanPlayer.HUMAN_PLAYER_TYPE;

import ai.dummy.DoNothingAIController;
import ai.dummy.FullRandomAIController;
import ai.dummy.MoveCommanderRandomlyAIController;
import controller.audio.AudioController;
import controller.decision.Choice;
import controller.decision.Decision;
import controller.decision.Decision.DecisionType;
import controller.selector.AttackSelector;
import controller.selector.CastSelector;
import controller.selector.LocationSelector;
import controller.selector.PathSelector;
import controller.selector.SummonSelector;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;
import model.board.Board;
import model.board.Tile;
import model.game.AIPlayer;
import model.game.Game;
import model.game.Game.FogOfWar;
import model.game.HumanPlayer;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.combatant.Combat;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.unit.commander.DummyCommander;
import view.gui.Frame;
import view.gui.decision.DecisionCursor;
import view.gui.panel.BoardCursor;
import view.gui.panel.GamePanel;

/**
 * Overall controlling class that unites all classes. Should be run in its own thread, because some
 * methods may cause arbitrary waiting, hopefully not to clog the EVT
 *
 * @author MPatashnik /** The game that this controller wraps public final Game game;
 */
public final class GameController {
  /** Text for confirming a decision */
  public static final String CONFIRM = "Yes";
  /** Text for canceling a decision */
  public static final String CANCEL = "No";
  /** Text representing moving */
  public static final String MOVE = "Move";
  /** Text representing fighting */
  public static final String FIGHT = "Attack";
  /** Text representing commander actions (summon/build/cast) */
  public static final String COMMANDER_ACTION = "Cast";
  /** Text representing summoning */
  public static final String SUMMON = "Summon";
  /** Text representing building (building summoning) */
  public static final String BUILD = "Build";
  /** Text representing casting (Using active abilities) */
  public static final String CAST = "Magic";
  /** Text Suffix for toggling showing / unshowing the danger radius for this unit. */
  private static final String TOGGLE_DANGER_RADIUS = "Toggle Danger Zone";
  /**
   * Text representing into - the user can't click this, it changes the info panel when it is
   * hovered.
   */
  private static final String INFO_PREFIX = "Info: ";

  /** Colors that will be used to tint player units. */
  private static final Color[] playerColorsArr = {
    Color.BLUE, Color.MAGENTA, Color.YELLOW, Color.GREEN
  };

  /** Different possiblities for toggle options */
  public enum Toggle {
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
  public enum SummonType {
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

  /** The thread the game is running in. Null if the game isn't running right now. */
  private Thread gameThread;

  /** The types of players in this game, in turn order. */
  private final List<String> playerTypes;

  /** The game this is controlling */
  public final Game game;

  /** The level commanders start at in this game. */
  private final int startingCommanderLevel;

  /** The random instance used for generating combat damage. */
  private final Random random;

  /** The active location selector, if any */
  private LocationSelector locationSelector;

  /** Loads a board and starts the game in the same frame as this, then disposes of this. */
  static void loadAndStart(
      String boardFilepath,
      List<String> playerTypes,
      FogOfWar fogOfWar,
      int startingCommanderLevel) {
    if (playerTypes.size() < 2) {
      throw new RuntimeException("Can't have game with less than 2 players");
    }
    Frame f = new Frame(10, 20);
    InputController.setFrame(f);
    KeyboardListener.attachToFrame(f);
    MouseListener.attachToFrame(f);

    // Read board and create game.
    Board board = BoardReader.readBoard(boardFilepath);
    Game g = new Game(board, fogOfWar);
    GameController gc = new GameController(g, f, playerTypes, startingCommanderLevel);

    // Create players.
    for (int i = 0; i < playerTypes.size(); i++) {
      Player p;
      switch (playerTypes.get(i)) {
        case HUMAN_PLAYER_TYPE:
          p = new HumanPlayer(g, playerColorsArr[i]);
          break;
        case DO_NOTHING_AI_TYPE:
          p = new AIPlayer(g, playerColorsArr[i], new DoNothingAIController(5000));
          break;
        case MOVE_COMMANDER_RANDOMLY_AI_TYPE:
          p = new AIPlayer(g, playerColorsArr[i], new MoveCommanderRandomlyAIController());
          break;
        case FULL_RANDOM_AI_TYPE:
          p = new AIPlayer(g, playerColorsArr[i], new FullRandomAIController());
          break;
        default:
          throw new RuntimeException("Don't know how to handle player type " + playerTypes.get(i));
      }
      gc.frame.createViewOptionsForPlayer(p);
      new DummyCommander().createForPlayer(p, startingCommanderLevel);
    }

    // Start game.
    gc.start();
  }

  /** Creates a new game controller for the given game and frame. */
  private GameController(Game g, Frame f, List<String> playerTypes, int startingCommanderLevel) {
    game = g;
    this.startingCommanderLevel = startingCommanderLevel;
    this.playerTypes = Collections.unmodifiableList(playerTypes);
    game.setGameController(this);
    frame = f;
    frame.setController(this);
    random = new Random();

    playerColors = new HashMap<>();
    toggle = new Stack<>();
  }

  /** Returns iff the game is currently running */
  public boolean isRunning() {
    return game.isRunning();
  }

  /**
   * Starts this gameController running. Does nothing if currently running or game is already over
   */
  public synchronized void start() {
    if (isRunning() || game.isGameOver()) return;
    gameThread = new Thread(game);
    gameThread.setName("Game Thread");
    gameThread.start();
  }

  /** Stops this game controller. Does nothing if this game isn't running. */
  private synchronized void kill() {
    if (!isRunning()) return;
    frame.dispose();
    game.kill();
    gameThread.interrupt();
    AudioController.stopMusic();
  }

  /** Loads the given board and kills this. */
  public synchronized void loadAndKillThis(
      String boardFilepath,
      List<String> playerTypes,
      FogOfWar fogOfWar,
      int startingCommanderLevel) {
    kill();
    loadAndStart(boardFilepath, playerTypes, fogOfWar, startingCommanderLevel);
  }

  /** Restarts this game by creating a new copy of this then disposing of this. */
  public synchronized void restart() {
    kill();
    loadAndStart(game.board.filepath, playerTypes, game.getFogOfWar(), startingCommanderLevel);
  }

  /** Returns the random to use for combat. */
  public Random getCombatRandom() {
    return random;
  }

  /** Returns the gamePanel located within frame */
  public GamePanel getGamePanel() {
    return frame.getGamePanel();
  }

  /**
   * Adds this player to the model.game at the end of the player list. Throws a runtimeException if
   * the model.game is already underway. Returns the number of players after adding p
   */
  public int addPlayer(Player p, Color c) throws RuntimeException {
    if (isRunning())
      throw new RuntimeException(
          "Can't add " + p + " to " + this + " because game is already started");
    game.addPlayer(p);
    playerColors.put(p, c.darker());
    return game.getRemainingPlayers().size();
  }

  /** Starts a new ability decision for the given player - call during levelup */
  public void startNewAbilityDecision(Player p) {
    startNewAbilityDecision(p.getCommander());
  }

  /** Instructs the Frame to repaint */
  public void repaint() {
    frame.repaint();
  }

  /** Gets the color for the given player */
  public Color getColorFor(Player p) {
    return playerColors.get(p);
  }

  /** Called when the model wants to begin the turn for player p */
  public void startTurnFor(Player p) {
    frame.startTurnFor(p);
    AudioController.playMusicForTurn(p);
  }

  /**
   * Returns the current Toggle setting. Returns Toggle.NONE if there are no current toggles open
   */
  public Toggle getToggle() {
    try {
      return toggle.peek();
    } catch (EmptyStackException e) {
      return Toggle.NONE;
    }
  }

  /** Sets the current Toggle setting by adding it to the top of the stack */
  private void addToggle(Toggle t) {
    toggle.push(t);
  }

  /** Removes the top-most Toggle setting. Returns the removed setting for checking purposes */
  private Toggle removeTopToggle() {
    return toggle.pop();
  }

  /**
   * Cancels the currently selected decision for any decision Throws a runtimeException if this was
   * a bad time to cancel because decision wasn't happening. This should be called *after* all
   * necessary information is stored from the decision variables
   */
  void cancelDecision() throws RuntimeException {
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.DECISION))
      throw new RuntimeException("Can't cancel decision, currently toggling " + getToggle());
    frame.getAnimator().removeAnimatable(getGamePanel().getDecisionPanel().cursor);
    decision = null;
    frame.getGamePanel().setDecisionPanel(null);
    frame.setActiveCursor(getGamePanel().boardCursor);
    frame.getInfoPanel().clearExtendedModifiersInfo();
    repaint();
  }

  /** Returns the currently active location selector, if any */
  public LocationSelector getLocationSelector() {
    return locationSelector;
  }

  /** Returns the type of the current decision, if any */
  public DecisionType getDecisionType() {
    return decision.getType();
  }

  /** Returns iff the current decision is manditory */
  public boolean isDecisionManditory() {
    return decision.isManditory();
  }

  /**
   * Creates a new Decision Panel for doing things with a model.unit, shown to the side of the
   * current tile (side depending on location of current tile) If there is no model.unit on the
   * given tile or the model.unit doesn't belong to the current player and the current player can't
   * see the tile, does nothing. If the model.unit on the tile, populates
   * getGamePanel().getDecisionPanel() with all the possible decisions. If the current player
   * doesn't own the unit on the tile, decisions will all be informational only.
   */
  void startActionDecision() {
    Tile t = getGamePanel().boardCursor.getElm();
    if (!getGamePanel().boardCursor.canSelect()) return;
    if (t.getOccupyingUnit() == null
        || (game.isRunning()
            && t.getOccupyingUnit().owner != game.getCurrentPlayer()
            && !game.getCurrentPlayer().canSee(t))) {
      return;
    }
    Unit u = t.getOccupyingUnit();

    // Add choices based on the model.unit on this tile.
    ArrayList<Choice> choices = new ArrayList<>();
    // Actionable choices - requires active and local human player.
    if (game.isRunning()
        && u.owner == game.getCurrentPlayer()
        && game.getCurrentPlayer().isLocalHumanPlayer()) {
      if (u instanceof MovingUnit) {
        choices.add(new Choice(u.canMove(), MOVE, u));
      }
      if (u instanceof Combatant) {
        choices.add(new Choice(u.canFight() && ((Combatant) u).hasFightableTarget(), FIGHT, u));
      }
      if (u instanceof Commander || u instanceof Summoner) {
        int actionsRemaining = u.getActionsRemaining();
        choices.add(
            new Choice(actionsRemaining > 0, COMMANDER_ACTION + " (" + actionsRemaining + ")", u));
      }
    }
    // Information choices - can be either player.
    if (u.getVisibleModifiers().size() > 0) {
      choices.add(
          new Choice(true, INFO_PREFIX + DecisionCursor.SHOW_EXTENDED_MODIFIERS_INFO_MESSAGE, u));
    }
    if (u instanceof Combatant) {
      choices.add(new Choice(true, TOGGLE_DANGER_RADIUS, u));
    }

    // If there are no applicable choices, do nothing
    if (choices.isEmpty()) return;

    // Otherwise, convert to array, create panel, set correct location on screen.
    decision = new Decision(DecisionType.ACTION_DECISION, false, true, choices);
    addToggle(Toggle.DECISION);
    getGamePanel().fixDecisionPanel("Action", game.getCurrentPlayer(), decision, true);
    getGamePanel().moveDecisionPanel();
  }

  /**
   * Processes the currently selected Action decision. Throws a runtimeException if an unknown
   * switch case is encountered.
   */
  void processActionDecision(Choice c) throws RuntimeException {
    String choice = c.getMessage().replaceAll(" \\([0-9]*\\)", "");
    Object val = c.getVal();
    if (choice.contains(INFO_PREFIX)) {
      return;
    }
    cancelDecision();
    switch (choice) {
      case MOVE:
        startPathSelection();
        break;
      case COMMANDER_ACTION:
        startCommanderActionDecision(c.getVal() instanceof Commander);
        break;
      case FIGHT:
        startAttackSelection();
        break;
      case TOGGLE_DANGER_RADIUS:
        frame
            .getViewOptionsForPlayer(game.getCurrentPlayer())
            .toggleDangerRadiusUnit((Combatant) val);
        break;
      default:
        throw new RuntimeException("Don't know how to handle this choice " + choice);
    }
  }

  /**
   * Starts a getGamePanel().getDecisionPanel() for performing a commander action. isCommander is
   * true if the commander was selected - otherwise could be any summoner the player controls.
   */
  void startCommanderActionDecision(boolean isCommander) {
    Tile t = getGamePanel().boardCursor.getElm();
    Summoner s = (Summoner) t.getOccupyingUnit();

    // Add choices based on the model.unit on this tile
    LinkedList<Choice> choices = new LinkedList<>();
    choices.add(new Choice(s.hasSummonSpace(), SUMMON));
    choices.add(new Choice(s.hasBuildSpace(), BUILD));
    if (isCommander) {
      Commander c = (Commander) s;
      choices.add(new Choice(c.canCast(), CAST));
    }

    // If there are no applicable choices, do nothing
    if (choices.isEmpty()) return;

    // Otherwise, convert to array, create panel, set correct location on screen.
    decision = new Decision(DecisionType.COMMANDER_ACTION_DECISION, false, true, choices);
    addToggle(Toggle.DECISION);
    getGamePanel()
        .fixDecisionPanel("Action > " + COMMANDER_ACTION, game.getCurrentPlayer(), decision, true);
    getGamePanel().moveDecisionPanel();
  }

  /**
   * Processes the currently selected CommanderAction decision. Throws a runtimeException if an
   * unknown switch case is encountered.
   */
  void processCommanderActionDecision(Choice c) throws RuntimeException {
    String choice = c.getMessage();
    cancelDecision();
    switch (choice) {
      case SUMMON:
        startSummonCombatantDecision();
        break;
      case BUILD:
        startSummonBuildingDecision();
        break;
      case CAST:
        startCastDecision();
        break;
      default:
        throw new RuntimeException("Don't know how to handle this choice " + choice);
    }
  }

  /** Starts a getGamePanel().getDecisionPanel() for ending the current player's turn */
  void startEndTurnDecision() {
    if (game.isRunning() && game.getCurrentPlayer().isLocalHumanPlayer()) {
      decision =
          new Decision(
              DecisionType.END_OF_TURN_DECISION,
              false,
              true,
              new Choice(true, GameController.CANCEL),
              new Choice(true, GameController.CONFIRM));
      addToggle(Toggle.DECISION);
      getGamePanel().fixDecisionPanel("End Turn?", game.getCurrentPlayer(), decision, true);
      getGamePanel().moveDecisionPanel();
    }
  }

  /** Processes an endOfTurn Decision */
  void processEndTurnDecision(Choice c) {
    String m = c.getMessage();
    cancelDecision();
    switch (m) {
      case GameController.CONFIRM:
        game.getCurrentPlayer().endTurn();
        break;
    }
  }

  /**
   * Starts a levelup ability selection decision. Assumes c has leveled up but hasn't chosen an
   * ability yet
   */
  private void startNewAbilityDecision(Commander c) throws RuntimeException {
    // TODO - revisit this
    //    if (c.getAbility(c.getLevel()) != null)
    //      throw new RuntimeException(
    //          "Can't pick a new ability for " + c + ", already has " +
    // c.getAbility(c.getLevel()));
    //
    //    Ability[] a = c.getPossibleAbilities(c.getLevel());
    //    if (a != null) {
    //      decision = new Decision(DecisionType.NEW_ABILITY_DECISION, true, true);
    //      for (Ability ab : a) {
    //        decision.add(new Choice(true, ab.name, ab));
    //      }
    //      addToggle(Toggle.DECISION);
    //      getGamePanel().fixDecisionPanel("Choose a New Ability", c.owner, decision, true);
    //      getGamePanel().centerDecisionPanel();
    //    }
    // else do nothing
  }

  /** Processes the levelup new ability decision */
  void processNewAbilityDecision(Choice c) {
    Commander com = getGamePanel().getDecisionPanel().player.getCommander();
    int index = c.getIndex();
    cancelDecision();
    com.chooseAbility(index);
  }

  /** Returns the type of the current summon decision under way, null otherwise */
  SummonType getSummonType() {
    return summonType;
  }

  /** Creates a getGamePanel().getDecisionPanel() for creating either units or buildings */
  private void startSummonDecision(Commander c, List<? extends Unit> creatables) {
    LinkedList<Choice> choices = new LinkedList<Choice>();
    Tile t = getGamePanel().boardCursor.getElm();
    if (!(t.getOccupyingUnit() instanceof Summoner)) {
      throw new RuntimeException(t.getOccupyingUnit() + " can't summon");
    }
    for (Unit u : creatables) {
      choices.add(
          new Choice(
              u.getManaCostWithScalingAndDiscountsForPlayer(c.owner) <= c.getMana()
                  && !new SummonSelector<>(this, (Unit & Summoner) t.getOccupyingUnit(), u)
                      .getCloud()
                      .isEmpty(),
              u.name
                  + Choice.SEPERATOR
                  + " ("
                  + u.getManaCostWithScalingAndDiscountsForPlayer(c.owner)
                  + ")",
              u));
    }
    decision = new Decision(DecisionType.SUMMON_DECISION, false, true, choices);
    addToggle(Toggle.DECISION);
    getGamePanel()
        .fixDecisionPanel(
            "Action > "
                + COMMANDER_ACTION
                + " > "
                + (summonType == SummonType.UNIT ? "Summon" : "Build"),
            c.owner,
            decision,
            true);
    getGamePanel().moveDecisionPanel();
  }

  /** Creates a getGamePanel().getDecisionPanel() for summoning new units */
  void startSummonCombatantDecision() {
    Player p = game.getCurrentPlayer();
    Commander c = p.getCommander();
    summonType = SummonType.UNIT;
    startSummonDecision(
        c,
        c.getSummonables()
            .values()
            .stream()
            .sorted(
                Comparator.comparing((Combatant combatant) -> combatant.combatantClasses.get(0))
                    .thenComparing(
                        combatant -> combatant.getManaCostWithScalingAndDiscountsForPlayer(p)))
            .collect(Collectors.toList()));
  }

  /** Creates a getGamePanel().getDecisionPanel() for summoning new buildings */
  void startSummonBuildingDecision() {
    Player p = game.getCurrentPlayer();
    Commander c = p.getCommander();
    summonType = SummonType.BUILDING;
    startSummonDecision(
        c,
        c.getBuildables()
            .values()
            .stream()
            .sorted(
                Comparator.comparingInt(
                    building -> building.getManaCostWithScalingAndDiscountsForPlayer(p)))
            .collect(Collectors.toList()));
  }

  /**
   * Creates a new summon selector at the current getGamePanel().boardCursor position. Returns true
   * iff a summoning decision was started.
   */
  boolean startSummonSelection(Choice choice) {
    if (!choice.isSelectable()) {
      return false;
    }
    Tile t = getGamePanel().boardCursor.getElm();
    if (!t.isOccupied() || !t.getOccupyingUnit().canSummon()) {
      return false;
    }
    String name = choice.getShortMessage();
    cancelDecision();
    Commander commander = t.getOccupyingUnit().owner.getCommander();
    Unit toSummon = commander.getUnitByName(name);
    locationSelector = new SummonSelector(this, t.getOccupyingUnit(), toSummon);
    ArrayList<Tile> cloud = locationSelector.getCloud();
    if (cloud.isEmpty()) {
      locationSelector = null;
      return false; // No possible summoning locations for this model.unit
    }
    Tile t2 = cloud.get(0);
    getGamePanel().boardCursor.setElm(t2);
    getGamePanel().fixScrollToShow(t2.getRow(), t2.getCol());
    addToggle(Toggle.SUMMON_SELECTION);
    return true;
  }

  /**
   * Cancels the summon selection - deletes it but does nothing. Throws a runtimeException if this
   * was a bad time to cancel because summonSelection wasn't happening.
   */
  void cancelSummonSelection() throws RuntimeException {
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.SUMMON_SELECTION))
      throw new RuntimeException(
          "Can't cancel summon selection, currently toggling " + getToggle());
    if (locationSelector != null) {
      SummonSelector ss = (SummonSelector) locationSelector;
      getGamePanel().boardCursor.setElm(ss.summoner.getLocation());
    }
    summonType = null;
    locationSelector = null;
  }

  /**
   * Processes the summoning selection - if ok, deletes it. Creates a new copy of the unit to summon
   * and charges the summoning player the cost. Throws a runtimeException if this was a bad time to
   * process because summonSelection wasn't happening.
   */
  void processSummonSelection(Tile loc) throws RuntimeException {
    SummonSelector summonSelector = (SummonSelector) locationSelector;
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.SUMMON_SELECTION))
      throw new RuntimeException(
          "Can't cancel summon selection, currently toggling " + getToggle());
    summonUnit(summonSelector.summoner, loc, summonSelector.toSummon);
    summonType = null;
    locationSelector = null;
    getGamePanel().boardCursor.setElm(loc); // Cause info update
    repaint();
  }

  /**
   * Actually does the summoning effect - exposed so AI can do it without having to go through the
   * selector process.
   */
  public void summonUnit(Unit summoner, Tile loc, Unit toSummon) {
    summoner.spendAction();
    Unit summonedUnit = toSummon.clone(summoner.owner, loc);
    summonedUnit.copyPersonalModifiersFrom(toSummon);
    summonedUnit.owner.recalculateState();
  }

  /** Creates a getGamePanel().getDecisionPanel() for choosing a spell to cast */
  void startCastDecision() {
    LinkedList<Choice> choices = new LinkedList<Choice>();
    Commander c = (Commander) getGamePanel().boardCursor.getElm().getOccupyingUnit();
    List<Ability> abilities =
        c.getCastables()
            .values()
            .stream()
            .sorted(
                Comparator.comparing((Ability a) -> a.abilityType)
                    .thenComparing(a -> a.getManaCostWithDiscountsForPlayer(c.owner)))
            .collect(Collectors.toList());
    for (Ability a : abilities) {
      choices.add(
          new Choice(
              a.getManaCostWithDiscountsForPlayer(c.owner) <= c.getMana()
                  && !new CastSelector(this, c, a).getCloud().isEmpty(),
              a.name + Choice.SEPERATOR + " (" + a.manaCost + ")",
              a));
    }
    decision = new Decision(DecisionType.CAST_DECISION, false, true, choices);
    addToggle(Toggle.DECISION);
    getGamePanel()
        .fixDecisionPanel(
            "Action > " + COMMANDER_ACTION + " > " + CAST, game.getCurrentPlayer(), decision, true);
    getGamePanel().moveDecisionPanel();
  }

  /** Processes a casting decision */
  void startCastSelection(Choice choice) {
    if (!choice.isSelectable()) {
      return;
    }
    Tile t = getGamePanel().boardCursor.getElm();
    if (!t.isOccupied() || !(t.getOccupyingUnit() instanceof Commander)) {
      return;
    }
    cancelDecision();
    Commander c = (Commander) t.getOccupyingUnit();
    Ability a = (Ability) choice.getVal();
    if (a != null && c != null) {
      locationSelector = new CastSelector(this, c, a);
      ArrayList<Tile> cloud = locationSelector.getCloud();
      if (cloud.isEmpty()) {
        locationSelector = null;
        return; // No possible casting locations for this model.unit
      }
      addToggle(Toggle.CAST_SELECTION);
      Tile t2 = cloud.get(0);
      getGamePanel().boardCursor.setElm(t2);
      getGamePanel().fixScrollToShow(t2.getRow(), t2.getCol());
      ((CastSelector) locationSelector).refreshEffectCloud();
    } else {
      throw new RuntimeException("Expected commander and ability");
    }
  }

  /**
   * Cancels the summon selection - deletes it but does nothing. Throws a runtimeException if this
   * was a bad time to cancel because summonSelection wasn't happening.
   */
  void cancelCastSelection() throws RuntimeException {
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.CAST_SELECTION))
      throw new RuntimeException("Can't cancel cast selection, currently toggling " + getToggle());
    if (locationSelector != null) {
      CastSelector cs = (CastSelector) locationSelector;
      getGamePanel().boardCursor.setElm(cs.caster.getLocation());
    }
    locationSelector = null;
  }

  /** Processes the cast selection, causing the ability to actually occur. */
  void processCastSelection(Tile loc) {
    CastSelector castSelector = (CastSelector) locationSelector;
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.CAST_SELECTION))
      throw new RuntimeException("Can't process cast selection, currently toggling " + getToggle());
    if (game.getCurrentPlayer() != null) {
      game.getCurrentPlayer().getCommander().spendAction();
    }
    castSelector.toCast.cast(
        castSelector.caster, loc, castSelector.caster.owner.getCastCloudBoost(), random);
    locationSelector = null;
    castSelector.caster.owner.refreshVisionCloud();
    getGamePanel().boardCursor.setElm(castSelector.caster.getLocation()); // Cause info update
    repaint();
  }

  /**
   * Creates a new pathSelector at the current getGamePanel().boardCursor position. Does nothing if
   * the current tile is unoccupied or the unit has already moved.
   */
  private void startPathSelection() {
    Tile t = getGamePanel().boardCursor.getElm();
    if (!t.isOccupied() || !t.getOccupyingUnit().canMove()) return;

    locationSelector = new PathSelector(this, (MovingUnit) t.getOccupyingUnit());
    addToggle(Toggle.PATH_SELECTION);
  }

  /**
   * Cancels the path selection - deletes it but does nothing. Throws a runtimeException if this was
   * a bad time to cancel because pathSelection wasn't happening.
   */
  void cancelPathSelection() throws RuntimeException {
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.PATH_SELECTION))
      throw new RuntimeException("Can't cancel path selection, currently toggling " + getToggle());
    if (locationSelector != null) {
      PathSelector ps = (PathSelector) locationSelector;
      getGamePanel().boardCursor.setElm(ps.getPath().getFirst());
    }
    locationSelector = null;
  }

  /**
   * Processes the path selection - if ok, deletes it. Do nothing if the path is empty (or length 1
   * - no movement) - stay in path selection mode. Otherwise makes err noise or something. Throws a
   * runtimeException if this was a bad time to process because pathSelection wasn't happening.
   */
  void processPathSelection(Tile loc) throws RuntimeException {
    PathSelector pathSelector = (PathSelector) locationSelector;
    if (loc.isOccupied()) return;
    if (pathSelector.getPath().size() < 2) return;
    if (!pathSelector.getPath().getLast().equals(loc))
      throw new RuntimeException("Can't do path to loc, incongruency");
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.PATH_SELECTION))
      throw new RuntimeException("Can't cancel path selection, currently toggling " + getToggle());
    pathSelector.unit.move(pathSelector.getPath());
    getGamePanel().boardCursor.setElm(loc);
    locationSelector = null;
  }

  /**
   * Starts an attack selection - selects from units within range. Assumes model.unit the
   * model.board cursor is currently on is the attacking Combatatant
   */
  private void startAttackSelection() {
    Tile t = getGamePanel().boardCursor.getElm();
    if (!t.isOccupied() || !t.getOccupyingUnit().canFight()) return;
    Combatant attacker = (Combatant) t.getOccupyingUnit();
    locationSelector = new AttackSelector(this, attacker);
    if (locationSelector.getCloud().isEmpty()) {
      locationSelector = null;
      return;
    }
    getGamePanel().boardCursor.setElm(locationSelector.getCloud().get(0));
    addToggle(Toggle.ATTACK_SELECTION);
  }

  /**
   * Cancels the attack selection - deletes it but does nothing. Throws a runtimeException if this
   * was a bad time to cancel because attackSelection wasn't happening.
   */
  void cancelAttackSelection() throws RuntimeException {
    Toggle t = removeTopToggle();
    if (!t.equals(Toggle.ATTACK_SELECTION))
      throw new RuntimeException(
          "Can't cancel attack selection, currently toggling " + getToggle());
    if (locationSelector != null) {
      AttackSelector as = (AttackSelector) locationSelector;
      getGamePanel().boardCursor.setElm(as.attacker.getLocation());
    }
    locationSelector = null;
  }

  /**
   * Starts a getGamePanel().getDecisionPanel() for confirming an attack decision. Creates a combat
   * instance and sets it as the value on both decisions.
   */
  void startConfirmAttackDecision() {
    if (!frame.getActiveCursor().canSelect()) return;
    Tile loc = ((BoardCursor) frame.getActiveCursor()).getElm();
    if (!loc.isOccupied()) return;
    AttackSelector attackSelector = (AttackSelector) locationSelector;
    Unit defender = loc.getOccupyingUnit();
    Combat combat = new Combat(attackSelector.attacker, defender);

    decision =
        new Decision(
            DecisionType.CONFIRM_ATTACK_DECISION,
            false,
            false,
            new Choice(true, GameController.CANCEL, combat),
            new Choice(true, GameController.CONFIRM, combat));
    addToggle(Toggle.DECISION);
    getGamePanel().fixDecisionPanel("Attack?", game.getCurrentPlayer(), decision, false);
    getGamePanel().moveDecisionPanel();
  }

  /**
   * Processes a confirm attack decision. If confirmed, process the attack. Expects a combat
   * instance to be set as the value for c.
   */
  void processConfirmAttackDecision(Choice c) {
    String m = c.getMessage();
    cancelDecision();
    switch (m) {
      case GameController.CONFIRM:
        Combat combat = (Combat) c.getVal();

        // Check that we're attacking. If so, process.
        Toggle t = removeTopToggle();
        if (!t.equals(Toggle.ATTACK_SELECTION))
          throw new RuntimeException(
              "Can't cancel attack selection, currently toggling " + getToggle());

        combat.process(random);
        locationSelector = null;
        if (getGamePanel().getDecisionPanel() == null) {
          getGamePanel().boardCursor.setElm(combat.attacker.getLocation());
        }
        break;
    }
  }
}
