package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerEntryWithMd5(var cefMd5: String? = null) : ContainerEntry()

