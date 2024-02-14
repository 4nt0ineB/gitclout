package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import static java.util.stream.Collectors.toMap;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Repository {
  public enum Status {
    ANALYZED, IN_ANALYSIS, UPDATING;
  }
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String userName;
  private String name;
  private String url;
  private @JsonIgnore String path;
  private String head;
  private Status status;
  @JsonIgnore
  @OneToMany(targetEntity= Tag.class, cascade=CascadeType.REMOVE, mappedBy="repositoryId", fetch= FetchType.LAZY)
  private final List<Tag> tags = new ArrayList<>();
  
  public Repository() {}
  
  public Repository(String name, String url, String path, String head) {
    Objects.requireNonNull(name, "The name can't be null");
    Objects.requireNonNull(url, "The url can't be null");
    Objects.requireNonNull(path, "The path can't be null");
    Objects.requireNonNull(head, "The head can't be null");
    this.name = name;
    this.url = url;
    this.path = path;
    this.head = head;
  }
  
  
  public record LightTag(UUID id, List<String> names){}
  public record LightRepository(UUID id, String user, String name, String url, List<LightTag> tags, Status status) {}
  
 /* @JsonGetter
  @JsonProperty("tagsOrder")
  public List<String> tagsOrder(){
    return tags.stream()
               .sorted(Comparator.comparing(Tag::commitTime))
               .map(Tag::tagId)
               .toList();
  }
  
  private Map<String, ArrayList<Tag>> tagBySha1(){
    return tags.stream()
               .collect(toMap(t -> Objects.toString(t.parentId()),
                   v -> new ArrayList<>(List.of(v)),
                   (o, o2) ->  {
                 o.addAll(o2);
                 return o;
               }));
  }*/
  /*
  @JsonGetter
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @JsonProperty("tags")
  public Map<String, ComputedTag> tagsDetail(){
    var tagBySha1 = tagBySha1();
    var computedTagsBySha1 = new HashMap<String, ComputedTag>();
    var firsts = tagBySha1.get("null");
    if(firsts == null){
      return computedTagsBySha1;
    }
    var toVisit = new ArrayDeque<>(firsts);
    for(var first: firsts){
      computedTagsBySha1.put(first.tagId, first.new ComputedTag(null));
    }
    while(!toVisit.isEmpty()){
      var parent = toVisit.poll();
      var children = tagBySha1.get(parent.tagId);
      if(children != null){
        for(var child: children){
          computedTagsBySha1.put(child.tagId(), child.new ComputedTag(computedTagsBySha1.get(child.parentId())));
        }
        toVisit.addAll(children.reversed());
      }
    }
    return computedTagsBySha1;
  }*/
  
  public Repository withTags(List<Tag> tags){
    Objects.requireNonNull(tags);
    return new Repository(name, url, path, head);
  }
  
  @Override
  public String toString() {
    return "[" + name + "]";
  }

}
