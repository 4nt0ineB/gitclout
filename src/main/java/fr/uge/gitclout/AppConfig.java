package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.FileTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Bean
    public FileTypes fileTypes(FileTypesInitializer fileTypesInitializer) throws IOException {
        return fileTypesInitializer.initFileTypes();
    }
}
