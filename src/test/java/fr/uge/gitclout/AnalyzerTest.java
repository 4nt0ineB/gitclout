package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.AnalysisManager;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerTest {
  private AnalysisManager manager;
  
 
  void setUp() throws IOException {
    //manager = new AnalysisManager(new Parser(), FileTypes.fromJson(Utils.fileToString("//")), 3);
  }
  
  
  
  void downloadRepo_ValidUrl_ReturnsRepository() throws IOException {
    // Given
    var url = "https://github.com/username/repository.git";
    
    // When
    var repository = manager.extractRepoInfo(url);
    
    // Then
    assertNotNull(repository);
    assertEquals("repository", repository.getName());
    assertEquals(url, repository.getUrl());
    assertEquals(".gitclout-data/repositories/username-repository", repository.getPath());
  }
  
 
  void downloadRepo_InvalidUrl_ThrowsIllegalArgumentException() {
    var invalidUrl = "https://github.com/username";
    
    // When / Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manager.extractRepoInfo(invalidUrl));
    assertEquals("bad formatted git repository url", exception.getMessage());
  }
}
