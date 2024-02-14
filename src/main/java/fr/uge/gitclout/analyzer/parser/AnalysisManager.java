package fr.uge.gitclout.analyzer.parser;

import fr.uge.gitclout.model.Repository;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * The AnalysisManager class orchestrates the analysis of repositories using analyzers.
 * It manages the submission, execution, and cancellation of analysis tasks.
 */
@Service
public class AnalysisManager {
  
  // dependencies
  private final FileTypes fileTypes;
  private final Parser parser;
  private final DaoManager daoManager;
  private final int concurrentAnalysis;
  private final int analysisPoolSize;
  // managing tasks and results
  private final LinkedBlockingQueue<Task> submittedAnalysisQueue = new LinkedBlockingQueue<>();
  private final ConcurrentHashMap<Task, Thread> runningAnalysis = new ConcurrentHashMap<>();
  //
  private final Logger LOGGER = Logger.getLogger(AnalysisManager.class.getName());
  
  
  /**
   * Constructor for AnalysisManager.
   *
   * @param parser     The parser used for analysis.
   * @param fileTypes  The file types manager.
   * @param daoManager The DAO manager for database operations.
   */
  public AnalysisManager(Parser parser, FileTypes fileTypes, DaoManager daoManager, int concurrentAnalysis, int analysisPoolSize) {
    Objects.requireNonNull(parser);
    Objects.requireNonNull(fileTypes);
    Objects.requireNonNull(daoManager);
    this.parser = parser;
    this.fileTypes = fileTypes;
    this.daoManager = daoManager;
    this.concurrentAnalysis = concurrentAnalysis;
    this.analysisPoolSize = analysisPoolSize;
    startAnalysisPool();
  }
  
  /**
   * Represents an analysis' task.
   */
  public static class Task {
    public enum Status {
      QUEUED, RUNNING, CANCELED;
      
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
      return STR."\{analyzer.isUpdate() ? "Update - " : ""}\{status.toString().toLowerCase(Locale.ENGLISH)}";
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
    
    @JsonGetter
    public String url() {
      return analyzer.url();
    }
    
    @JsonGetter
    public String repositoryName() {
      return analyzer.repositoryName();
    }
    
    private void setStatus(Status status) {
      this.status = status;
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
            var repo = task.analyzer.analyze();
            if(task.analyzer.isUpdate()){
              daoManager.repository().delete(repo).subscribe(unused -> {
                daoManager.repository().save(repo).subscribe(no -> {});
              });
            } else if (task.status.equals(Task.Status.CANCELED)) { // if canceled, delete the folder
              RepositoryDao.deleteLocalFolderIfExists(repo);
            } else {
              daoManager.repository().save(repo).subscribe(unused -> {});
            }
            runningAnalysis.remove(task);
          } catch (IOException e) {
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
   * @param url The URL of the repository to be analyzed.
   * @throws InterruptedException If the analysis is interrupted.
   */
  public void analyze(String url) throws InterruptedException {
    var id = UUID.randomUUID();
    submittedAnalysisQueue.put(new Task(id, new Analyzer(parser, fileTypes, url, analysisPoolSize)));
  }
  
  public void update(UUID repositoryId) throws InterruptedException {
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
  }
  
  /**
   * Cancels a specific analysis task based on its ID.
   *
   * @param taskId The ID of the task to be canceled.
   * @return True if the task was successfully canceled, otherwise false.
   */
  public Single<Boolean> cancel(UUID taskId) {
    var analysis = runningAnalysis.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().id.equals(taskId))
                                  .findAny();
    if (analysis.isEmpty()) {
      return Single.just(false);
    }
    var entry = analysis.orElseThrow();
    var task = entry.getKey();
    task.setStatus(Task.Status.CANCELED);
    task.analyzer.cancel();
    return Single.just(true);
  }
  
  /**
   * Retrieves the current states of running and submitted analysis tasks.
   *
   * @return List of Task objects representing the states.
   */
  public List<Task> getStates() {
    return Stream.concat(runningAnalysis.keySet().stream()
        , submittedAnalysisQueue.stream()).toList();
  }
  
  /*
  
    analyzer.analyze(
      repo,
      (tag) -> analyzedTagQueue.add(tag)
    )
  
   */
  
  public Repository downloadRepo(String url) {
    // analyzer.downloadAnd get Info
    // return repo
    
  }
}
