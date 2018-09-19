package controller.game;

import model.game.Game.FogOfWar;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

import java.util.ArrayList;
import java.util.List;

import static ai.delegating.DelegatingAIControllers.DELEGATING_RANDOM_AI_TYPE;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) throws InterruptedException {
    Thread.sleep(0);
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Backyard.csv";
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(DELEGATING_RANDOM_AI_TYPE);
    GameController.loadAndStart(
        "game/boards/" + boardFilename, defaultPlayerTypes, FogOfWar.REGULAR, 1, 9, 18, 3);
  }
}
