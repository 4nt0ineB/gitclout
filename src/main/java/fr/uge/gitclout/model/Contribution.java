package fr.uge.gitclout.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.uge.gitclout.analyzer.parser.FileTypes;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name="tag_contribution")
@Entity
public class Contribution {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  private String tagId;
  private String username;
  @ElementCollection
  //@CollectionTable(name="contributions", joinColumns={@JoinColumn(name="tagId", referencedColumnName="id")})
  @MapKeyColumn(name="type")
  @Column(name="price")
  private Map<String, Integer> detail;
  
  public Contribution() {}
  
   /*@JsonGetter
  @JsonProperty("detail")
  public Map<String, Map<String, Integer>> organized(){
    return Map.of();
   return detail.entrySet().stream()
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
  }*/
}
