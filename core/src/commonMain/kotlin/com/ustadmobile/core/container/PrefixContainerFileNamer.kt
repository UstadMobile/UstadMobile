package com.ustadmobile.core.container

/**
 * Simuple ContainerFileNamer that will apply a prefix to each name
 */
class PrefixContainerFileNamer(val prefix: String): ContainerFileNamer {

    override fun nameContainerFile(relPathIn: String, uriIn: String) = "$prefix$relPathIn"

}