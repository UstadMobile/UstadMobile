package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContainerEntryWithMd5(var cefMd5: String? = null) : ContainerEntry()

@Serializable
data class ContainerWithContainerEntryWithMd5(val container: Container, val containerEntries: List<ContainerEntryWithMd5>)

