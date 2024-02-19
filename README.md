# GitClout

## The project
GitClout is a web app that analyzes a specified Git repository (e.g. github or gitlab) to offers insights into the contributions made by each contributor.

The back-end uses Java with Spring Boot 3 with WebMVC to serve a REST API and the static pages.

The front-end uses Angular 17 and tailwind.

***Ongoing re-implementation of the original [GitClout app](https://gitlab.com/4nt0ineB/sebbah-bastos),*** with new frameworks.

### Run the app
```sh
java --enable-preview -jar gitclout-1.0.0.jar 
```

At start, the address of the web main page will be printed out as well as the swagger page address of the OPEN API file, for example :
```shell
WEB server is up! http://localhost:8080
OPEN API http://localhost:8080/openapi/ui
```

#### Configuration

You can override some default properties with inline options at app start


| Name                          | default         | comment                                                                                                                                 |
|-------------------------------|-----------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| --server.port                 | 8080            |                                                                                                                                         |
| --app.analysisPoolSize        | 2               |                                                                                                                              
| --app.analysisThreadPoolSize  | 3               |                                                                                                                                         |
| --app.data                    | .gitclout-data/ | A directory is created <br>at the current working directory, <br> containing the app’s data, <br>cloned repositories and database, <br> |





### Programming language support

As of today GitClout app v1.0.0 supports these programming languages,
and other types of contributions in a given git repository :


| Category | Type                                                                                                          | 
|----------|---------------------------------------------------------------------------------------------------------------|
| CODE     | c, java, html, css, python, ruby, <br/>javascript, typescript, ocaml, php, jasm                               |
| CONFIG   | git, yaml,                                                                                                    |
| DOC      | markdown, documentation in code (javadoc, docstring, etc.. any extracted doc in code file is undistinguished) |
| BUILD    | makefile, maven, gradle                                                                                       |


### Credits

Many thanks to Dominik Stadler's jgit-cookbook repository and Rüdiger Herrmann for his post on Diffs with JGit
that allowed to better understand and deal with the JGit API.<br>

https://github.com/centic9/jgit-cookbook <br>
https://www.codeaffine.com/2016/06/16/jgit-diff/
