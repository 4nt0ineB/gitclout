package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.model.entity.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LightRepository(UUID id, String user, String name, String url, Map<String, LightTag> tags,
                              @JsonUnwrapped
                              AnalysisManager.Status status) {}
