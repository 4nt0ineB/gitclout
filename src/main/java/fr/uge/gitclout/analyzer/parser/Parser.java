package fr.uge.gitclout.analyzer.parser;

import fr.uge.gitclout.model.FileType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Parser {
  
  /**
   * Parses the InputStream using the specified FileType to extract information based on patterns.
   *
   * @param inputStream The input stream to parse.
   * @param fileType    The FileType specifying the file format and extraction rules.
   * @return A HashMap containing line numbers and extracted information based on the FileType rules.
   * @throws IOException If an I/O error occurs while reading the InputStream.
   */
  public HashMap<Integer, String> parseInputStreamWith(InputStream inputStream, FileType fileType) throws IOException {
    var extractions = new HashMap<Integer, String>();
    try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
      var filetype = fileType.name();
      Pattern combinedPattern = fileType.regexPattern();
      var existingExtractions = fileType.extractionScopes();
      var lineNum = -1;
      var startLine = 0;
      String multilineExtractionEnd = null;
      String line;
      while ((line = reader.readLine()) != null) {
        lineNum++;
        if (line.isBlank()) {
          continue;
        }
        var matcher = combinedPattern.matcher(line);
        if (!existingExtractions.isEmpty() && matcher.find()) {
          if(multilineExtractionEnd == null) { // we are not in a multiple line extraction
            var groupLabel = existingExtractions.entrySet().stream()
                                                .filter(e -> {
                                                  var match = matcher.group(e.getKey());
                                                  return match != null && !match.isBlank();
                                                }).findAny();
            if(groupLabel.isEmpty()) {
              continue;
            }
            var group = groupLabel.orElseThrow();
            var extraction = group.getValue();
            if(extraction.inline()) {
              extractions.put(lineNum, extraction.category());
              multilineExtractionEnd = null;
            } else {
              multilineExtractionEnd = group.getKey();
              startLine = lineNum;
              combinedPattern = fileType.regexPatternMultilineEnd();
            }
          } else {
            if(matcher.group(multilineExtractionEnd) == null){
              continue;
            }
            var category = existingExtractions.get(multilineExtractionEnd).category();
            IntStream.rangeClosed(startLine, lineNum)
                     .forEach(i -> extractions.put(i, category));
            startLine = 0;
            multilineExtractionEnd = null;
            combinedPattern = fileType.regexPattern();
          }
        } else {
          extractions.put(lineNum, filetype);
        }
        // Check if multiline comment extends to the end of the file
        if (multilineExtractionEnd != null) {
          var category = existingExtractions.get(multilineExtractionEnd).category();
          IntStream.rangeClosed(startLine, lineNum)
                   .forEach(i -> extractions.put(i, category));
        }
      }
    }
    return extractions;
  }
  
}
