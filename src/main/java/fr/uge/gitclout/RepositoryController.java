package fr.uge.gitclout;

import fr.uge.gitclout.model.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repository")
class RepositoryController {
  
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
  ResponseEntity<Repository> findById(@PathVariable UUID id) {
    return repository.findById(id).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
  
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("")
  Repository.LightRepository create(@RequestBody String url)  {
    if(url == null || url.isBlank()) {
      return null;
    }
    return repository.fetchAndAnalyse(url);
  }
  
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  void delete(@PathVariable UUID id) {
    repository.deleteById(id);
  }
  
}
