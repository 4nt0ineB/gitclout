package fr.uge.gitclout.controller;

import fr.uge.gitclout.model.LightRepository;
import fr.uge.gitclout.model.RepositoryDetail;
import fr.uge.gitclout.service.RepositoryService;
import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.model.entity.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/repository")
public class RepositoryController {
  private static final Logger logger = Logger.getLogger(RepositoryController.class.getName());
  @Autowired
  private final RepositoryService repository;
  
  public RepositoryController(RepositoryService repository){
    this.repository = repository;
  }
  
  @GetMapping("")
  @Transactional
  ResponseEntity<List<LightRepository>> getAll(){
    return ResponseEntity.ok(repository.findAll());
  }
  
  @GetMapping("/{id}")
  ResponseEntity<RepositoryDetail> findById(@PathVariable UUID id) {
    return repository.findById(id).map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
  }
  
  
  record GitRepoCreationRequestURL(String url){}
  @PostMapping("")
  ResponseEntity<LightRepository>  create(@RequestBody GitRepoCreationRequestURL request) throws IOException {
    logger.info("receive url: " + request);
    if(request.url == null || request.url.isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    return Optional.ofNullable(repository.fetchAndAnalyse(request.url)).map(lightRepository -> ResponseEntity.status(HttpStatus.CREATED).body(lightRepository))
                   .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
  }
  
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }
  
  
  @GetMapping("/analysis/status")
  ResponseEntity<List<AnalysisManager.Status>> getAnalysisStatus() {
    return ResponseEntity.ok(repository.getStatus());
  }
  
}
