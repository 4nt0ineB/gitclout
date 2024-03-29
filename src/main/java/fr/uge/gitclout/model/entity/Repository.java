package fr.uge.gitclout.model.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.uge.gitclout.analyzer.FileTypes;
import jakarta.persistence.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;

@Entity
public class Repository {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String username;
  private String name;
  private String url;
  private @JsonIgnore String path;
  private String head;
  @JsonIgnore
  @OneToMany(targetEntity = Tag.class, cascade = CascadeType.REMOVE, mappedBy = "repositoryId", fetch = FetchType.LAZY)
  private final List<Tag> tags = new ArrayList<>();
  
  public Repository() {
  }
  
  public Repository(String name, String username, String url, String path, String head) {
    Objects.requireNonNull(name, "The name can't be null");
    Objects.requireNonNull(url, "The url can't be null");
    Objects.requireNonNull(path, "The path can't be null");
    this.name = name;
    this.username = username;
    this.url = url;
    this.path = path;
    this.head = head;
  }
  
  public Repository(UUID id, String username, String name, String url, String path, String head) {
    this.id = id;
    this.username = username;
    this.name = name;
    this.url = url;
    this.path = path;
    this.head = head;
  }
  
  
  @JsonGetter
  @JsonProperty("tagsOrder")
  public List<String> tagsOrder() {
    return tags.stream()
               .sorted(Comparator.comparing(Tag::commitTime))
               .map(Tag::tagId)
               .toList();
  }
  
  private Map<String, ArrayList<Tag>> childrenTags() {
    return tags.stream()
               .collect(toMap(t -> Objects.toString(t.parentId()),
                   v -> new ArrayList<>(List.of(v)),
                   (o, o2) -> {
                     o.addAll(o2);
                     return o;
                   }));
  }
  
  
  public Map<String, Tag.ComputedTag> tagsDetail(FileTypes fileTypes) {
    var tagBySha1 = childrenTags();
    var computedTagsBySha1 = new HashMap<String, Tag.ComputedTag>();
    var firsts = tagBySha1.get("null");
    if (firsts == null) {
      return computedTagsBySha1;
    }
    var toVisit = new ArrayDeque<>(firsts);
    for (var first : firsts) {
      computedTagsBySha1.put(first.tagSha1Id, first.new ComputedTag(null, fileTypes));
    }
    while (!toVisit.isEmpty()) {
      var parent = toVisit.poll();
      var children = tagBySha1.get(parent.tagSha1Id);
      if (children != null) {
        for (var child : children) {
          computedTagsBySha1.put(child.tagId(), child.new ComputedTag(computedTagsBySha1.get(child.parentId()), fileTypes));
        }
        toVisit.addAll(children.reversed());
      }
    }
    return computedTagsBySha1;
  }
  
  public Repository withTags(List<Tag> tags) {
    Objects.requireNonNull(tags);
    return new Repository(name, username, url, path, head);
  }
  
  @Override
  public String toString() {
    return "[" + name + "]";
  }
  
  public UUID getId() {
    return this.id;
  }
  
  public String getUsername() {
    return this.username;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getUrl() {
    return this.url;
  }
  
  public String getPath() {
    return this.path;
  }
  
  public String getHead() {
    return this.head;
  }
  
  public List<Tag> getTags() {
    return this.tags;
  }
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  @JsonIgnore
  public void setPath(String path) {
    this.path = path;
  }
  
  public void setHead(String head) {
    this.head = head;
  }
}
