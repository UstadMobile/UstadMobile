package com.ustadmobile.core.network.containerfetcher

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

class ContainerFetcherRequest2(
        /**
         * List of the ContainerEntryFiles that need to be downloaded. This must include the
         * containerentryfile object so that the md5 is available.
         *
         * d288df22a4d74bade392dd61766aab55;322e2c67c38104012a7a22bd1ca024f8
         */
        val entriesToDownload: List<ContainerEntryWithMd5>,

        /**
         * The url of the site to download from (as per the endpoint url)
         */
        val siteUrl: String,

        /**
         * The destination directory uri. This MUST be a URI, not a folder path. At the moment
         * this is always a local file directory, but in future other storage mechanisms might be
         * used. For a directory on JVM/Android use the File.toKmpUriString function.
         */
        val destDirUri: String) {
}