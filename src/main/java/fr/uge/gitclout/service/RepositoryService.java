package fr.uge.gitclout.service;

import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.analyzer.FileTypes;
import fr.uge.gitclout.model.LightRepository;
import fr.uge.gitclout.model.LightTag;
import fr.uge.gitclout.model.RepositoryDetail;
import fr.uge.gitclout.model.entity.Repository;
import fr.uge.gitclout.model.entity.Tag;
import fr.uge.gitclout.model.repository.RepositoryRepository;
import fr.uge.gitclout.model.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
  
  public List<LightRepository> findAll(){
    var status = getStatus().stream().collect(Collectors.toMap(AnalysisManager.Status::statusId, o -> o));
    return repoRepository.findAll().stream().map(repository -> {
      var associatedStatus =  status.getOrDefault(repository.getId(), analysisManager.fullFromRepository(repository));
      return repositoryToLightRepository(repository, associatedStatus);
    }).toList();
  }
  
  public Repository save(Repository repositoryModel){
    return repoRepository.save(repositoryModel);
  }
  
  Repository findFirst(){
   return repoRepository.findTopByOrderByIdDesc();
  }
  
  private LightRepository repositoryToLightRepository(Repository repository, AnalysisManager.Status status){
    //var tags = repository.getTags().stream().map(t -> )).toList();
    var tags = repository.getTags().stream().collect(Collectors.toMap(Tag::tagId, t -> new LightTag(t.id(), t.tagId(), t.parentId(), List.of(t.name()))));
    return new LightRepository(
        repository.getId(),
        repository.getUsername(),
        repository.getName(),
        repository.getUrl(),
        tags,
        repository.tagsOrder(),
        status
    );
  }
  
  public Optional<RepositoryDetail> findById(UUID uuid) {
    var repo = repoRepository.findById(uuid);
    return repo.map(repository -> {
      var status = getStatus().stream().filter(s -> s.statusId().equals(repository.getId())).findFirst()
                              .orElseGet(() -> analysisManager.fullFromRepository(repository));
      return repositoryToRepositoryDetail(repository, status);
    });
  }
  
  
  
  public Optional<AnalysisManager.Status> getStatus(UUID id) {
    var repo = repoRepository.findById(id);
    return repo.map(repository -> getStatus().stream()
                   .filter(s -> s.statusId().equals(repository.getId()))
                   .findFirst()
                   .orElseGet(() -> analysisManager.fullFromRepository(repository))
    );
  }
  
  public LightRepository fetchAndAnalyse(String repositoryUrl) throws IOException {
    var existingRepo = repoRepository.findTopByPath(repositoryUrl);
    if(existingRepo != null) {
      return null;
    }
    var repo = analysisManager.extractRepoInfo(repositoryUrl);
    save(repo);
    try {
      analysisManager.analyze(repo, tagRepository::save);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    var status = getStatus().stream().filter(s -> s.statusId().equals(repo.getId())).findFirst()
        .orElseGet(() -> analysisManager.fullFromRepository(repo));
    return repositoryToLightRepository(repo, status);
  }
  
  public List<AnalysisManager.Status> getStatus() {
    return analysisManager.getStatus();
  }
  
  public boolean deleteById(UUID id) {
    var repo = findById(id);
    return repo.map(repositoryDetail -> {
      repoRepository.deleteById(id);
      analysisManager.deleteLocalRepoDir(repositoryDetail.repository());
      return true;
    }).orElse(false);
  }
  
  
  public RepositoryDetail repositoryToRepositoryDetail (Repository repository, AnalysisManager.Status status){
    return new RepositoryDetail(repository, status, fileTypes);
  }
  
  
}
