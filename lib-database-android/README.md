# lib-database-android

This module is separated out to avoid using the annotation processor
on the main app-android module, as the room annotation processor does
not support incremental annotation processing.

It contains entities (generated from the lib-entities module by
the lib-database-entity-processor module) and the database and DAO
classes (generated from lib-database by
lib-database-annotation-processor-core).
