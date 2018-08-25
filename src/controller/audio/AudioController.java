package controller.audio;

import jaco.mp3.player.MP3Player;

import java.io.File;

/**
 * Controller for managing audio, including both repeating music and sound effects.
 *
 * @author Mshnik
 */
public final class AudioController {

  private static MP3Player mp3Player;

  public static void play(String filepath) {
    if (mp3Player != null) {
      mp3Player.stop();
    }
    mp3Player = new MP3Player(new File(filepath));
    mp3Player.play();
  }
}
