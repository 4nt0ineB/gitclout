package fr.uge.gitclout;

import fr.uge.gitclout.analyzer.parser.AnalysisManager;
import fr.uge.gitclout.model.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/repository")
class RepositoryController {
  private static final Logger logger = Logger.getLogger(RepositoryController.class.getName());
  @Autowired
  private final RepositoryService repository;
  
  public RepositoryController(RepositoryService repository){
    this.repository = repository;
  }
  
  @GetMapping("")
  @Transactional
  ResponseEntity<List<Repository.LightRepository>> getAll(){
    return ResponseEntity.ok(repository.findAll());
  }
  
  @GetMapping("/{id}")
  ResponseEntity<RepositoryService.RepositoryDetail> findById(@PathVariable UUID id) {
    
    return repository.findById(id).map(repository::repositoryToRepositoryDetail).map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
  }
  
  
  record GitRepoCreationRequestURL(String url){}
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  Repository.LightRepository create(@RequestBody GitRepoCreationRequestURL request) throws IOException {
    
    logger.info("receive url: " + request);
    if(request.url == null || request.url.isBlank()) {
      return null;
    }
    return repository.fetchAndAnalyse(request.url);
  }
  
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }
  
  
  @GetMapping("/analysis/status")
  ResponseEntity<List<AnalysisManager.Task>> getAnalysisStatus() {
    return ResponseEntity.ok(repository.getStatus());
  }
  
}
