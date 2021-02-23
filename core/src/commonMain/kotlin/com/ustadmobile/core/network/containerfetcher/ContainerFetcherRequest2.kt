package com.ustadmobile.core.network.containerfetcher

class ContainerFetcherRequest2(
        /**
         * List of the MD5Sums that should be downloaded as a string of the md5 hex separated by ;
         * e.g.
         * d288df22a4d74bade392dd61766aab55;322e2c67c38104012a7a22bd1ca024f8
         */
        val md5list: String,

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