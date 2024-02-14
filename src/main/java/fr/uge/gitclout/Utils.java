package fr.uge.gitclout;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Utils {
  
  private Utils(){}
  
  /**
   * Make a string of the content from the given file
   * @param path Path to the init file for the database
   * @return The content of the file
   * @throws IllegalStateException if the path for the given path was not found
   * @throws IOException If fails to read the file
   */
  public static String fileToString(String path) throws IOException {
    try (var input = Utils.class.getResourceAsStream(path)) {
      if(input == null){
        throw new IllegalStateException("file \""+ path + "\" not found");
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}

