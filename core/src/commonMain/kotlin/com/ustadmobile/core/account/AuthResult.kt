package com.ustadmobile.core.account

import com.ustadmobile.lib.db.entities.Person

data class AuthResult(val authenticatedPerson: Person? = null, val success: Boolean = false)