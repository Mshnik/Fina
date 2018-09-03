package model.unit.ability;

import model.unit.MovingUnit;
import model.unit.Unit;
import model.unit.combatant.Combatant;
import model.unit.commander.Commander;
import model.unit.modifier.CustomModifier;
import model.unit.modifier.Modifier;
import model.unit.modifier.ModifierBundle;
import model.unit.modifier.Modifiers;

import java.util.Random;
import java.util.stream.Collectors;

/** An effect applied to a unit as part of an ability. */
public final class AbilityEffect {

  /** Minimum damage to deal if this is an effect that deals damage, null otherwise. */
  private final int minDamage;

  /** Minimum damage to deal if this is an effect that deals damage, null otherwise. */
  private final int maxDamage;

  /** HP to heal if this is an effect that heals constant hp, 0 otherwise. */
  private final int healConstantHp;

  /** HP to heal if this is an effect that heals a percentage of max HP, 0 otherwise. */
  private final double healPercentageOfMaxHp;

  /** True if this effect destroys a unit without dealing damage. */
  private final boolean destroyUnit;

  /** True if this effect refreshes movement and attack of a unit. */
  private final boolean refreshUnit;

  /** True if this effect gains research for the targeted commander. */
  private final int research;

  /** Number of turns all modifiers granted through abilities last. */
  private static final int MODIFIER_TURN_DURATION = 3;

  /** ModifierBundle if this is an effect that gives a modifier, null otherwise. */
  private final ModifierBundle modifierEffect;

  /** Constructor for AbilityEffect - expects at most one type of effect to be set. */
  private AbilityEffect(
      int minDamage,
      int maxDamage,
      int healConstantHp,
      double healPercentageOfMaxHp,
      boolean destroyUnit,
      boolean refreshUnit,
      int research,
      ModifierBundle modifierEffect) {
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.healConstantHp = healConstantHp;
    this.healPercentageOfMaxHp = healPercentageOfMaxHp;
    this.destroyUnit = destroyUnit;
    this.refreshUnit = refreshUnit;
    this.research = research;
    this.modifierEffect = modifierEffect;
  }

  /** Creates an ability effect that deals damage. */
  static AbilityEffect damage(int minDamage, int maxDamage) {
    return new AbilityEffect(minDamage, maxDamage, 0, 0, false, false, 0, null);
  }

  /** Creates an ability effect that heals constant hp. */
  static AbilityEffect healConstantHp(int healConstantHp) {
    return new AbilityEffect(0, 0, healConstantHp, 0, false, false, 0, null);
  }

  /** Creates an ability effect that heals a percentage of max hp. */
  static AbilityEffect healPercentageOfMaxHp(double percentageOfMaxHp) {
    return new AbilityEffect(0, 0, 0, percentageOfMaxHp, false, false, 0, null);
  }

  /** Creates an ability effect that destroys a unit. */
  static AbilityEffect destroyUnit() {
    return new AbilityEffect(0, 0, 0, 0, true, false, 0, null);
  }

  /** Creates an ability effect that refreshes a unit. */
  static AbilityEffect refreshUnit() {
    return new AbilityEffect(0, 0, 0, 0, false, true, 0, null);
  }

  /** Creates an ability effect that gains the commander research. */
  static AbilityEffect research(int research) {
    return new AbilityEffect(0, 0, 0, 0, false, false, research, null);
  }

  /**
   * Creates an ability effect that grants a set of modifiers. Modifiers are set to have the correct
   * duration and stack mode.
   */
  static AbilityEffect modifierBundle(Modifier... modifiers) {
    return modifierBundle(new ModifierBundle(modifiers));
  }

  /**
   * Creates an ability effect that grants a modifier bundle. Modifiers are set to have the correct
   * duration and stack mode.
   */
  static AbilityEffect modifierBundle(ModifierBundle modifiers) {
    return new AbilityEffect(
        0,
        0,
        0,
        0,
        false,
        false,
        0,
        new ModifierBundle(
            modifiers
                .stream()
                .map(m -> m.uniqueCopy(MODIFIER_TURN_DURATION, Modifier.StackMode.DURATION_ADD))
                .collect(Collectors.toList())));
  }

  /** Makes this abilityEffect affect the given unit. */
  void affect(Unit u, Commander caster, Random random) {
    if (maxDamage != 0) {
      damageEffect(u, caster, random);
    } else if (healConstantHp != 0) {
      u.changeHealth(healConstantHp, caster);
    } else if (healPercentageOfMaxHp != 0) {
      u.changeHealth((int) (healPercentageOfMaxHp * u.getMaxHealth()), caster);
    } else if (destroyUnit) {
      u.died(caster);
    } else if (refreshUnit) {
      if (u instanceof MovingUnit) {
        ((MovingUnit) u).refreshMovement();
      }
      if (u instanceof Combatant) {
        ((Combatant) u).setCanFight(true);
      }
    } else if (research > 0) {
      caster.addResearch(research);
    } else if (modifierEffect != null) {
      modifierEffect.clone(u, caster);
    } else {
      throw new RuntimeException("This effect didn't match any known effect case");
    }
  }

  private void damageEffect(Unit u, Commander caster, Random random) {
    int damage = random.nextInt(maxDamage - minDamage + 1) + minDamage;

    // Subtract ratio from hexproof.
    damage *=
        Math.max(
            0,
            1
                - u.getModifiersByName(Modifiers.hexproof(0))
                    .stream()
                    .mapToDouble(m -> ((CustomModifier) m).val.doubleValue())
                    .sum());

    // Subtract constant from toughness.
    damage -=
        u.getModifiersByName(Modifiers.toughness(0))
            .stream()
            .mapToInt(m -> ((CustomModifier) m).val.intValue())
            .sum();

    // Make sure damage isn't negative.
    damage = Math.max(0, damage);

    // Apply damage.
    System.out.println("Spell damage: " + damage);
    u.changeHealth(-damage, caster);
  }
}
