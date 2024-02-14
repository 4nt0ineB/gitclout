package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.AnalysisManager;
import fr.uge.gitclout.analyzer.parser.FileTypes;
import fr.uge.gitclout.analyzer.parser.Parser;
import fr.uge.gitclout.model.Repository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    var repository = manager.downloadRepo(url);
    
    // Then
    assertNotNull(repository);
    assertEquals("repository", repository.getName());
    assertEquals(url, repository.getUrl());
    assertEquals(".gitclout-data/repositories/username-repository", repository.getPath());
  }
  
 
  void downloadRepo_InvalidUrl_ThrowsIllegalArgumentException() {
    var invalidUrl = "https://github.com/username";
    
    // When / Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manager.downloadRepo(invalidUrl));
    assertEquals("bad formatted git repository url", exception.getMessage());
  }
}
