# Data Access Object (DAO) Annotation Processor

The annotation processor is designed to automate the creation of room persistence style Data Access
Objects (DAOs).

## Debugging the annotation processor using IntelliJ

In gradle.properties, uncomment the lines:
```
#org.gradle.daemon=true
#org.gradle.jvmargs=-Xmx3072m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006
```

In IntelliJ select Run, Debug..., Add a remote configuration, and 
enter the por as per org.gradle.jvmargs (e.g. 5006)


