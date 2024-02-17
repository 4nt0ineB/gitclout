package fr.uge.gitclout.model;

import java.util.List;
import java.util.UUID;

public record LightTag(UUID id, String sha1, String parentSha1, List<String> names){}
