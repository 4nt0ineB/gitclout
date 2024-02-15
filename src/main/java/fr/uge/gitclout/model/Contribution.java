package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.uge.gitclout.analyzer.parser.FileTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "tag_contribution")
@Entity
@Getter
@Setter
public class Contribution {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private UUID tagId;
  private String username;
  @ElementCollection
  @CollectionTable(name="contributions", joinColumns={@JoinColumn(name="tag_contribution_id", referencedColumnName="id")})
  @MapKeyColumn(name = "type")
  @Column(name = "price")
  @JsonInclude
  private Map<String, Integer> detail;
  
  public Contribution(UUID id, UUID tagId, String username, Map<String, Integer> detail) {
    this.id = id;
    this.tagId = tagId;
    this.username = username;
    this.detail = detail;
  }
  
  public Contribution() {
  }
  
  public Map<String, Map<String, Integer>> organized(FileTypes fileTypes) {
    return detail.entrySet()
                 .stream()
                 .collect(Collectors.toMap(e -> fileTypes.getCategory(e.getKey())
                     , entry -> {
                       var a = new HashMap<String, Integer>();
                       a.put(entry.getKey(), entry.getValue());
                       return a;
                     },
                     (o, o2) -> {
                       o.putAll(o2);
                       return o;
                     }, HashMap::new));
  }
}
