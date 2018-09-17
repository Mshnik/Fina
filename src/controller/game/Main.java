package controller.game;

import static ai.dummy.FullRandomAIController.FULL_RANDOM_AI_TYPE;
import static model.game.HumanPlayer.HUMAN_PLAYER_TYPE;

import java.util.ArrayList;
import java.util.List;
import model.game.Game.FogOfWar;
import model.unit.ability.Abilities;
import model.unit.building.Buildings;
import model.unit.combatant.Combatants;

public final class Main {
  /** Simple main method to test out Frame features */
  public static void main(String[] args) throws InterruptedException {
    Thread.sleep(0);
    // Force unit, building, spell, audio loading.
    Combatants.getCombatantsForAge(1);
    Buildings.getBuildingsForLevel(1);
    Abilities.getAbilitiesForAge(1);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Expanse.csv";
    List<String> defaultPlayerTypes = new ArrayList<>();
    defaultPlayerTypes.add(FULL_RANDOM_AI_TYPE);
    defaultPlayerTypes.add(FULL_RANDOM_AI_TYPE);
    GameController.loadAndStart(
        "game/boards/" + boardFilename, defaultPlayerTypes, FogOfWar.REGULAR, 1, 9, 18, 3);
  }
}
