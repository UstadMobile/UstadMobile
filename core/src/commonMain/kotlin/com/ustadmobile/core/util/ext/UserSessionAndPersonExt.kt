package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UserSessionWithPersonAndLearningSpace
import com.ustadmobile.lib.db.entities.UserSessionAndPerson

fun UserSessionAndPerson.withLearningSpace(learningSpace: LearningSpace) =
    UserSessionWithPersonAndLearningSpace(
        userSession ?: throw IllegalArgumentException("session withendpoint : usersession must not be null"),
        person ?: throw IllegalArgumentException("session withendpoint: person msut not be null"),
        learningSpace
    )