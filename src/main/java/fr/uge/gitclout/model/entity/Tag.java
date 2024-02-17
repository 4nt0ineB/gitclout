package fr.uge.gitclout.model.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.uge.gitclout.analyzer.FileTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Table(name="tag")
@Entity
@Setter
@Getter
@AllArgsConstructor
public class Tag {
  
  @Id
  private UUID id;
  @JsonProperty("tagId")
  String tagSha1Id;
  @JsonIgnore
  UUID repositoryId;
  @JsonProperty
  int commitTime;
  @JsonProperty("parentTagId")
  String parentId;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String name;
  @Getter
  @OneToMany(mappedBy = "tagId", cascade = CascadeType.ALL, orphanRemoval = true, fetch= FetchType.LAZY)
  @MapKey(name = "username")
  Map<String, Contribution> contributions = new HashMap<>();
  
  public static final String TAG_NAME_SEPARATOR = "\n";
  
  public Tag(){}
  
  public UUID id(){return id;}
  public String tagId(){return tagSha1Id;}
  public String name(){return name;}
  public String parentId(){return parentId;}
  public UUID repositoryId(){return repositoryId;}
  public int commitTime(){return commitTime;}
  
  public Tag(String tagId, UUID repositoryId, int commitTime, String parentId, String name) {
    this.tagSha1Id = tagId;
    this.repositoryId = repositoryId;
    this.commitTime = commitTime;
    this.parentId = parentId;
    this.name = name;
    Objects.requireNonNull(name, "name can't be null");
  }
  
  
  public class ComputedTag{
    
    private final FileTypes fileTypes;
    
    private final ComputedTag parent;
    
    public ComputedTag(ComputedTag parent, FileTypes fileTypes){
      this.parent = parent;
      this.fileTypes = fileTypes;
    }
    
    @JsonGetter
    public String[] name(){
      return reference().name().split(TAG_NAME_SEPARATOR);
    }
    
    public Tag reference(){
      return Tag.this;
    }
    
    @JsonProperty("parent")
    public String parent(){
      return parent == null ? null : parent.reference().getTagSha1Id();
    }
    
    @JsonGetter
    public Map<String, Map<String, Map<String, Integer>>> contributions(){
      return reference().getContributions().entrySet().stream()
                         .collect(Collectors.toMap(Map.Entry::getKey,
                             e -> e == null ? null : e.getValue().organized(fileTypes)));
    }
  }
  
}
