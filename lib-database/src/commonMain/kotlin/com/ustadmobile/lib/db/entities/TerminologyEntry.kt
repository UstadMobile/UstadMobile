package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class TerminologyEntry(val id: String, val messageId: Int, var term: String?, var errorMessage: String? = null)