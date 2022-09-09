package model.unit.commander;

import model.board.Tile;
import model.game.Player;
import model.unit.ability.Abilities;
import model.unit.ability.Ability;
import model.unit.modifier.Modifier.StackMode;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.StatModifier;
import model.unit.stat.Stat;
import model.unit.stat.StatType;
import model.unit.stat.Stats;

public final class DummyCommander extends Commander {

  /**
   * Stats for maxHealth, range and visionRange of dummy commander
   */
  private static final Stats STATS =
      new Stats(
          new Stat(StatType.MAX_HEALTH, 250),
          new Stat(StatType.MANA_PER_TURN, 200),
          new Stat(StatType.ACTIONS_PER_TURN, 2),
          new Stat(StatType.SUMMON_RANGE, 1),
          new Stat(StatType.VISION_RANGE, 3),
          new Stat(StatType.MOVEMENT_TOTAL, 3),
          new Stat(StatType.GRASS_COST, 1),
          new Stat(StatType.WOODS_COST, 2),
          new Stat(StatType.MOUNTAIN_COST, 9999),
          new Stat(StatType.SEA_COST, 9999));

  /**
   * Levelup stats for DummyCommander for levels 2.
   */
  private static final ModifierBundle LEVELUP_2 =
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
              StatType.ACTIONS_PER_TURN,
              StatModifier.ModificationType.ADD,
              1));

  /**
   * Levelup stats for DummyCommander for levels 3.
   */
  private static final ModifierBundle LEVELUP_3 =
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
              StatType.ACTIONS_PER_TURN,
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
   * Levelup stats for DummyCommander for levels 4.
   */
  private static final ModifierBundle LEVELUP_4 =
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
              StatType.ACTIONS_PER_TURN,
              StatModifier.ModificationType.ADD,
              1));

  /**
   * The amount of research required to get to the next level. Index i = cost to get from level i+1
   * to i+2 (because levels are 1 indexed).
   */
  private static final int[] RESEARCH_REQS = {50, 200, 10000, 5000};

  /**
   * Abilities this commander can have, by level. Abilities after level 1 may have choices.
   */
  private static final Ability[][] ABILITIES = new Ability[][]{
      {
          Abilities.getAbilityForName("Channel"),
      },
      {
          Abilities.getAbilityForName("Heal"),
          Abilities.getAbilityForName("Strengthen")
      },
      {
          Abilities.getAbilityForName("Heal"),
          Abilities.getAbilityForName("Strengthen")
      },
      {
          Abilities.getAbilityForName("Heal"),
          Abilities.getAbilityForName("Strengthen")
      }
  };

  public DummyCommander() {
    this(null, null);
  }

  private DummyCommander(Player owner, Tile location) {
    super(owner, location, "Dummy", "mario.png", STATS);
  }

  @Override
  protected Commander createClone(Player owner, Tile cloneLocation) {
    if (this.owner != null) {
      throw new RuntimeException(
          "Shouldn't make a clone of an already on the board commander " + this);
    }
    return new DummyCommander(owner, cloneLocation);
  }

  @Override
  int[] getResearchRequirements() {
    return RESEARCH_REQS;
  }

  @Override
  ModifierBundle getLevelupModifierBundle(int level) {
    switch (level) {
      case 2:
        return LEVELUP_2;
      case 3:
        return LEVELUP_3;
      case 4:
        return LEVELUP_4;
      default:
        throw new UnsupportedOperationException("Unsupported level up: " + level);
    }
  }

  @Override
  public Ability[][] allAbilities() {
    return ABILITIES;
  }
}
