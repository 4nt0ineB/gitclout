package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @param id
 * @param parentId can be set to null
 * @param name
 */
@Table(name="tag")
@Entity
public class Tag {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @JsonProperty("tagId")
  String tagId;
  long repositoryId;
  @JsonProperty
  int commitTime;
  @JsonProperty("parentTagId")
  String parentId;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String name;
  @JsonIgnore
  @OneToMany(mappedBy = "tagId", cascade = CascadeType.ALL, orphanRemoval = true)
  @MapKey(name = "username")
  Map<String, Contribution> contributions;
  
  public static final String TAG_NAME_SEPARATOR = "\n";
  
  public Tag(){}
  
  public String tagId(){return tagId;}
  public String parentId(){return parentId;}
  public int commitTime(){return commitTime;}
  
  public Tag(String tagId, long repositoryId, String parentId, String name) {
    Objects.requireNonNull(name, "name can't be null");
  }
  
  /*public Tag withName(String name){
    Objects.requireNonNull(name);
    return new Tag(id, repositoryId, commitTime, parentId, name, contributions);
  }
  
  public Tag withParent(Optional<String> parentId){
    return new Tag(id, repositoryId, commitTime, parentId.orElse(null), name, contributions);
  }
  
  class ComputedTag{
    
    @JsonIgnore
    private final ComputedTag parent;
    
    public ComputedTag(ComputedTag parent){
      this.parent = parent;
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
      return parent == null ? null : parent.reference().id();
    }
    
    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Map<String, Map<String, Integer>>> contributions(){
      return reference().contributions().entrySet().stream()
                         .collect(Collectors.toMap(Map.Entry::getKey,
                             e -> e == null ? null : e.getValue().organized()));
    }
  }*/
  
}
