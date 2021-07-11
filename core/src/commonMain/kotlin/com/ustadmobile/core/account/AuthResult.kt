package com.ustadmobile.core.account

import com.ustadmobile.lib.db.entities.Person
import kotlinx.serialization.Serializable

/**
 * Represents the results of authentication
 */
@Serializable
data class AuthResult(
    val authenticatedPerson: Person? = null,
    val success: Boolean = false,
    val reason: Int = 0
) {
    companion object {

        const val REASON_NEEDS_CONSENT = 1

    }
}