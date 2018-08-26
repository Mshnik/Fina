package controller.audio;

import jaco.mp3.player.MP3Player;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Controller for managing audio, including both repeating music and sound effects.
 *
 * @author Mshnik
 */
public final class AudioController {

  public enum SoundEffect {
    CLICK_YES("click_yes.wav"),
    CLICK_NO("click_no.wav"),
    CURSOR_MOVE("cursor_move.wav");

    private final String filepath;

    private SoundEffect(String filename) {
      filepath = "sound/effects/" + filename;
    }
  }

  public static boolean MUTE = false;
  private static MP3Player mp3Player;

  public static void playEffect(SoundEffect effect) {
    if (!MUTE) {
      new Thread(() -> playEffectHelper(effect.filepath)).start();
    }
  }

  public static void playMusic(String filepath) {
    if (!MUTE) {
      if (mp3Player != null) {
        mp3Player.stop();
      }
      mp3Player = new MP3Player(new File(filepath));
      mp3Player.play();
    }
  }

  /**
   * Plays the given sound effect. Should be called in a separate thread to not clog processing
   * logic. Logic taken from http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
   */
  private static void playEffectHelper(String filename) {
    File soundFile = new File(filename);
    if (!soundFile.exists()) {
      System.err.println("Wave file not found: " + filename);
      return;
    }
    AudioInputStream audioInputStream = null;
    try {
      audioInputStream = AudioSystem.getAudioInputStream(new File(filename));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    AudioFormat format = audioInputStream.getFormat();
    SourceDataLine auline = null;
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

    try {
      auline = (SourceDataLine) AudioSystem.getLine(info);
      auline.open(format);
    } catch (LineUnavailableException e) {
      throw new RuntimeException(e);
    }

    auline.start();
    int nBytesRead = 0;
    byte[] abData = new byte[524288]; // 128Kb

    try {
      while (nBytesRead != -1) {
        nBytesRead = audioInputStream.read(abData, 0, abData.length);
        if (nBytesRead >= 0) auline.write(abData, 0, nBytesRead);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      auline.drain();
      auline.close();
    }
  }
}
