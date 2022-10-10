package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerFilesWithContentEntry {

    var containerUid: Long = 0L

    var containerEntryFileUid: Long = 0L

    var containerEntryFilePath: String? = null

    var contentEntryTitle: String? = null

    var contentEntryDesc: String? = null

    var contentEntryId: String? = null

}