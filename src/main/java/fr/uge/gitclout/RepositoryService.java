package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.AnalysisManager;
import fr.uge.gitclout.analyzer.parser.Analyzer;
import fr.uge.gitclout.analyzer.parser.FileTypes;
import fr.uge.gitclout.analyzer.parser.Parser;
import fr.uge.gitclout.model.Repository;
import fr.uge.gitclout.model.repositories.RepositoryRepository;
import fr.uge.gitclout.model.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RepositoryService {
  
  @Autowired
  private final RepositoryRepository repoRepository;
  @Autowired
  private final TagRepository tagRepository;
  
  private final AnalysisManager analysisManager;
  
  public RepositoryService(final RepositoryRepository repository, TagRepository tagRepository) throws IOException {
    this.repoRepository = repository;
    this.tagRepository = tagRepository;
    this.analysisManager = new AnalysisManager(new Parser(), FileTypes.fromJson(Utils.fileToString("/extensions.json")), 2, 3);
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
}
