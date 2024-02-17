package fr.uge.gitclout.model.repository;

import fr.uge.gitclout.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@org.springframework.stereotype.Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
}
