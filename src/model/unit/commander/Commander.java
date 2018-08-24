package model.unit.commander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Ability;
import model.unit.building.Building;
import model.unit.building.Buildings;
import model.unit.combatant.Combatant;
import model.unit.combatant.FileCombatant;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.Modifier.StackMode;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.StatModifier;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

/**
 * Represents a commander for a player. Each player should have one.
 *
 * <p>Commanders have mana and a level to maintain
 *
 * @author MPatashnik
 */
public abstract class Commander extends MovingUnit implements Summoner {

  /** Base level of health for lvl1 commanders */
  static final int BASE_HEALTH = 500;

  /** Base starting level of mana per turn for lvl1 commanders */
  static final int BASE_MANA_PT = 200;

  /** Base starting level of actions per turn for lvl1 commander */
  static final int BASE_ACTIONS_PT = 2;

  /** Prefix for all levelup buffs. */
  public static final String LEVEL_UP_MODIFIER_PREFIX = "Level Up";

  protected static final ModifierBundle LEVELUP =
      new ModifierBundle(
          new StatModifier(
              LEVEL_UP_MODIFIER_PREFIX + " Mana",
              Integer.MAX_VALUE,
              StackMode.STACKABLE,
              StatType.MANA_PER_TURN,
              StatModifier.ModificationType.ADD,
              100),
          new StatModifier(
              LEVEL_UP_MODIFIER_PREFIX + " Actions",
              Integer.MAX_VALUE,
              StackMode.STACKABLE,
              StatType.COMMANDER_ACTIONS_PER_TURN,
              StatModifier.ModificationType.ADD,
              1),
          new StatModifier(
              LEVEL_UP_MODIFIER_PREFIX + " Move",
              Integer.MAX_VALUE,
              StackMode.STACKABLE,
              StatType.MOVEMENT_TOTAL,
              StatModifier.ModificationType.ADD,
              1),
          new StatModifier(
              LEVEL_UP_MODIFIER_PREFIX + " Vision",
              Integer.MAX_VALUE,
              StackMode.STACKABLE,
              StatType.VISION_RANGE,
              StatModifier.ModificationType.ADD,
              1));

  /**
   * The amount of research required to get to the next level for free. Index i = cost to get from
   * level i+1 to i+2 (because levels are 1 indexed).
   */
  private static final int[] RESEARCH_REQS = {200, 600, 1200};

  /** The highest level commanders can achieve */
  private static final int MAX_LEVEL = RESEARCH_REQS.length + 1;

  /** The ratio of level -> research for the owner of the killing model.unit */
  public static final double LEVEL_TO_RESEARCH_RATIO = 50;

  /** The current mana level of this commander. Varies over the course of the model.game. */
  private int mana;

  /**
   * The current level of this player. Starts at 1, increases by 1 every time the player performs a
   * levelup action. Capped at 5
   */
  private int level;

  /**
   * The amount of research towards the next level this commander has accrewed. Always in the range
   * [0, RESEARCH_REQS[level-1]]
   */
  private int research;

  /**
   * Research this commander has gained while it is not his turn Added to research total at start of
   * turn
   */
  private int outOfTurnResearch;

  /**
   * Abilities this commander has picked. each index is -1 before a commander has reached that level
   */
  private int[] abilityChoices;

  /** The abilities the commander has cast this turn */
  private LinkedList<Ability> currentTurnCasts;

  /**
   * Constructor for Commander. Also adds this model.unit to the starting tile for the owner, and
   * its owner as a model.unit that player owns, Commanders have a manaCost of 0.
   *
   * @param owner - the player owner of this model.unit
   * @param imageFilename - the image to draw when drawing this unit.
   * @param stats - the stats of this commander. Notably, because of restrictions on commander, the
   *     attack, counterattack, and attackType Attributes are unused, because they are either
   *     unnecessary or calculated elsewhere.
   * @param startingLevel - the level this commander starts at.
   */
  public Commander(String name, String imageFilename, Player owner, Stats stats, int startingLevel)
      throws RuntimeException, IllegalArgumentException {
    super(owner, name, imageFilename, 0, 0, stats);
    if (startingLevel > MAX_LEVEL) {
      throw new RuntimeException("Can't start at level above max level");
    }
    research = 0;
    currentTurnCasts = new LinkedList<Ability>();
    setHealth(getMaxHealth(), this);
    setMana(0);

    abilityChoices = new int[MAX_LEVEL];
    for (int i = 1; i < MAX_LEVEL; i++) { // leave abilityChoices[0] = 0
      abilityChoices[i] = -1;
    }
    location = owner.game.board.getCommanderStartLocation(owner);
    location.addOccupyingUnit(this);
    owner.addUnit(this);

    // Level up to starting level.
    level = 1;
    for (int i = 1; i < startingLevel; i++) {
      levelUp();
    }
  }

