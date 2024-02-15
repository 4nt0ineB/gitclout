package fr.uge.gitclout;


import fr.uge.gitclout.model.Repository;
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
  
  private List<Repository.LightRepository> lightRepositories;
  private List<Repository> repositories;
  
  
  @BeforeEach
  void setUp() {
    lightRepositories = List.of(
      new Repository.LightRepository(UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb")
      , "Giraudo", "Calimba", "https://github.com/SamueleGiraudo/Calimba",
          List.of(
              new Repository.LightTag(UUID.fromString("95a96a40-5e35-4cb7-a89e-db60cf674049"), List.of("v1")),
              new Repository.LightTag(UUID.fromString("00cc5a0b-817d-4783-b757-761139403298"), List.of("v2"))),
          Repository.Status.ANALYZED
    ));
    
   var repo = new Repository(UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb"),
       "Giraudo", "Calimba", "https://github.com/SamueleGiraudo/Calimba", "the/path/to/the/repo/Calimba",
       "00cc5a0b-817d-4783-b757-761139403298", Repository.Status.IN_ANALYSIS);
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
                    "tags": [
                        {
                            "id": "95a96a40-5e35-4cb7-a89e-db60cf674049",
                            "names": ["v1"]
                        },
                        {
                            "id": "00cc5a0b-817d-4783-b757-761139403298",
                            "names": ["v2"]
                        }
                    ],
                    "status": "ANALYZED"
                }
            ]
        """;
    when(repositoryService.findAll()).thenReturn(lightRepositories);
    mockMvc.perform(MockMvcRequestBuilders.get("/api/repository"))
           .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(jsonResponse, true));
  }
  
  //@Test
  void shouldFindRepositoryWhenGivenValidId() throws Exception {
    
    // Stub the repositoryService.findById method to return Optional.of(repository)
    when(repositoryService.findById(UUID.fromString("be19123e-441b-49ab-b030-8ef6ae224cbb"))).thenReturn(Optional.of(repositories.get(0)));
    
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
