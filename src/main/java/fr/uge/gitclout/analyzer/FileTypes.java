package fr.uge.gitclout.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.gitclout.model.FileType;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;


/**
 * Represents a utility class for creating FileTypes based on a JSON configuration.
 */
public class FileTypes {
  
  private final Map<String, FileType> types = new HashMap<>();
  
  public FileTypes() {
  
  }
  
  private FileTypes(Map<String, FileType> languagesExtractions) {
    this.types .putAll(languagesExtractions);
  }
  
  
  
  public Map<String, FileType> types(){
    return Collections.unmodifiableMap(types);
  }
  
  public String getCategory(String fileType){
    Objects.requireNonNull(fileType);
    var category = types.get(fileType);
    return category == null ? fileType : category.contributionCategory();
  }
  
  public Optional<FileType> typeFromFileName(String filePath) {
   return types.values().stream()
               .filter(type -> filePath.endsWith(type.extension()))
               .findAny();
  }
  
  private static Map<String, FileType> extractCodePatterns(JsonNode codeNode){
    var fileTypes = new HashMap<String, FileType>();
    forEachNodesInObject(codeNode, (language, detail) -> {
      List<FileType.ExtractionScope> extractionScopes = new ArrayList<>();
      var extractionsNode = detail.get("extractions");
      var extension = detail.get("extension").asText();
      if (extractionsNode != null && extractionsNode.isObject()) {
        forEachNodesInObject(extractionsNode, (category, extractionDetails) -> {
          var inlineNode = extractionDetails.get("inline");
          if(inlineNode != null){
            for(var pattern: inlineNode){
              extractionScopes.add(new FileType.ExtractionScope(category, pattern.asText(), Optional.empty()));
            }
          }
          var multiLineNode = extractionDetails.get("multiline");
          if(multiLineNode != null){
            for (var multiLineArray: multiLineNode) {
              if (multiLineArray.isArray() && multiLineArray.size() == 2) {
                var startPattern = multiLineArray.get(0).asText();
                var endPattern = multiLineArray.get(1).asText();
                var scopeEnd = new FileType.ExtractionScope(category, endPattern, Optional.empty());
                var scopeStart = new FileType.ExtractionScope(category, startPattern, Optional.of(scopeEnd));
                extractionScopes.add(scopeStart);
              }
            }
          }
        });
      }
      var filetype = new FileType(language, "CODE", extension, Optional.of(extractionScopes));
      fileTypes.put(language, filetype);
    });
    return fileTypes;
  }
  
  /**
   * Parses a JSON string configuration to create FileTypes.
   *
   * @param jsonString The JSON string containing the FileType configurations.
   * @return A FileTypes object containing parsed FileType configurations.
   * @throws IOException If an error occurs during JSON processing.
   */
  public static FileTypes fromJson(String jsonString) throws IOException {
    // getContentTypeFor(String fileName)
    // https://docs.oracle.com/javaee/7/api/javax/ws/rs/core/MediaType.html
    var fileTypes = new HashMap<String, FileType>();
    ObjectMapper mapper = new ObjectMapper();
    var rootNode = mapper.readTree(jsonString);
    forEachNodesInObject(rootNode, (fileCategory, detail) -> {
      if(fileCategory.equals("CODE")){
        fileTypes.putAll(extractCodePatterns(detail));
        return;
      }
      forEachNodesInObject(detail, (type, extension) -> {
        var filetype = new FileType(type, fileCategory, extension.asText(), Optional.empty());
        fileTypes.put(type, filetype);
      });
    });
    return new FileTypes(fileTypes);
  }
  
  private static void forEachNodesInObject(JsonNode node, BiConsumer<String, JsonNode> consumer){
    var entries = node.fields();
    while (entries.hasNext()) {
      var entry = entries.next();
      var field = entry.getKey();
      var value = entry.getValue();
      consumer.accept(field, value);
    }
  }
}