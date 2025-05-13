package com.ustadmobile.core.domain.credentials

import com.ustadmobile.lib.db.entities.Person
import io.ktor.http.Url
import com.ustadmobile.core.util.ext.formattedHost

data class CreatePasskeyParams(
    val username: String,
)
