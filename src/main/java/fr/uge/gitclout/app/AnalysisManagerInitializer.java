package fr.uge.gitclout.app;

import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.analyzer.FileTypes;
import fr.uge.gitclout.analyzer.Parser;
import fr.uge.gitclout.analyzer.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Component
public class AnalysisManagerInitializer {
  @Autowired
  private final FileTypes fileTypes;
  @Autowired
  private final Parser parser;
  @Value("${app.concurrentAnalysis}")
  private int concurrentAnalysis;
  @Value("${app.analysisPoolSize}")
  private int analysisPoolSize;
  
  public AnalysisManagerInitializer(FileTypes fileTypes, Parser parser) {
    this.fileTypes = fileTypes;
    this.parser = parser;
  }
  
  public AnalysisManager initAnalysisManager() throws IOException {
    return new AnalysisManager(parser, fileTypes, concurrentAnalysis, analysisPoolSize);
  }
}
