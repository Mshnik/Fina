package model.unit.commander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import controller.decision.Decision;
import model.board.Tile;
import model.game.Player;
import model.unit.MovingUnit;
import model.unit.Summoner;
import model.unit.Unit;
import model.unit.ability.Abilities;
import model.unit.ability.Ability;
import model.unit.building.Building;
import model.unit.building.Buildings;
import model.unit.combatant.Combatant;
import model.unit.combatant.Combatants;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
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

  /**
   * Prefix for all levelup buffs.
   */
  static final String LEVEL_UP_MODIFIER_PREFIX = "Level Up";

  /**
   * The highest level commanders can achieve
   */
  public static final int MAX_LEVEL = 4;

  /**
   * The bonus ratio of damage -> research for damaging a commander.
   */
  public static final double BONUS_DAMAGE_TO_RESEARCH_RATIO = 1.25;

  /**
   * The ratio of level -> research for the owner of the killing model.unit
   */
  public static final double LEVEL_TO_RESEARCH_RATIO = 50;

  /**
   * The current mana level of this commander. Varies over the course of the model.game.
   */
  private int mana;

  /**
   * The current level of this player. Starts at 1, increases by 1 every time the player performs a
   * levelup action. Capped at 4.
   */
  private int level;

  /**
   * The amount of research towards the next level this commander has accrued. Always in the range
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
  private final Ability[] abilityChoices;

  /**
   * The abilities the commander has cast this turn
   */
  private final LinkedList<Ability> currentTurnCasts;

  /**
   * Constructor for Commander. Also adds this model.unit to the starting tile for the owner, and
   * its owner as a model.unit that player owns, Commanders have a manaCost of 0.
   *
   * @param owner         - the player owner of this model.unit
   * @param imageFilename - the image to draw when drawing this unit.
   * @param stats         - the stats of this commander. Notably, because of restrictions on commander, the
   *                      attack, counterattack, and attackType Attributes are unused, because they are either
   *                      unnecessary or calculated elsewhere.
   */
  public Commander(Player owner, Tile location, String name, String imageFilename, Stats stats)
      throws RuntimeException {
    super(owner, name, imageFilename, 0, 0, stats);
    research = 0;
    level = 1;
    currentTurnCasts = new LinkedList<>();
    setHealth(getMaxHealth(), this);
    setMana(0);
    this.location = location;

    abilityChoices = new Ability[MAX_LEVEL];
  }

  /**
   * Forces implementing methods to return a commander.
   */
  @Override
  protected abstract Commander createClone(Player p, Tile cloneLocation);

  /**
   * Creates a clone of this Commander for the commander for the given player, at the given starting
   * level.
   */
  public void createForPlayer(Player p, int startingLevel) {
    Commander commander = createClone(p, p.game.board.getCommanderStartLocation(p));
    commander.getLocation().addOccupyingUnit(commander);
    p.addUnit(commander);
    p.refreshVisionCloud(commander);
    for (int i = 1; i < startingLevel; i++) {
      commander.research += getResearchRequirements()[i - 1];
    }
  }

  /**
   * Adds clearing currentTurnCasts to super's refresh for turn. Adds out of turn research to
   * standard research total
   */
  @Override
  public void refreshForTurn() {
    super.refreshForTurn();
    currentTurnCasts.clear();
  }

  /**
   * Checks for level up, and levels up if need be (may be multiple times if that acutally happens).
   */
  public void ingestResearchAndCheckLevelUp() {
    addResearch(outOfTurnResearch);
    outOfTurnResearch = 0;
    while (research >= getResearchRequirement() && level < MAX_LEVEL) {
      levelUp();
    }
  }

  /**
   * Commanders can summon if it has an action remaining.
   */
  @Override
  public boolean canSummon() {
    return getActionsRemaining() > 0;
  }

  /**
   * Commanders can cast if it has an action remaining.
   */
  @Override
  public boolean canCast() {
    return getActionsRemaining() > 0;
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

  /**
   * Restricted attack - has val 0.
   */
  @Override
  public int getMinAttack() {
    return 0;
  }

  @Override
  public int getMaxAttack() {
    return 0;
  }

  @Override
  public int getMinAttackRange() {
    return 0;
  }

  @Override
  public int getMaxAttackRange() {
    return 0;
  }

  /**
   * Commanders can't fight
   */
  public boolean canFight() {
    return false;
  }

  /**
   * Returns Commander
   */
  @Override
  public String getIdentifierString() {
    return "Commander";
  }

  // HEALTH and MANA

  /**
   * Returns the current mana of this commander
   */
  public int getMana() {
    return mana;
  }

  /**
   * Sets to the given amount of mana. Input must be non-negative
   */
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

  /**
   * Returns the current amount of research this commander has accrewed
   */
  public int getResearch() {
    return research;
  }

  /**
   * Returns the array of research requirements for advancing from each level to the next. Because
   * levels are 1 indexed, requirement[i] is the requirement to get to level i+2. Requirement[MAX_LEVEL-1]
   * is the amount of research needed to use this commander's ultimate power.
   */
  abstract int[] getResearchRequirements();

  /**
   * Returns the amount of research necessary to get to the next level or use an ultimate power, if at max level.
   */
  public int getResearchRequirement() {
    return getResearchRequirements()[level - 1];
  }

  /**
   * Returns the amount of research still necessary to get to the next level
   */
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
   * Adds the given amount of research to research. Called when research is gained during this
   * commander's turn Input must be positive (you can only gain research)
   */
  private void addInTurnResearch(int deltaResearch) {
    research += deltaResearch;
  }

  /**
   * Adds the amount of out of turn research. Called when it is not this commander's turn
   */
  private void addOutOfTurnResearch(int deltaResearch) {
    outOfTurnResearch += deltaResearch;
  }

  /**
   * Returns the bundle of stat boosts to give the commander when it levels up to level {@code level}.
   */
  abstract ModifierBundle getLevelupModifierBundle(int level);

  /**
   * Called by Player class when this levels up. Can be overriden by subclass to cause affect when
   * leveled up, but this super method should be called first. resets research, recalculates
   * manaPerTurn and health of commander, updates stats
   */
  private void levelUp() {
    if (level < MAX_LEVEL) {
      research -= getResearchRequirement();
      level++;
      getLevelupModifierBundle(level).clone(this, this);
      owner.updateManaPerTurn();
      owner.refreshVisionCloud(this);
      if (owner.game.getController().hasDecision() && owner.game.getController().getDecisionType() != Decision.DecisionType.NEW_ABILITY_DECISION) {
        owner.game.getController().startNewAbilityDecision(owner);
      }
    }
  }

  /**
   * Returns all abilites this commander could have. Only some of these will be active, by level and levelup decision.
   */
  public abstract Ability[][] allAbilities();

  /**
   * Returns the abilities this commander currently has.
   */
  public List<Ability> getAbilities() {
    return Arrays.stream(abilityChoices).filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * Returns the level with an ability choice needed. Returns -1 if no choices are needed.
   */
  public int getAbilityChoiceNeededLevel() {
    for (int i = 1; i <= level; i++) {
      if (abilityChoices[i - 1] == null) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Returns the possible abilities to choose between for {@code level}.
   */
  public Ability[] getAbilityChoices(int level) {
    return allAbilities()[level - 1];
  }

  /**
   * Chooses ability index i for the given level. Called when the player ends the levelup decision
   */
  public void chooseAbility(int level, int i) throws RuntimeException {
    if (level > this.level) {
      throw new RuntimeException("Cannot make a choice for level " + level + ", this is only level " + this.level);
    } else if (abilityChoices[level - 1] != null) {
      throw new RuntimeException("Can't pick ability for level " + level + " already picked " + i);
    }
    abilityChoices[level - 1] = allAbilities()[level - 1][i];
  }

  /**
   * Returns the set of modifiers the player can see in the UI. Commander levelup modifiers should
   * be filtered out.
   */
  public List<Modifier> getVisibleModifiers() {
    return getModifiers()
        .stream()
        .filter(m -> !m.name.contains(LEVEL_UP_MODIFIER_PREFIX))
        .collect(Collectors.toList());
  }

  /**
   * Commanders can't attack, so attack modifications aren't ok.
   */
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

  /**
   * Returns a dummy model.unit for the given name, if possible (otherwise null )
   */
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
  @Override
  public Map<String, Combatant> getSummonables() {
    HashMap<String, Combatant> units = new HashMap<String, Combatant>();
    for (int i = 1; i <= getLevel(); i++) {
      for (Combatant c : Combatants.getCombatantsForAge(i)) {
        units.put(c.name, c);
      }
    }
    return units;
  }

  /**
   * Returns the list of buildings this can build. Can be overriden to add additional buildings
   */
  @Override
  public Map<String, Building> getBuildables() {
    HashMap<String, Building> units = new HashMap<String, Building>();
    for (int i = 0; i <= getLevel(); i++) {
      for (Building b : Buildings.getBuildingsForLevel(i)) {
        units.put(b.name, b);
      }
    }
    return units;
  }

  /**
   * Returns a map of actions this can cast.
   */
  public Map<String, Ability> getCastables() {
    HashMap<String, Ability> units = new HashMap<String, Ability>();
    for (int i = 0; i <= getLevel(); i++) {
      for (Ability a : Abilities.getAbilitiesForAge(i)) {
        units.put(a.name, a);
      }
    }

    for (Ability ability : getAbilities()) {
      units.put(ability.name, ability);
    }

    return units;
  }

  @Override
  public void preMove(List<Tile> path) {
  }

  @Override
  public void postMove(List<Tile> path) {
    owner.maybeRemoveActionableUnit(this);
  }

  @Override
  public void preCounterFight(Combatant other) {
  }

  @Override
  public void postCounterFight(int damageDealt, Combatant other, int damageTaken) {
  }

  @Override
  public String toStringFull() {
    String s = super.toStringFull();
    s += " Mana =" + getMana();
    s += " Level =" + getLevel() + " and " + getResearch() + "/" + getResearchRequirement() + " ";
    return s;
  }
}
