package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

data class ContainerUploaderRequest2(
    val uploadUuid: String,
     /**
     * List of the ContainerEntryFiles that need to be uploaded. This must include the
     * containerentryfile object so that the md5 is available.
     */
    val entriesToUpload: List<ContainerEntryWithMd5>,

     /**
      * The endpoint of the site
      */
     val siteUrl: String
 )

