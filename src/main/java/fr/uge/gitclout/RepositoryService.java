package fr.uge.gitclout;

import com.fasterxml.jackson.annotation.*;
import fr.uge.gitclout.analyzer.parser.AnalysisManager;
import fr.uge.gitclout.analyzer.parser.FileTypes;
import fr.uge.gitclout.analyzer.parser.Parser;
import fr.uge.gitclout.model.Repository;
import fr.uge.gitclout.model.Tag;
import fr.uge.gitclout.model.repositories.RepositoryRepository;
import fr.uge.gitclout.model.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class RepositoryService {
  private static final Logger logger = Logger.getLogger(RepositoryService.class.getName());
  
  @Autowired
  private final RepositoryRepository repoRepository;
  @Autowired
  private final TagRepository tagRepository;
  @Autowired
  private final FileTypes fileTypes;
  @Autowired
  private final AnalysisManager analysisManager;
  
  public RepositoryService(RepositoryRepository repository, TagRepository tagRepository, FileTypes fileTypes,
                           AnalysisManager analysisManager)  {
    this.repoRepository = repository;
    this.tagRepository = tagRepository;
    this.fileTypes = fileTypes;
    this.analysisManager = analysisManager;
  }
  
  public List<Repository.LightRepository> findAll(){
    return repoRepository.findAll().stream().map(this::repositoryToLightRepository).toList();
  }
  
  public Repository save(Repository repositoryModel){
    return repoRepository.save(repositoryModel);
  }
  
  Repository findFirst(){
   return repoRepository.findTopByOrderByIdDesc();
  }
  
  private Repository.LightRepository repositoryToLightRepository(Repository repository){
    var tags = repository.getTags().stream().map(t -> new Repository.LightTag(t.id(), List.of(t.name()))).toList();
    return new Repository.LightRepository(
        repository.getId(),
        repository.getUserName(),
        repository.getName(),
        repository.getUrl(),
        tags,
        repository.getStatus());
  }
  
  public Optional<Repository> findById(UUID uuid) {
    return repoRepository.findById(uuid);
  }
  
  public Repository.LightRepository fetchAndAnalyse(String repositoryUrl) throws IOException {
    var repo = analysisManager.downloadRepo(repositoryUrl);
    save(repo);
    try {
      analysisManager.analyze(repo, tagRepository::save);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return repositoryToLightRepository(repo);
  }
  
  public List<AnalysisManager.Task> getStatus() {
    return analysisManager.getStatus();
  }
  
  public void deleteById(UUID id) {
    repoRepository.deleteById(id);
  }
  
  
  public RepositoryDetail repositoryToRepositoryDetail (Repository repository){
    return new RepositoryDetail(repository, fileTypes);
  }
  
  record RepositoryDetail (
      @JsonUnwrapped
      Repository repository,
      @JsonIgnore FileTypes fileTypes
      ) {
    @JsonGetter
    public Map<String, Tag.ComputedTag> tags() {
      return repository.tagsDetail(fileTypes);
    }
  }
}
