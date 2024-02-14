package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.AnalysisManager;
import fr.uge.gitclout.model.Repository;
import fr.uge.gitclout.model.repositories.RepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RepositoryService {
  
  @Autowired
  private final RepositoryRepository repository;
  @Autowired
  private final AnalysisManager analysisManager;
  
  public RepositoryService(final RepositoryRepository repository, AnalysisManager analysisManager){
    this.repository = repository;
    this.analysisManager = analysisManager;
  }
  
  public List<Repository.LightRepository> findAll(){
    return repository.findAll().stream().map(this::repositoryToLightRepository).toList();
  }
  
  public Repository save(Repository repositoryModel){
    return repository.save(repositoryModel);
  }
  
  Repository findFirst(){
   return repository.findTopByOrderByIdDesc();
  }
  
  private Repository.LightRepository repositoryToLightRepository(Repository repository){
    var tags = repository.getTags().stream().map(t -> new Repository.LightTag(t.id(), List.of(t.name()))).toList();
    return new Repository.LightRepository(
        repository.getId(),
        repository.getUser(),
        repository.getName(),
        repository.getUrl(),
        tags,
        repository.getStatus());
  }
  
  public Optional<Repository> findById(UUID uuid) {
    return repository.findById(uuid);
  }
  
  public Repository.LightRepository fetchAndAnalyse(String repositoryUrl) {
    
    return null;
  }
  
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
