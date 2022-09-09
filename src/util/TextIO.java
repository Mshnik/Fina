package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class enables reading and writing text from/to a file.
 *
 * @author MPatashnik
 */
public final class TextIO {

  /**
   * Constructor: It prevents instantiation of TextIO
   */
  private TextIO() {
  }

  /**
   * Write s to text file f.
   *
   * @param f - a file path to write to
   * @param s - text to write
   * @throws IOException
   */
  public static void write(String f, String s) throws IOException {
    write(new File(f), s);
  }

  /**
   * Write s to text file f.
   *
   * @param f - a file to write to
   * @param s - text to write to the file f.
   * @throws IOException
   */
  public static void write(File f, String s) throws IOException {
    FileWriter fr = new FileWriter(f);
    BufferedWriter br = new BufferedWriter(fr);

    br.write(s);

    br.flush();
    br.close();
  }

  /**
   * Read text file f and return it as a String.
   *
   * @param f - the File to read
   * @return - a String of the contents of file f, with newlines as necessary.
   * @throws IOException - if the file reading goes bad.
   */
  public static String read(File f) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      fr = new FileReader(f);
      br = new BufferedReader(fr);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    StringBuilder stringBuilder = new StringBuilder();
    String line = null;
    do {
      line = br.readLine();
      if (line != null) {
        stringBuilder.append('\n');
        stringBuilder.append(line);
      }
    } while (line != null);

    br.close();
    return stringBuilder.substring(1); // Cut off preceding newline character
  }

  /**
   * Read text file f and return it with one line in each list element.
   *
   * @param f - the File to read
   * @return - a String array of the contents of file f, each entry of which is a line
   * @throws IOException - if the file reading goes bad.
   */
  public static List<String> readToArray(File f) throws IOException {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      fr = new FileReader(f);
      br = new BufferedReader(fr);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    List<String> lines = new ArrayList<>();
    String line = null;
    do {
      line = br.readLine();
      if (line != null) {
        lines.add(line);
      }
    } while (line != null);

    br.close();
    return lines;
  }
}
