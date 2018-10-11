# lib-database-entity-processor

Room Persistence Framework requires entity POJOs to have specific
annotation, but that annotation is not portable. We thus have to use
our own annotation. lib-database-entity-processor will use Roaster to
parse the POJOs and add the required room annotations, and then output
the generated source into the lib-database-android project.

This module contains the java entity objects (POJOs) used for
the database.

