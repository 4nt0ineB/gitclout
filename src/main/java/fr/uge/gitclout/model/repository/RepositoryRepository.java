package fr.uge.gitclout.model.repository;

import fr.uge.gitclout.model.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
  Repository findTopByOrderByIdDesc();
  Repository findTopByPath(String path);
}
