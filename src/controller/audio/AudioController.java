package controller.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingUtilities;
import model.game.Player;
import model.unit.commander.Commander;
import model.unit.commander.DummyCommander;

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

    /** The filepath to the sound effect file. */
    private final String filePath;

    /**
     * The sound clip for this SoundEffect. Multiple clips are maintained so the sound effect can
     * play multiple times concurrently.
     */
    private final List<Clip> clips;

    /** Constructs a new sound effect from the given filename. */
    private SoundEffect(String filename) {
      clips = new ArrayList<>();
      filePath = "sound/effects/" + filename;
    }

    /** Reads this sound effect into a new clip and adds it into clips. */
    private Clip readIntoNewClip() {
      try {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clips.add(clip);
        return clip;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Plays this sound effect once. If forceReplay is true, restarts the clip if it is currently
     * playing. Otherwise, doesn't play the clip on this call.
     */
    private void play() {
      Optional<Clip> freeClip = clips.stream().filter(c -> !c.isActive()).findAny();
      Clip clip;
      if (freeClip.isPresent()) {
        clip = freeClip.get();
      } else {
        clip = readIntoNewClip();
        System.out.println("Created new clip");
      }
      clip.setFramePosition(0);
      clip.start();
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
      effect.play();
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
    //    playMusic(
    //        MUSIC_MAP.getOrDefault(player.getCommander().getClass(),
    // Music.DUMMY_COMMANDER_THEME));
  }

  /** Stops music. */
  public static void stopMusic() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
    }
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
