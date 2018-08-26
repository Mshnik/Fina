package controller.audio;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.game.Player;
import model.unit.commander.Commander;
import model.unit.commander.DummyCommander;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing audio, including both repeating music and sound effects.
 *
 * @author Mshnik
 */
public final class AudioController {

  /** Valid sound effects. */
  public enum SoundEffect {
    CLICK_YES("click_yes.wav"),
    CLICK_NO("click_no.wav"),
    CURSOR_MOVE("cursor_move.wav");

    private final String filepath;

    private SoundEffect(String filename) {
      filepath = "sound/effects/" + filename;
    }
  }

  /** Valid music. Can be menu or commander. */
  public enum Music {
    DUMMY_COMMANDER_THEME("jess_theme.mp3");

    private final String filepath;

    private Music(String filename) {
      filepath = "sound/music/" + filename;
    }
  }

  private static final Map<Class<? extends Commander>, Music> MUSIC_MAP = new HashMap<>();

  /* Set up music map. */
  static {
    MUSIC_MAP.put(DummyCommander.class, Music.DUMMY_COMMANDER_THEME);

    // Start up a JavaFX room for sound, don't show anything.
    // this will prepare JavaFX toolkit and environment so we can use media.
    SwingUtilities.invokeLater(JFXPanel::new);
  }

  private static boolean MUTE;
  private static MediaPlayer mediaPlayer;

  /** Plays the given sound effect. */
  public static void playEffect(SoundEffect effect) {
    if (!MUTE) {
      try {
        Clip clip = AudioSystem.getClip();
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(effect.filepath));
        clip.open(ais);
        clip.loop(0);
      } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** Plays the given music. */
  private static void playMusic(Music music) {
    if (!MUTE) {
      if (mediaPlayer != null) {
        mediaPlayer.stop();
      }
      mediaPlayer = new MediaPlayer(new Media(new File(music.filepath).toURI().toString()));
      mediaPlayer.setVolume(0.25);
      mediaPlayer.setCycleCount(Integer.MAX_VALUE);
      mediaPlayer.play();
    }
  }

  /** Plays the music for the given commander. */
  public static void playMusicForTurn(Player player) {
    playMusic(
        MUSIC_MAP.getOrDefault(player.getCommander().getClass(), Music.DUMMY_COMMANDER_THEME));
  }

  /** Sets the mute setting and stops any currently playing music. */
  public static void setMute(boolean mute) {
    if (mediaPlayer != null && mute) {
      mediaPlayer.pause();
    } else if (mediaPlayer != null) {
      mediaPlayer.play();
    }
    MUTE = mute;
  }
}
