package fr.uge.gitclout;


import fr.uge.gitclout.analyzer.AnalysisManager;
import fr.uge.gitclout.controller.RepositoryController;
import fr.uge.gitclout.model.LightRepository;
import fr.uge.gitclout.model.LightTag;
import fr.uge.gitclout.model.RepositoryDetail;
import fr.uge.gitclout.model.entity.Repository;
import fr.uge.gitclout.service.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(RepositoryController.class)
@AutoConfigureMockMvc
public class RepositoryControllerTest {
 
  
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private RepositoryService repositoryService;
  
  
  private List<LightRepository> lightRepositories;
  private List<Repository> repositories;
  
  
  @BeforeEach
  void setUp() {
    var id = UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb");
    lightRepositories = List.of(
      new LightRepository(id
      , "Giraudo", "Calimba", "https://github.com/SamueleGiraudo/Calimba",
          Map.of("95a96a40-5e35-4cb7-a89e-db60cf674049",
              new LightTag(UUID.fromString("95a96a40-5e35-4cb7-a89e-db60cf674049"),
                  "984fb537cacb1e4c350c885c98ab4181c3aaef5e",
                  null,
                  List.of("v1")),
              "00cc5a0b-817d-4783-b757-761139403298",
              new LightTag(UUID.fromString("00cc5a0b-817d-4783-b757-761139403298"),
                  "fd48f7eb780f03fbe6e248035479cd4eee2b141e",
                  "984fb537cacb1e4c350c885c98ab4181c3aaef5e",
                  List.of("v2")))
      , new AnalysisManager.Status(id, AnalysisManager.Task.Status.DONE, 2, 2)
    ));
    
   var repo = new Repository(UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb"),
       "Giraudo", "Calimba", "https://github.com/SamueleGiraudo/Calimba", "the/path/to/the/repo/Calimba",
       "00cc5a0b-817d-4783-b757-761139403298");
   repositories = List.of(repo);
  }
  
  @Test
  void shouldFindAllRepositories() throws Exception {
    var jsonResponse = """
        [
                {
                    "id": "be19123e-441b-49ab-b030-8ef6ae224cbb",
                    "name": "Calimba",
                    "user": "Giraudo",
                    "url": "https://github.com/SamueleGiraudo/Calimba",
                    "tags": {
                        "95a96a40-5e35-4cb7-a89e-db60cf674049": {
                            "id": "95a96a40-5e35-4cb7-a89e-db60cf674049",
                            "names": ["v1"]
                        },
                        "00cc5a0b-817d-4783-b757-761139403298": {
                            "id": "00cc5a0b-817d-4783-b757-761139403298",
                            "names": ["v2"]
                        }
                    }
                }
            ]
        """;
    when(repositoryService.findAll()).thenReturn(lightRepositories);
    mockMvc.perform(MockMvcRequestBuilders.get("/api/repository"))
           .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(jsonResponse, false));
  }
  
  //@Test
  void shouldFindRepositoryWhenGivenValidId() throws Exception {
    
    // Stub the repositoryService.findById method to return Optional.of(repository)
    when(repositoryService.findById(UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb")))
        .thenReturn(Optional.of(repositories.get(0)).map(repository -> repositoryService.repositoryToRepositoryDetail(repository, null)));
    
    // When-Then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/repository/{id}", "be19123e-441b-49ab-b030-8ef6ae224cbb"))
           .andExpect(MockMvcResultMatchers.status().isOk())
           .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("be19123e-441b-49ab-b030-8ef6ae224cbb"))
           .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Calimba"));
  }
  
  @Test
  void shouldNotFindRepositoryWhenGivenInvalidId() throws Exception {
    var invalidId = UUID.fromString("adaaca1f-58c4-42ec-a28f-887f8090cfe6");
    when(repositoryService.findById(invalidId)).thenReturn(Optional.empty());
    
    mockMvc.perform(MockMvcRequestBuilders.get("/api/repository/{id}", invalidId))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }
  
  @Test
  void shouldCreateNewRepositoryWhenUrlIsValid() throws Exception {
    var url = "https://github.com/SamueleGiraudo/Calimba";
    var json = "{\"url\":\""+ url + "\"}";
    when(repositoryService.fetchAndAnalyse(url)).thenReturn(lightRepositories.get(0));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/repository")
                                          .contentType("application/json")
                                          .content(json))
           .andExpect(MockMvcResultMatchers.status().isCreated());
  }
  
  @Test
  void shouldDeleteRepositoryWhenGivenValidId() throws Exception {
    var id = UUID.fromString("adaaca1f-58c4-42ec-a28f-887f8090cfe6");
    doNothing().when(repositoryService).deleteById(id);
    mockMvc.perform(MockMvcRequestBuilders.delete("/api/repository/{id}", id))
           .andExpect(MockMvcResultMatchers.status().isNoContent());
  }
  
}
