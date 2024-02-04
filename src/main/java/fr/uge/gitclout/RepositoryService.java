package fr.uge.gitclout;

import fr.uge.gitclout.model.RepositoryModel;
import fr.uge.gitclout.model.repositories.RepositoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RepositoryService {
  private final RepositoryRepository repository;
  
  public RepositoryService(final RepositoryRepository repository){
    this.repository = repository;
  }
  
  public Iterable<RepositoryModel> findAll(){
    return repository.findAll();
  }
  
  public RepositoryModel save(RepositoryModel repositoryModel){
    return repository.save(repositoryModel);
  }
  
  RepositoryModel findFirst(){
   return repository.findTopByOrderByIdDesc();
  }
  
}
