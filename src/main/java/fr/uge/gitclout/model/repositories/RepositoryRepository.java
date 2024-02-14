package fr.uge.gitclout.model.repositories;

import fr.uge.gitclout.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
  Repository findTopByOrderByIdDesc();
}
