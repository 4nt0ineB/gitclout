package fr.uge.gitclout.analyzer;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

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
  
  public static void deleteDirectory(File dir) {
    Deque<File> stack = new ArrayDeque<>();
    stack.push(dir);
    
    while (!stack.isEmpty()) {
      File current = stack.pop();
      File[] files = current.listFiles();
      
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            stack.push(file);
          } else {
            file.delete();
          }
        }
      }
      current.delete();
    }
  }
}

