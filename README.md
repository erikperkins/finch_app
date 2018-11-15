# Cloudberry [![Build Status](https://travis-ci.com/erikperkins/cloudberry.svg?branch=master)](https://travis-ci.com/erikperkins/cloudberry) [![Coverage Status](https://coveralls.io/repos/github/erikperkins/cloudberry/badge.svg?branch=master)](https://coveralls.io/github/erikperkins/cloudberry?branch=master)

## Build

### Packaging
In Intellij Idea, do

File -> Project Structure -> Artifacts -> + -> JAR -> From modules with dependencies...
=> Module <app name>
=> Main class <main class>
=> Extract to the target JAR

### Starting in bash
From the project root, do
```
$ java -Xms16m Xmx32m -jar out/artifacts/<app name>_jar/<app name>.jar
```

### Starting in Docker
```
$ docker run --rm -d -p 8000:8000 erikperkins/cloudberry
```

## Test
### IntelliJ Idea
Add a test configuration from the `Edit Configurations` option in the job menu
in the upper right. Use a `ScalaTest` template, and select the test class to be
run. Save the test configuration by clicking `OK` or Apply`, then click the
green play arrow to the right of the job menu.

### Travis CI
```
$ sbt ++$TRAVIS_SCALA_VERSION test
```
