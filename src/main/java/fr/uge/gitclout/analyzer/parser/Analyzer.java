package fr.uge.gitclout.analyzer.parser;

import fr.uge.gitclout.model.Contribution;
import fr.uge.gitclout.model.Tag;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Analyzer {
  // ---------------
  // Internal Representation
  // ---------------
  record ContributionsOfTag(RevCommit commit, RevCommit parent, Map<String, Map<String, Integer>> contributions) {}
  
  // ---------------
  // Configuration and Settings
  // ---------------
  private final int analysisPoolSize;
  private final boolean isUpdate;
  
  // ---------------
  // Dependency Injection
  // ---------------
  private final Parser parser;
  private final FileTypes fileTypes;
  
  // ---------------
  // Repository Information
  // ---------------
  private String url;
  private String localRepositoryPath;
  private ObjectId head;
  private String repositoryName;
  
  // ---------------
  // Analysis Progress
  // ---------------
  private final AtomicInteger tagsAnalyzed = new AtomicInteger(0);
  private int totalTags;
  
  // ---------------
  // Repository Handling
  // ---------------
  private final RepositoryModel repositoryModel;
  private Repository repository;
  private Git git;
  private Map<RevCommit, Set<Ref>> tagsOfCommits;
  
  // ---------------
  // Analysis Control
  // ---------------
  private final AtomicBoolean isCanceled = new AtomicBoolean(false);
  
  public Analyzer(Parser parser, FileTypes fileTypes, String url, int analysisPoolSize) {
    
    this(parser, fileTypes, url, analysisPoolSize, false, null);
  }
  
  private Analyzer(Parser parser, FileTypes fileTypes, String url, int analysisPoolSize, boolean isUpdate, RepositoryModel repositoryModel) {
    Objects.requireNonNull(parser);
    Objects.requireNonNull(fileTypes);
    Objects.requireNonNull(url);
    if (analysisPoolSize < 1) {
      throw new IllegalArgumentException("PoolSize can't be less than 1");
    }
    this.parser = parser;
    this.fileTypes = fileTypes;
    this.url = url;
    this.analysisPoolSize = analysisPoolSize;
    this.isUpdate = isUpdate;
    this.repositoryModel = repositoryModel;
  }
  
  
  public static Analyzer forUpdate(Parser parser, FileTypes fileTypes, RepositoryModel repositoryModel, int analysisPoolSize) {
    return new Analyzer(parser, fileTypes, repositoryModel.url(), analysisPoolSize, true, repositoryModel);
  }
  
  public boolean isUpdate() {
    return isUpdate;
  }
  
  public int totalTags() {
    return totalTags;
  }
  
  public int analyzedTags() {
    return tagsAnalyzed.get();
  }
  
  public String url() {
    return url;
  }
  
  public String repositoryName() {
    return repositoryName;
  }
  
  public String localRepositoryPath() {
    return localRepositoryPath;
  }
  
  public void cancel() {
    isCanceled.set(true);
  }
  
  public static void main(String[] args) throws GitAPIException, IOException {
    var path = Config.create().get("app.extensions")
                     .asString()
                     .orElseThrow(() -> new IOException("extensions.json configuration file could not be found"));
    var jsonString = Utils.fileToString(path);
    var parser = new Parser();
    var analyzer = new Analyzer(parser, FileTypes.fromJson(jsonString),
        "https://github.com/SamueleGiraudo/Calimba.git", 2);
    analyzer.localRepositoryPath = ".gitclout-data/repositories/test-repo";
    var file = new File(analyzer.localRepositoryPath);
    RepositoryModel result;
    analyzer.url = "";
    analyzer.repositoryName = "";
    try (var gitRepo = Git.open(file)) {
      analyzer.git = gitRepo;
      result = analyzer.runAnalysis(null);
    }
  }
  
  /**
   * Download and analyze the repository
   *
   * @return A RepositoryModel object representing the analyzed repository.
   * @throws IOException     If the analysis fails.
   */
  public RepositoryModel analyze() throws IOException {
    setUrl(url);
    if (isUpdate) {
      return updateRepository(repositoryModel);
    }
    var file = new File(localRepositoryPath);
    if (file.exists()) {
      throw new IllegalArgumentException("The repository already exists");
    }
    try (var gitRepo = Git.cloneRepository().setURI(url)
                          .setBare(true)
                          .setDirectory(file).call()) {
      git = gitRepo;
      return runAnalysis(null);
    } catch (GitAPIException e) {
      throw new IOException(e);
    }
  }
  
  /**
   * Start the analysis
   * @param tagParentMap The map representing tag-parent relationships (can be null to generate the map).
   * @return A RepositoryModel representing the analyzed repository.
   */
  private RepositoryModel runAnalysis(Map<RevCommit, RevCommit> tagParentMap) throws IOException, GitAPIException {
    repository = git.getRepository();
    head = repository.resolve("HEAD");
    if (tagParentMap == null) {
      tagsOfCommits = mapTagsToCommit(git);
      var tags = git.tagList().call();
      tagParentMap = getTagParentMap(repository, tags);
    }
    totalTags = tagParentMap.size();
    var contributions = mutlithreadTasks(tagParentMap);
    if (isCanceled.get()) {
      return buildRepositoryModel(Map.of());
    }
    return analysisToModel(contributions);
  }
  
  /**
   * Converts a list of ContributionsOfTag objects into a RepositoryModel representing analysis details.
   *
   * @param tagDetail List of ContributionsOfTag objects containing tag analysis details.
   * @return A RepositoryModel object representing the analyzed repository.
   */
  private RepositoryModel analysisToModel(List<ContributionsOfTag> tagDetail) {
    var tagBySha1 = buildTagMap(tagDetail);
    var computedTags = new HashMap<String, Tag>();
    var computedTagsFromOld = new HashMap<String, Tag>();
    var firsts = tagBySha1.get("null");
    if (firsts != null) {
      var toVisit = new ArrayDeque<>(firsts);
      processTagDetails(tagBySha1, computedTags,  Map.of(), toVisit);
    } else if(isUpdate && !tagDetail.isEmpty()) {
      // get the firsts oldest of the new tags
      var toVisit = new ArrayDeque<>(tagBySha1.entrySet().stream()
                                              .min(Comparator.comparing(entry ->
                                                  entry.getValue().getFirst().commit.getCommitTime()))
                                              .map(Map.Entry::getValue).orElseThrow());
      // pick one of the oldest tag of new commits
      var topTag = toVisit.peekFirst();
      // get the last tag of the old repo,
      // that is the oldest ancestor of all new tags (parent of topTag)
      var origin = repositoryModel.tags().stream()
                                  .filter(t -> t.id().equals(topTag.parent.getName()))
                                  .findFirst().orElseThrow();
      computedTags.put(topTag.parent.getName(), origin.withParent(Optional.empty()));
      var bla = repositoryModel.tags().stream().filter(t -> t.commitTime() < origin.commitTime())
                               .collect(Collectors.toMap(Tag::parentId, t -> t));
      computedTagsFromOld.putAll(bla);
      var mapped = bla.values().stream().map(t ->{
        try {
          return contributionsOfTagFromTag(t);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toCollection(ArrayList::new));
      mapped.addAll(tagDetail);
      tagBySha1 = buildTagMap(mapped);
      processTagDetails(tagBySha1, computedTags, computedTagsFromOld, toVisit);
    }
    return buildRepositoryModel(computedTags);
  }
  
  private ContributionsOfTag contributionsOfTagFromTag(Tag tag) throws IOException {
    return new ContributionsOfTag(repository.parseCommit(repository.resolve(tag.id()))
        , repository.parseCommit(repository.resolve(tag.id()))
        , tag.contributions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
        entry -> entry.getValue().detail()))
    );
  }
  
  private Map<String, List<ContributionsOfTag>> buildTagMap(List<ContributionsOfTag> tagDetail) {
    return tagDetail.stream()
                    .collect(toMap(t -> t.parent == null ? "null" : t.parent.getName(),
                        v -> new ArrayList<>(List.of(v)), (o, o2) -> {
                          o.addAll(o2);
                          return o;
                        }));
  }
  
  /**
   * Merge the tag contributions to get the total contribution since the first tag
   * (or the very first commit for the first tag)
   * This method takes in three parameters:
   * - tagBySha1: A Map linking commit SHA-1 IDs to a list of ContributionsOfTag objects.
   * - computedTags: A Map linking commit names to Tag objects.
   * - toVisit: An ArrayDeque of ContributionsOfTag objects representing commits to be visited.
   * The method iterates through the 'toVisit' queue in topological order.
   *
   * @param tagBySha1    A map of commit SHA-1 IDs to a list of ContributionsOfTag objects.
   * @param computedTags A map of commit names to Tag objects.
   * @param toVisit      An ArrayDeque containing ContributionsOfTag objects representing commits to be visited.
   */
  private void processTagDetails(Map<String, List<ContributionsOfTag>> tagBySha1,
                                 Map<String, Tag> computedTags,
                                 Map<String, Tag> computedTagsFromOld,
                                 ArrayDeque<ContributionsOfTag> toVisit) {
    while (!toVisit.isEmpty()) {
      var current = toVisit.poll();
      var commitTime = current.commit.getCommitTime();
      var parentId = current.parent == null ? null : current.parent.getName();
      var names = getAllTagNames(current.commit);
      Map<String, Map<String, Integer>> contributions;
      if (current.parent == null) {
        contributions = current.contributions;
      } else {
        var parent = computedTags.get(current.parent.getName());
        if(parent != null){
          contributions = addContributionsOfTag(current, parent);
        }else{
          parent = computedTagsFromOld.get(current.parent.getName());
          if(parent == null){
            contributions = current.contributions;
          }else{
            contributions = addContributionsOfTag(current, parent);
          }
        }
      }
      var mappedContributions = contributions.entrySet().stream()
                                             .collect(toMap(Map.Entry::getKey,
                                                 entry -> new Contribution(entry.getValue(), fileTypes)));
      var tag = new Tag(current.commit.getName(), commitTime, parentId, names, mappedContributions);
      computedTags.put(current.commit.getName(), tag);
      var children = tagBySha1.get(current.commit.getName());
      if (children != null) {
        toVisit.addAll(children.reversed());
      }
    }
  }
  
  /**
   * Retrieves all tag names associated with a given ContributionsOfTag's commit.
   * Removes the "refs/tags/" prefix from tag names and joins them using a separator.
   *
   * @param commit The RevCommit object.
   * @return A string containing all associated tag names separated by a defined separator.
   */
  private String getAllTagNames(RevCommit commit) {
    return tagsOfCommits.get(commit)
                        .stream()
                        .map(Ref::getName)
                        .map(tagName -> tagName.replace("refs/tags/", ""))
                        .collect(joining(Tag.TAG_NAME_SEPARATOR));
  }
  
  /**
   * Merges contributions from a ContributionsOfTag object with a Tag object's contributions.
   * Combines the contributions and returns a HashMap with merged contribution data.
   *
   * @param contributionsOfTag The ContributionsOfTag object containing strict difference contributions between two tags.
   * @param tag                The Tag containing contributions to be merged with.
   * @return A HashMap containing combined contributions from ContributionsOfTag and Tag.
   */
  private static HashMap<String, Map<String, Integer>> addContributionsOfTag(Analyzer.ContributionsOfTag contributionsOfTag
      , Tag tag){
    var contributionsCombined = new HashMap<>(contributionsOfTag.contributions);
    for (var entry : tag.contributions().entrySet()) {
      var key = entry.getKey();
      var innerMap = entry.getValue()
                          .detail();
      contributionsCombined.merge(key, innerMap, (innerMap1, innerMap2Values) -> {
        innerMap2Values.forEach((innerKey, value) ->
            innerMap1.merge(innerKey, value, Integer::sum));
        return innerMap1;
      });
    }
    return contributionsCombined;
  }
  
  private RepositoryModel buildRepositoryModel(Map<String, Tag> computedTags) {
    return new RepositoryModel(UUID.randomUUID(),
        repositoryName, url, localRepositoryPath,
        head.toObjectId().name(),
        computedTags.values().stream().toList());
  }
  
  /**
   * Sets the URL for the repository and extracts relevant information like repository name and local path.
   * The method uses a specified URL pattern to extract necessary details such as the repository name and username
   * to construct a local path for the repository.
   * For example, for the URL "https://github.com/userName/repositoryName.git",
   * the repository directory would be "userName-repositoryName".
   *
   * @param url The repository URL, expected to be in the format "https://.../userName/repositoryName.git"
   * @throws IllegalArgumentException If the provided URL does not match the expected pattern.
   */
  private void setUrl(String url) {
    this.url = url;
    Pattern pattern = Pattern.compile(".*/(?<userName>.*)/(?<repositoryName>.*?)(\\.git)?");
    var matcher = pattern.matcher(url);
    if (matcher.matches()) {
      repositoryName = matcher.group("repositoryName");
      localRepositoryPath = ".gitclout-data/repositories/" + matcher.group("userName") + "-" + repositoryName;
      return;
    }
    System.err.println("The url is not valid");
    throw new IllegalArgumentException("The url is not valid");
  }
  
  /**
   * Retrieve an AbstractTreeIterator for a specific commit SHA1.
   *
   * @param SHA1 The SHA1 of the commit you want to retrieve the tree for.
   * @return An AbstractTreeIterator for the specified commit's tree.
   * @throws IOException If there are any issues accessing Git objects.
   */
  private AbstractTreeIterator getTreeIterator(String SHA1) throws IOException {
    var tree = new CanonicalTreeParser();
    // Resolve the tree ID for the given commit SHA1.
    var treeId = git.getRepository().resolve(SHA1 + "^{tree}");
    // Initialize the tree parser with the resolved tree ID.
    try (var reader = git.getRepository().newObjectReader()) {
      tree.reset(reader, treeId);
    }
    return tree;
  }
  
  /**
   * Retrieves a mapping of Git tags to their corresponding commits.
   *
   * @param repository The Git repository to be queried.
   * @param tags       A list of Ref objects representing the tags in the repository.
   * @return A Map linking Ref objects (tags) to RevCommit objects (commits).
   * @throws IOException If an I/O error occurs while accessing the repository.
   */
  public Map<Ref, RevCommit> getTagsToCommitMap(Repository repository, List<Ref> tags) throws IOException {
    Map<Ref, RevCommit> tagToCommitMap = new HashMap<>();
    for (Ref tag : tags) {
      try (RevWalk revWalk = new RevWalk(repository)) {
        var peeledObjectId = tag.getPeeledObjectId();
        RevCommit commit = revWalk.parseCommit(
            peeledObjectId != null ? peeledObjectId : tag.getObjectId());
        tagToCommitMap.put(tag, commit);
      }
    }
    return tagToCommitMap;
  }
  
  /**
   * Finds the parent tag of a given tag.
   *
   * @param currentTag     The tag for which the parent is to be found.
   * @param tagToCommitMap A Map linking tags to their corresponding commits.
   * @param repository     The Git repository to be queried.
   * @return The parent tag of the given tag, or null if no parent is found.
   * @throws IOException If an I/O error occurs while accessing the repository.
   */
  public Ref findTagParent(Ref currentTag, Map<Ref, RevCommit> tagToCommitMap,
                           Repository repository) throws IOException {
    var currentCommit = tagToCommitMap.get(currentTag);
    try (RevWalk revWalk = new RevWalk(repository)) {
      revWalk.markStart(currentCommit);
      for (var commit : revWalk) {
        for (var entry : tagToCommitMap.entrySet()) {
          if (!entry.getKey().equals(currentTag)
              && entry.getValue().equals(commit)) {
            return entry.getKey();
          }
        }
      }
    }
    return null;
  }
  
  private static Map<RevCommit, Set<Ref>> mapTagsToCommit(Git git) throws GitAPIException, IOException {
    var repo = git.getRepository();
    RevWalk revWalk = new RevWalk(repo);
    // commitSha1 -> tags
    Map<RevCommit, HashSet<Ref>> tagCommitMap = new HashMap<>();
    for (var tag : git.tagList().call()) {
      var tagCommit = revWalk.parseCommit(tag.getObjectId());
      tagCommitMap.computeIfAbsent(tagCommit, (k) -> new HashSet<>()).add(tag);
    }
    return Collections.unmodifiableMap(tagCommitMap);
  }
  
  /**
   * Builds a mapping of Git tags to their parent tags.
   *
   * @param repository The Git repository to be queried.
   * @param tags       A list of Ref objects representing the tags in the repository.
   * @return A Map linking each tag to its parent tag.
   * @throws IOException If an I/O error occurs while accessing the repository.
   */
  public Map<RevCommit, RevCommit> getTagParentMap(Repository repository, List<Ref> tags) throws IOException {
    var tagToCommitMap = getTagsToCommitMap(repository, tags);
    Map<RevCommit, RevCommit> tagParentMap = new HashMap<>();
    for (var currentTag : tags) {
      var parentTag = findTagParent(currentTag, tagToCommitMap, repository);
      var currentCommit = tagToCommitMap.get(currentTag);
      var parentCommit = tagToCommitMap.get(parentTag);
      // avoid a tag to be a parent of itself
      if (parentCommit != null && currentCommit.getName().equals(parentCommit.getName())) {
        parentCommit = null;
      }
      tagParentMap.put(currentCommit, parentCommit);
    }
    return tagParentMap;
  }
  
  
  
  /**
   * Performs multi-threaded execution of analysis tasks based on a tag-parent mapping.
   *
   * @param tagParentMap A map representing tag-parent relationships.
   * @return A list of ContributionsOfTag objects resulting from the analysis tasks.
   */
  private List<ContributionsOfTag> mutlithreadTasks(Map<RevCommit, RevCommit> tagParentMap){
    List<ContributionsOfTag> contributions = new ArrayList<>();
    // multithreaded analysis
    var executorService = Executors.newFixedThreadPool(analysisPoolSize);
    var callables = toCallables(tagParentMap);
    // run tasks
    List<Future<ContributionsOfTag>> futures;
    try {
      futures = executorService.invokeAll(callables);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    }
    for (var future : futures) {
      switch (future.state()) {
        case RUNNING -> throw new AssertionError("should not be there");
        case SUCCESS -> contributions.add(future.resultNow());
        case FAILED -> System.out.println(future.exceptionNow());
        case CANCELLED -> System.out.println("cancelled");
      }
    }
    executorService.shutdown();
    return contributions;
  }
  
  /**
   * Converts a map of tag-parent relationships into a list of Callable instances.
   * Each Callable computes the difference between two tags and produces a {@link ContributionsOfTag} object.
   *
   * @param tagParentMap A map representing tag-parent relationships.
   * @return An ArrayList containing Callable instances for computing tag differences.
   */
  private ArrayList<Callable<ContributionsOfTag>> toCallables(Map<RevCommit, RevCommit> tagParentMap) {
    var callables = new ArrayList<Callable<ContributionsOfTag>>();
    for (var entry : tagParentMap.entrySet()) {
      var parent = entry.getValue();
      var commit = entry.getKey();
      callables.add(() -> {
        Map<String, Map<String, Integer>> contributions;
        if (isCanceled.get()) {
          contributions = Map.of();
        } else {
          contributions = diffBetweenTwoTags(commit.getName(),
              parent != null ? parent.getName() : null);
          tagsAnalyzed.incrementAndGet();
        }
        return new ContributionsOfTag(commit, parent, contributions);
      });
    }
    return callables;
  }
  
  /**
   * Calculates the difference between two Git tags, considering file modifications and additions.
   * <p>
   * Handles interruption
   *
   * @param newTagSHA1 The SHA1 hash of the new Git tag.
   * @param oldTagSHA1 The SHA1 hash of the old Git tag (can be null for the initial commit).
   * @return A map representing contributions by author, file type, and count.
   * @throws IOException     If an I/O error occurs.
   * @throws GitAPIException If an error occurs during Git operations.
   */
  private Map<String, Map<String, Integer>> diffBetweenTwoTags(String newTagSHA1, String oldTagSHA1) throws IOException, GitAPIException {
    var newTagRevCommit = repository.parseCommit(repository.resolve(newTagSHA1));
    var newTagTree = getTreeIterator(newTagSHA1);
    var oldTagTree = oldTagSHA1 != null ? getTreeIterator(oldTagSHA1) : new EmptyTreeIterator();
    Map<String, Map<String, Integer>> contributions = new HashMap<>();
    try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
      diffFormatter.setRepository(repository);
      diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
      diffFormatter.setDetectRenames(true);
      List<DiffEntry> diffs = diffFormatter.scan(oldTagTree, newTagTree);
      // Critical blocking section : parsing of all files
      // must handle interruption
      for (DiffEntry entry : diffs) {
        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY || entry.getChangeType() == DiffEntry.ChangeType.ADD) {
          if (isCanceled.get()) {
            contributions = Map.of();
            break;
          }
          var fileContributions = contributionsOnFile(newTagRevCommit, diffFormatter, entry);
          mergeContributions(contributions, fileContributions);
        }
      }
    }
    return contributions;
  }
  
  /**
   * Merges contributions from a new set into the main contributions map.
   *
   * @param mainContributions The main contributions map.
   * @param newContributions  The new contributions to be merged.
   */
  private void mergeContributions(Map<String, Map<String, Integer>> mainContributions,
                                  Map<String, Map<String, Integer>> newContributions) {
    newContributions.forEach((author, fileContributions) ->
        mainContributions.merge(author, fileContributions, (existingContributions, additionalContributions) -> {
          additionalContributions.forEach((type, count) ->
              existingContributions.merge(type, count, Integer::sum));
          return existingContributions;
        })
    );
  }
  
  /**
   * Get the set of all lines indices the that have been modified or added in the given diff Entry
   *
   * @return
   */
  private Set<Integer> triggeredLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
    Set<Integer> indices = new HashSet<>();
    if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY || entry.getChangeType() == DiffEntry.ChangeType.ADD) {
      var fileHeader = diffFormatter.toFileHeader(entry);
      // Extract edits for the modified file
      EditList editList = fileHeader.toEditList();
      for (Edit edit : editList) {
        if (edit.getType() == Edit.Type.INSERT || edit.getType() == Edit.Type.REPLACE) {
          for (int i = edit.getBeginB(); i < edit.getEndB(); i++) {
            indices.add(i);
          }
        }
      }
    }
    return indices;
  }
  
  /**
   * Calculates contributions based on a BlameResult, a set of line indices, and a map of extractions.
   * Contributions are grouped by author and summed for each extracted value.
   *
   * @param blame       The BlameResult containing information about source code authors.
   * @param indices     The set of line indices to consider for contributions.
   * @param extractions A map of line indices to extracted values (e.g., code or comment lines).
   * @return A map representing contributions, where the outer map key is the author name and the inner map
   *         represents contributions for each extracted value.
   */
  private Map<String, Map<String, Integer>> calculateContributions(BlameResult blame, Set<Integer> indices,
                                                                   Map<Integer, String> extractions) {
    return indices.stream()
                  .filter(extractions::containsKey)
                  .collect(groupingBy(
                      index -> blame == null ? "unspecified-user" : blame.getSourceAuthor(index).getName(),
                      toMap(
                          extractions::get,
                          count -> 1,
                          Integer::sum,
                          HashMap::new
                      )
                  ));
  }
  
  /**
   * Analyzes contributions on a specific file for a given commit.
   * Contributions are calculated for code and comment lines based on the provided DiffFormatter and entry.
   *
   * @param commit         The commit for which contributions are analyzed.
   * @param diffFormatter  The DiffFormatter for obtaining the differences in the file.
   * @param entry          The DiffEntry representing changes in the file.
   * @return A map representing contributions, where the outer map key is the author name and the inner map
   *         represents contributions for each extracted value (e.g., code or comment lines).
   * @throws IOException      If an I/O error occurs.
   * @throws GitAPIException  If an error occurs during Git API operations.
   */
  private Map<String, Map<String, Integer>> contributionsOnFile(RevCommit commit, DiffFormatter diffFormatter,
                                                                DiffEntry entry) throws IOException, GitAPIException {
    var filePath = entry.getNewPath();
    var guessedExtension = fileTypes.typeFromFileName(filePath.toLowerCase(Locale.ENGLISH));
    if (guessedExtension.isEmpty()) {
      return Map.of();
    }
    var fileType = guessedExtension.orElseThrow();
    var pathRelativeToCommit = repository.resolve(commit.getId().getName() + ":" + filePath);
    var blame = git.blame()
                   .setFilePath(entry.getNewPath())
                   .setStartCommit(commit)
                   .setFollowFileRenames(true)
                   .call();
    var indices = triggeredLines(diffFormatter, entry);
    try (var inputStream = repository.open(pathRelativeToCommit).openStream()) {
      var extractions = parser.parseInputStreamWith(inputStream, fileType);
      // Group by author and sum contributions for code and comment lines
      return calculateContributions(blame, indices, extractions);
    }
  }
  
  private static void deleteLocalTags(Git git, List<Ref> tags) throws GitAPIException {
    for (Ref tag : tags) {
      String tagName = tag.getName();
      // Delete each tag
      git.tagDelete().setTags(tagName).call();
    }
  }
  
  /**
   * Updates an existing repository with the latest changes from the remote repository.
   * Modifies the provided RepositoryModel instance to reflect the updated state.
   *
   * @param repositoryToBeUpdated The RepositoryModel instance representing the repository to be updated.
   * @return The updated RepositoryModel instance after applying the changes.
   * @throws IOException     If the analysis fails.
   */
  private RepositoryModel updateRepository(RepositoryModel repositoryToBeUpdated) throws IOException {
    Objects.requireNonNull(repositoryToBeUpdated, "The repository can't be null");
    try (Git gitRepo = Git.open(new File(repositoryToBeUpdated.path()))) {
      git = gitRepo;
      repository = git.getRepository();
      var oldTagList = git.tagList().call();
      var oldTagParentMap = getTagParentMap(repository, oldTagList);
      // Pull the latest changes from the remote repository
      deleteLocalTags(git, oldTagList);
      FetchResult result = git.fetch().setCheckFetchedObjects(true).call();
      var newTagList = git.tagList().call();
      var newTagParentMap = getTagParentMap(repository, newTagList);
      // Delete ------ update relationship
      var tagList = new ArrayList<>(repositoryToBeUpdated.tags());
      updateProcessTagRemoval(oldTagParentMap, newTagParentMap, tagList);
      // ---- add
      var toBeAnalyzed = updateProcessTagAddition(oldTagParentMap, newTagParentMap, tagList);
      // analyze this all in executors
      totalTags = toBeAnalyzed.size();
      var newRepo = runAnalysis(toBeAnalyzed);
      var oldTags = tagListToMapById(tagList);
      var newTags = tagListToMapById(newRepo.tags());
      newTags.putAll(oldTags);
      return repositoryToBeUpdated.withTags(newTags.values().stream().toList());
    } catch (GitAPIException e) {
      throw new IOException(e);
    }
  }
  
  /**
   * has side effects on tagList
   */
  private void updateProcessTagRemoval(Map<RevCommit, RevCommit> oldTagParentMap,
                                       Map<RevCommit, RevCommit> newTagParentMap, List<Tag> tagList)
      throws GitAPIException, IOException {
    for (var rel : findChangedTags(oldTagParentMap, newTagParentMap).entrySet()) {
      // get sha1 of parent
      var currentSha1 = rel.getKey();
      // get sha1 of commit to remove
      var parentSha1 = rel.getValue();
      // get sha1 of child of commit to remove
      var childSha1Opt = oldTagParentMap.entrySet().stream()
                                        .filter(entry ->
                                            entry.getValue() != null && entry.getValue().equals(currentSha1))
                                        .map(Map.Entry::getKey).findFirst();
      childSha1Opt.ifPresent(childSha1 -> {
        // get the associated commit / aka Tag in repo
        var optTag = tagList.stream().filter(tag -> tag.id().equals(childSha1.getName())).findFirst();
        // update its parent sha1 reference
        optTag.ifPresent(tag -> {
          tagList.removeIf(tagToDel -> tagToDel.id().equals(childSha1.getName()));
          var newCurrentTag = tag.withParent(parentSha1 == null ? Optional.empty(): Optional.of(parentSha1.getName()));
          tagList.add(newCurrentTag);
        });
      });
      tagList.removeIf(t -> t.id().equals(currentSha1.getName()));
    }
  }
  
  /**
   * has side effects on tagList
   * @return map of commit/parent contributions to be analyzed
   */
  private Map<RevCommit, RevCommit> updateProcessTagAddition(Map<RevCommit, RevCommit> oldTagParentMap,
                                                             Map<RevCommit, RevCommit> newTagParentMap, List<Tag> tagList)
      throws GitAPIException, IOException {
    tagsOfCommits = mapTagsToCommit(git);
    var toBeAnalyzed = new HashMap<RevCommit, RevCommit>();
    for (var rel : findChangedTags(newTagParentMap, oldTagParentMap).entrySet()) {
      var currentCommit = rel.getKey();
      var commitInOldRepo = tagList.stream().filter(tag -> tag.id().equals(currentCommit.getName())).findFirst();
      if (commitInOldRepo.isPresent()) {
        // case 1 - commit exists in old map (already analyzed)
        var newNames = getAllTagNames(currentCommit);
        var tag = commitInOldRepo.orElseThrow();
        var newCurrentTag = tag.withName(newNames);
        tagList.remove(tag);
        tagList.add(newCurrentTag);
      } else {
        // case 2 - just analyze the tag
        var parent = rel.getValue();
        toBeAnalyzed.put(currentCommit, parent);
        var child = tagList.stream().filter(tag -> tag.id().equals(currentCommit.getName())).findFirst();
        // update its parent sha1 reference
        child.ifPresent(tag -> {
          tagList.removeIf(tagToDel -> tagToDel.id().equals(tag.id()));
          var newCurrentTag = tag.withParent(Optional.of(currentCommit.getName()));
          tagList.add(newCurrentTag);
        });
      }
    }
    return toBeAnalyzed;
  }
  
  
  private HashMap<String, Tag> tagListToMapById(List<Tag> tags){
    return tags.stream().collect(Collectors.toMap(Tag::id, a -> a, (a, b) -> a, HashMap::new));
  }
  
  /**
   * Compares two maps representing tag-parent relationships and identifies tags that are present in one map but not in the other.
   *
   * @param mapA The first map representing tag-parent relationships.
   * @param mapB The second map representing tag-parent relationships.
   * @return A map containing tags that were present in the first map but have been deleted in the second map,
   *         and tags that are present in the second map but were not in the first map.
   *         The map's key represents the commit, and the value represents the tag's parent commit.
   */
  private Map<RevCommit, RevCommit> findChangedTags(Map<RevCommit, RevCommit> mapA,
                                                    Map<RevCommit, RevCommit> mapB) {
    Map<RevCommit, RevCommit> changedTags = new HashMap<>();
    for (var entry : mapA.entrySet()) {
      var commit = entry.getKey();
      if (!mapB.containsKey(commit)) {
        changedTags.put(commit, entry.getValue());
      }
    }
    return changedTags;
  }
}
