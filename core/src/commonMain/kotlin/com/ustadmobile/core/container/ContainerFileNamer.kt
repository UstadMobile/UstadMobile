package com.ustadmobile.core.container

interface ContainerFileNamer {

    /**
     * Determine the name of the file once it is added to the container.
     *
     * @param relPathIn the relative path in. When a directory or zip file is being added, this is
     * the relative path
     *
     * @param uriIn the full uri of the file being added
     *
     * @return The name of the file as it should be inside the container. There should be no leading
     * slash. Null to indicate the file should not be added to the container at all.
     */
    fun nameContainerFile(relPathIn: String, uriIn: String): String?

}