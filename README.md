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
