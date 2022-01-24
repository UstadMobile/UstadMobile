package com.ustadmobile.core.network.containerfetcher

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

class ContainerFetcherRequest2(
        /**
         * List of the ContainerEntryFiles that need to be downloaded. This must include the
         * containerentryfile object so that the md5 is available.
         */
        val entriesToDownload: List<ContainerEntryWithMd5>,

        /**
         * The endpoint of the site
         */
        val siteUrl: String,

        /**
         * The endpoint of the mirror to use. If downloading from the site itself, then siteUrl = mirrorUrl
         */
        val mirrorUrl: String,

        /**
         * The destination directory uri. This MUST be a URI, not a folder path. At the moment
         * this is always a local file directory, but in future other storage mechanisms might be
         * used. For a directory on JVM/Android use the File.toKmpUriString function.
         */
        val destDirUri: String
)