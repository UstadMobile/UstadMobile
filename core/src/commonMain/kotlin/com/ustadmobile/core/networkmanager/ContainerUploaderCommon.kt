package com.ustadmobile.core.networkmanager

import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.coroutines.Deferred

data class ContainerUploaderRequest(val uploadJobUid: Long, val fileList: String, val uploadToUrl: String, val endpointUrl: String)

data class ContainerUploaderRequest2(val uploadUuid: String,
                                     /**
                                     * List of the ContainerEntryFiles that need to be uploaded. This must include the
                                     * containerentryfile object so that the md5 is available.
                                     */
                                    val entriesToUpload: List<ContainerEntryWithMd5>,

                                     /**
                                      * The endpoint of the site
                                      */
                                     val siteUrl: String)


abstract class ContainerUploaderCommon() {

    abstract suspend fun enqueue(request: ContainerUploaderRequest): Deferred<Int>

}