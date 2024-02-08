package fr.uge.gitclout.model;

import jakarta.persistence.Entity;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public class FileType {
  
  private final String name;
  private final String contributionCategory;
  private final String extension;

  // generated at instantiation
  private final HashMap<String, String> startPatterns = new HashMap<>();
  private final HashMap<String, String> endPatterns = new HashMap<>();
  private final Pattern regexPattern;
  private final HashMap<String, ExtractionScope> extractionScopes = new HashMap<>();
  private final Pattern regexPatternMultilineEnd;
  
  
  public FileType(String name, String contributionCategory,
                  String extension,
                  Optional<List<ExtractionScope>> extractionScopes ) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(contributionCategory);
    Objects.requireNonNull(extractionScopes);
    this.name = name;
    this.extension = extension;
    this.contributionCategory = contributionCategory;
    String start = "";
    String end = "";
    var i = 0;
    if(extractionScopes.isEmpty()){
      regexPattern = Pattern.compile(start);
      regexPatternMultilineEnd = Pattern.compile(end);
      return;
    }
    for(var extraction: extractionScopes.orElseThrow()){
      var patternId = i;
      var patternLabel = "p" + patternId;
      this.extractionScopes.put(patternLabel, extraction);
      startPatterns.put(patternLabel, "(?<" +  patternLabel + ">" + extraction.pattern + ")");
      extraction.boundary
          .ifPresent(endOfScope ->
              endPatterns.put(patternLabel, "(?<" + patternLabel  + ">" + endOfScope.pattern + ")"));
      i++;
    }
    start = startPatterns.values().stream().collect(joining("|", "^", "$"));
    end = endPatterns.values().stream().collect(joining("|", "^", "$"));
    regexPattern = Pattern.compile(start);
    regexPatternMultilineEnd = Pattern.compile(end);
  }
  
  
  public Pattern regexPattern(){
    return regexPattern;
  }
  
  public Pattern regexPatternMultilineEnd(){
    return regexPatternMultilineEnd;
  }
  
  public String extractionCategoryFromLabel(String label){
    Objects.requireNonNull(label);
    var extraction = startPatterns.get(label);
    if(extraction == null){
      throw new IllegalArgumentException("This extraction does not exists");
    }
    return extractionScopes.get(label).category;
  }
  
  public Map<String, ExtractionScope> extractionScopes(){
    return Collections.unmodifiableMap(extractionScopes);
  }
  
  public String name(){
    return name;
  }
  
  public String extension(){
    return extension;
  }
  
  public String contributionCategory(){
    return contributionCategory;
  }
  
  public record ExtractionScope(String category, String pattern, Optional<ExtractionScope> boundary) {
    public ExtractionScope {
      Objects.requireNonNull(category);
      Objects.requireNonNull(pattern);
      Objects.requireNonNull(boundary);
      if(category.isBlank()){
        throw new IllegalArgumentException("The category name can't be blank");
      }
      if (pattern.isEmpty()) {
        throw new IllegalArgumentException("Extraction scope can't be empty (" + category + "," + ", \"" + pattern + "\")");
      }
    }
    public boolean inline(){
      return boundary.isEmpty();
    }
  }
  
}