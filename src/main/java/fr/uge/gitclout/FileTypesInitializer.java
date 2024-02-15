package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.FileTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FileTypesInitializer {

    private final String jsonPath;

    public FileTypesInitializer(@Value("${app.extensions}") String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public FileTypes initFileTypes() throws IOException {
        return FileTypes.fromJson(Utils.fileToString(jsonPath));
    }
}
