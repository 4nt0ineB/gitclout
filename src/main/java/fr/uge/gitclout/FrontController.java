package fr.uge.gitclout;

import fr.uge.gitclout.model.RepositoryModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class FrontController {
  private final RepositoryService repository;
  
  public FrontController(RepositoryService repository){
    this.repository = repository;
  }
  
  @GetMapping("/api/repository")
  @Transactional
  public ResponseEntity<RepositoryModel> get(){
    repository.save(new RepositoryModel("test", "test", "test", "test"));
    RepositoryModel result = repository.findFirst();
    return ResponseEntity.ok(result);

  }
  

}
