package fr.uge.gitclout.analyzer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import fr.uge.gitclout.model.entity.Repository;
import fr.uge.gitclout.model.entity.Tag;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The AnalysisManager class orchestrates the analysis of repositories using analyzers.
 * It manages the submission, execution, and cancellation of analysis tasks.
 */

public class AnalysisManager {
  private final Logger LOGGER = Logger.getLogger(AnalysisManager.class.getName());
  

  private final FileTypes fileTypes;
  private final Parser parser;
  private final int concurrentAnalysis;
  private final int analysisPoolSize;
  @Value("${app.data}")
  private String gitRepositoryRootPath;
  
  // managing tasks and results
  private final LinkedBlockingQueue<Task> submittedAnalysisQueue = new LinkedBlockingQueue<>();
  private final ConcurrentHashMap<Task, Thread> runningAnalysis = new ConcurrentHashMap<>();
  
  
  /**
   * Constructor for AnalysisManager.
   *
   * @param parser     The parser used for analysis.
   * @param fileTypes  The file types manager.
   */
  public AnalysisManager(Parser parser, FileTypes fileTypes, int concurrentAnalysis, int analysisPoolSize) {
    this.concurrentAnalysis = concurrentAnalysis;
    this.analysisPoolSize = analysisPoolSize;
    Objects.requireNonNull(fileTypes);
    this.parser = parser;
    this.fileTypes = fileTypes;
    startAnalysisPool();
  }
  
  
  public record Status (
      @JsonIgnore
      UUID statusId,
      Task.Status status,
      int totalTags,
      int analyzedTags
  ) {
  }
  
  /**
   * Represents an analysis' task.
   */
  
  
  public static class Task {
    public enum Status {
      QUEUED, RUNNING, CANCELED, DONE;
      
      @JsonValue
      String value() {
        return super.toString()
                    .toLowerCase(Locale.ENGLISH);
      }
    }
    private final UUID id;
    private final Analyzer analyzer;
    private Status status;
    
    private Task(UUID id, Analyzer analyzer) {
      this.id = id;
      this.analyzer = analyzer;
      status = Status.QUEUED;
    }
    
    @JsonGetter
    public String status() {
      return analyzer.isUpdate() ? "Update - " : status.toString().toLowerCase(Locale.ENGLISH);
    }
    
    @JsonGetter
    public UUID id() {
      return id;
    }
    
    @JsonGetter
    public int totalTags() {
      return analyzer.totalTags();
    }
    
    @JsonGetter
    public int analyzedTags() {
      return analyzer.analyzedTags();
    }
    
    private Task setStatus(Status status) {
      this.status = status;
      return this;
    }
    
    public AnalysisManager.Status toStatus(){
      return new AnalysisManager.Status(id, status, totalTags(), analyzedTags());
    }
  }
  
  /**
   * Initializes the routine for analysis.
   */
  public void startAnalysisPool() {
    // Run analysis tasks concurrently
    for (int i = 0; i < concurrentAnalysis; i++) {
      Thread.ofPlatform().start(() -> {
        while (!Thread.interrupted()) {
          Task task = null;
          try {
            task = submittedAnalysisQueue.take();
          } catch (InterruptedException e) {
            continue;
          }
          try {
            runningAnalysis.put(task, Thread.currentThread());
            task.setStatus(Task.Status.RUNNING);
            LOGGER.info("Starting analysis Task :" + task.id);
            var repo = task.analyzer.analyze();
            /*if(task.analyzer.isUpdate()){
              daoManager.repository().delete(repo).subscribe(unused -> {
                daoManager.repository().save(repo).subscribe(no -> {});
              });
            } else if (task.status.equals(Task.Status.CANCELED)) { // if canceled, delete the folder
              RepositoryDao.deleteLocalFolderIfExists(repo);
            } else {
              daoManager.repository().save(repo).subscribe(unused -> {});
            }*/
            runningAnalysis.remove(task);
          } catch (Exception e) {
            LOGGER.info(e.getMessage());
            runningAnalysis.remove(task);
          }
        }
      });
    }
  }
  
  /**
   * Initiates the analysis for a given URL.
   *
   * @throws InterruptedException If the analysis is interrupted.
   */
  public void analyze(Repository repository, Consumer<Tag> fct) throws InterruptedException {
    LOGGER.info("Analysis request, Task refering repository id:"+ repository.getId() + " put in queue");
    submittedAnalysisQueue.put(new Task(repository.getId(), new Analyzer(parser, fileTypes, analysisPoolSize, false, repository, fct)));
  }
  
  
  
  /*public void update(UUID repositoryId) throws InterruptedException {
    var id = UUID.randomUUID();
    daoManager.repository()
              .get(repositoryId)
              .subscribe(repositoryModel -> {
                repositoryModel.ifPresent(repo -> {
                  try {
                    submittedAnalysisQueue.put(new Task(id, Analyzer.forUpdate(parser, fileTypes, repo, analysisPoolSize)));
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                });
              });
  }*/
  
  /**
   * Cancels a specific analysis task based on its ID.
   *
   * @param taskId The ID of the task to be canceled.
   * @return True if the task was successfully canceled, otherwise false.
   */
  public Boolean cancel(UUID taskId) {
    var analysis = runningAnalysis.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().id.equals(taskId))
                                  .findAny();
    if (analysis.isEmpty()) {
      return false;
    }
    var entry = analysis.orElseThrow();
    var task = entry.getKey();
    task.setStatus(Task.Status.CANCELED);
    task.analyzer.cancel();
    return true;
  }
  
  /**
   * Retrieves the current states of running and submitted analysis tasks.
   *
   * @return List of Task objects representing the states.
   */
  public List<Status> getStatus() {
    return Stream.concat(runningAnalysis.keySet().stream()
        , submittedAnalysisQueue.stream()).map(Task::toStatus).toList();
  }
  
  public Repository extractRepoInfo(String url) throws IOException {
    Pattern pattern = Pattern.compile(".*/(?<userName>.*)/(?<repositoryName>.*?)(\\.git)?");
    var matcher = pattern.matcher(url);
    if (matcher.matches()) {
      var repositoryName = matcher.group("repositoryName");
      var localRepositoryPath = gitRepositoryRootPath + "/repositories/" + matcher.group("userName") + "-" + repositoryName;
      // download
      var file = new File(localRepositoryPath);
      if (file.exists()) {
        throw new IllegalArgumentException("The repository already exists");
      }
      return new Repository(repositoryName, url, localRepositoryPath, null);
    }
    throw new IllegalArgumentException("bad formatted git repository url");
  }
  
  public Status fullFromRepository(Repository repository) {
    var tagsNumber = repository.getTags().size();
    return new Status(repository.getId(), Task.Status.DONE, tagsNumber, tagsNumber);
  }
}