  /** Throws a runtime exception - commanders are not clonable */
  @Override
  protected Unit createClone(Player owner) {
    throw new RuntimeException("Can't clone commander " + this);
  }

  /**
   * Adds clearing currentTurnCasts to super's refresh for turn. Adds out of turn research to
   * standard research total
   */
  @Override
  public void refreshForTurn() {
    super.refreshForTurn();
    addResearch(outOfTurnResearch);
    outOfTurnResearch = 0;
    currentTurnCasts.clear();
  }

  /** Commanders can always summon */
  @Override
  public boolean canSummon() {
    return true;
  }

  /** Commanders can also cast - but only if they have at least one active ability */
  @Override
  public boolean canCast() {
    return !getActiveAbilities().isEmpty();
  }

  /**
   * Returns true if summoning a model.unit is currently ok - checks surrounding area for free space
   */
  public boolean hasSummonSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied()) return true;
    }
    return false;
  }

  /**
   * Returns true if building a model.unit is currently ok - checks surrounding area for free
   * ancient ground
   */
  public boolean hasBuildSpace() {
    ArrayList<Tile> tiles = owner.game.board.getRadialCloud(location, getSummonRange());
    for (Tile t : tiles) {
      if (!t.isOccupied()) return true;
    }
    return false;
  }

  // RESTRICTIONS
  /** Restricted attack - has val 0. */
  @Override
  public int getMinAttack() {
    return 0;
  }

  @Override
  public int getMaxAttack() {
    return 0;
  }

  /** Commanders can't fight */
  public boolean canFight() {
    return false;
  }

  /** Returns Commander */
  @Override
  public String getIdentifierString() {
    return "Commander";
  }

  // HEALTH and MANA
  /** Returns the current mana of this commander */
  public int getMana() {
    return mana;
  }

  /** Sets to the given amount of mana. Input must be non-negative */
  protected void setMana(int newMana) throws IllegalArgumentException {
    if (newMana < 0) throw new IllegalArgumentException("Can't set mana to " + newMana + " - OOB");
    addMana(newMana - mana);
  }

  /**
   * adds the given amount of mana If this would make mana negative, instead sets mana to 0, but
   * returns the amount of mana below 0 this change would put. If the mana is still positive,
   * returns the new value of mana.
   */
  public int addMana(int deltaMana) {
    mana = mana + deltaMana;
    int nMana = mana;
    if (mana < 0) mana = 0;
    return nMana;
  }

  // LEVEL AND RESEARCH
  /**
   * Returns the level of this player. Uses the mutable level field of commander , thus ignores the
   * immutable model.unit level field
   */
  @Override
  public int getLevel() {
    return level;
  }

  /** Returns the current amount of research this commander has accrewed */
  public int getResearch() {
    return research;
  }

  /** Returns the amount of research necessary to get to the next level */
  public int getResearchRequirement() {
    if (level == MAX_LEVEL) return Integer.MAX_VALUE;
    return RESEARCH_REQS[level - 1];
  }

  /** Returns the amount of research still necessary to get to the next level */
  public int getResearchRemaining() {
    return getResearchRequirement() - research;
  }

  /**
   * Adds the amount of research to this commander. If in turn, adds to standard research and may
   * cause levelup. If out of turn, adds to out of turn research and delays levelup until turn.
   */
  public void addResearch(int deltaResearch) throws IllegalArgumentException {
    if (deltaResearch < 0)
      throw new IllegalArgumentException("Can't add negative amount of research to " + this);
    if (owner.isMyTurn()) addInTurnResearch(deltaResearch);
    else addOutOfTurnResearch(deltaResearch);
  }

  /**
   * Adds the given amount of research to research, capping at the requirement. Called when reserach
   * is gained during this commander's turn Input must be positive (you can only gain research)
   */
  private void addInTurnResearch(int deltaResearch) {
    research = Math.min(research + deltaResearch, getResearchRequirement());
    if (research == getResearchRequirement()) levelUp();
  }

  /** Adds the amount of out of turn research. Called when it is not this commander's turn */
  private void addOutOfTurnResearch(int deltaResearch) {
    outOfTurnResearch += deltaResearch;
  }

  /**
   * Called by Player class when this levels up. Can be overriden by subclass to cause affect when
   * leveled up, but this super method should be called first. resets research, recalculates
   * manaPerTurn and health of commander, updates stats
   */
  private void levelUp() {
    research = 0;
    level++;
    LEVELUP.clone(this, this);
    owner.updateManaPerTurn();
    owner.refreshVisionCloud();
    owner.game.getController().startNewAbilityDecision(owner);
  }

  /** Chooses ability index i for this level. Called when the player ends the levelup decision */
  public void chooseAbility(int i) throws RuntimeException {
    if (abilityChoices[level - 1] != -1)
      throw new RuntimeException("Can't pick ability for level " + level + " already picked " + i);
    abilityChoices[level - 1] = i;
  }

  /** Commanders can't attack, so attack modifications aren't ok. */
  @Override
  public boolean modifierOk(Modifier m) {
    if (m instanceof StatModifier) {
      StatType s = ((StatModifier) m).modifiedStat;
      return !s.isAttackStat();
    }
    if (m instanceof CustomModifier) {
      return ((CustomModifier) m).appliesToCommanders;
    }
    return false;
  }

  // SUMMONING
  /** Returns a dummy model.unit for the given name, if possible (otherwise null ) */
  public Unit getUnitByName(String name) {
    Map<String, Combatant> summonables = getSummonables();
    Unit toSummon = summonables.get(name);
    if (toSummon == null) toSummon = getBuildables().get(name);
    return toSummon;
  }

  /**
   * Returns the list of units this can summon. Can be overridden to add additional units. Base
   * Return is FileUnits for this' level.
   */
  public Map<String, Combatant> getSummonables() {
    HashMap<String, Combatant> units = new HashMap<String, Combatant>();
    for (int i = 1; i <= getLevel(); i++) {
      for (FileCombatant c : FileCombatant.getCombatantsForAge(i)) {
        units.put(c.name, c);
      }
    }
    return units;
  }

  /** Returns the list of buildings this can build. Can be overriden to add additional buildings */
  public Map<String, Building> getBuildables() {
    HashMap<String, Building> units = new HashMap<String, Building>();
    for (int i = 0; i <= getLevel(); i++) {
      for (Building b : Buildings.getBuildingsForLevel(i)) {
        units.put(b.name, b);
      }
    }
    return units;
  }

  // ABILITIES
  /**
   * Returns the possible abilities for the given level. Note that the incoming levels are 1
   * indexed, so keep that in mind
   */
  public abstract Ability[] getPossibleAbilities(int level);

  /**
   * Returns the ability chosen for the given level. This is an ability the commander actually has
   * access to. Will return null if the input is greater than the current level
   */
  public Ability getAbility(int level) {
    try {
      return getPossibleAbilities(level)[abilityChoices[level - 1]];
    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /** Returns all abilities this Commander currently has access to. */
  public LinkedList<Ability> getAbilities() {
    LinkedList<Ability> abilities = new LinkedList<Ability>();
    for (int i = 1; i <= getLevel(); i++) {
      Ability a = getAbility(i);
      if (a != null) abilities.add(a);
    }
    return abilities;
  }

  /** Returns all active (castable) abilities this Commander currently has access to. */
  public LinkedList<Ability> getActiveAbilities() {
    LinkedList<Ability> abilities = new LinkedList<Ability>();
    for (int i = 1; i <= getLevel(); i++) {
      Ability a = getAbility(i);
      if (a != null && !a.isPassive()) abilities.add(a);
    }
    return abilities;
  }

  /** Returns all passive (always on) abilities this Commander currently has access to. */
  public LinkedList<Ability> getPassiveAbilities() {
    LinkedList<Ability> abilities = new LinkedList<Ability>();
    for (int i = 1; i <= getLevel(); i++) {
      Ability a = getAbility(i);
      if (a != null && a.isPassive()) abilities.add(a);
    }
    return abilities;
  }

  /** Returns the abilities this commander has cast this turn */
  public LinkedList<Ability> getAbilitiesCastThisTurn() {
    return new LinkedList<Ability>(currentTurnCasts);
  }

  /** Returns the ability associated with the given name for this commander, if possible */
  public Ability getAbilityByName(String name) {
    for (int i = 1; i <= MAX_LEVEL; i++) {
      for (Ability a : getPossibleAbilities(i)) {
        if (a.name.equals(name)) return a;
      }
    }
    return null;
  }

  @Override
  public String toStringFull() {
    String s = super.toStringFull();
    s += " Mana =" + getMana();
    s += " Level =" + getLevel() + " and " + getResearch() + "/" + getResearchRequirement() + " ";
    for (Ability a : getAbilities()) {
      s += a.toStringShort() + " ";
    }
    return s;
  }
}
