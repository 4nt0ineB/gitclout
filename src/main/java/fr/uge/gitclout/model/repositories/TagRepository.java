package fr.uge.gitclout.model.repositories;

import fr.uge.gitclout.model.Repository;
import fr.uge.gitclout.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
}
