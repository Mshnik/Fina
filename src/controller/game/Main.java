package controller.game;

import controller.audio.AudioController;
import model.game.Game.FogOfWar;
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
    AudioController.setMute(true);

    // Select initial board file and make start game.
    String boardFilename = args.length > 0 ? args[0] : "Darkwood.csv";
    GameController.loadAndStart("game/boards/" + boardFilename, 2, FogOfWar.REGULAR);
  }
}
