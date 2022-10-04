package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class ContainerManifest(

    var container: Container? = null,

    var entryMap: Map<String,List<String>>? = null

)