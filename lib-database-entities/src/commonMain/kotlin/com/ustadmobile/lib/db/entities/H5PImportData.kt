package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class H5PImportData(val contentEntry: ContentEntry, val container: Container, val parentChildJoin: ContentEntryParentChildJoin)