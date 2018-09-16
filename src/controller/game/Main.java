package controller.game;

import static ai.dummy.FullRandomAIController.FULL_RANDOM_AI_TYPE;

import java.util.ArrayList;
import java.util.List;
import model.game.Game.FogOfWar;
import model.game.HumanPlayer;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) {
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Darkwood.csv";
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(FULL_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(FULL_RANDOM_AI_TYPE);
    GameController.loadAndStart(
        "game/boards/" + boardFilename, defaultPlayerTypes, FogOfWar.REGULAR, 1, -1, 10, 3);
  }
}
