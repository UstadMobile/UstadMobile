package com.ustadmobile.core.username

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.filterusername.FilterUsernameUseCase
import com.ustadmobile.core.username.helper.UsernameErrorException


class UsernameSuggestionUseCase(
    private val filterUsernameUseCase: FilterUsernameUseCase,
    private val db: UmAppDatabase,
) {

    companion object {
        private const val MAX_ATTEMPTS = 1000
    }

    operator fun invoke(name: String): String {
        val personDao = db.personDao()
        val baseUsername = filterUsernameUseCase(name, "")
        var suggestedUsername: String

        for (index in 0 until MAX_ATTEMPTS) {
            suggestedUsername = if (index == 0) baseUsername else "$baseUsername$index"
            if (personDao.findByUsername(suggestedUsername) == null) {
                return suggestedUsername
            }
        }

        throw UsernameErrorException("Unable to generate a unique username")
    }
}
