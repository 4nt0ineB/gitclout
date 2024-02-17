package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.analyzer.FileTypes;
import fr.uge.gitclout.model.entity.Repository;
import fr.uge.gitclout.model.entity.Tag;

import java.util.Map;

public record RepositoryDetail (
      @JsonUnwrapped
      Repository repository,
      @JsonUnwrapped
      AnalysisManager.Status status,
      @JsonIgnore FileTypes fileTypes
      ) {
    @JsonGetter
    public Map<String, Tag.ComputedTag> tags() {
      return repository.tagsDetail(fileTypes);
    }
  }