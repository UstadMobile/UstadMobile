package com.ustadmobile.core.impl

/**
 * Represents a request to mount a container (e.g. epub, xapi package, etc).
 */
class ContainerMountRequest
/**
 * Create a new mount request
 *
 * @param containerUri The file to be mounted
 * @param epubMode true if the file is an epub and will have scripts injected for pagination etc
 */
(
        /**
         *
         * @return
         */
        var containerUri: String?, var isEpubMode: Boolean)
