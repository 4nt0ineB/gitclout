package fr.uge.gitclout.model.repositories;

import fr.uge.gitclout.RepositoryService;
import fr.uge.gitclout.model.RepositoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryRepository extends JpaRepository<RepositoryModel, UUID> {
  RepositoryModel findTopByOrderByIdDesc();
}
