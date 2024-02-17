package fr.uge.gitclout.app;

import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.analyzer.FileTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {
    
    @Bean
    public AnalysisManager analysisManager(AnalysisManagerInitializer analysisManagerInitializer) throws IOException {
        return analysisManagerInitializer.initAnalysisManager();
    }
    
    @Bean
    public FileTypes fileTypes(FileTypesInitializer fileTypesInitializer) throws IOException {
        return fileTypesInitializer.initFileTypes();
    }
}
