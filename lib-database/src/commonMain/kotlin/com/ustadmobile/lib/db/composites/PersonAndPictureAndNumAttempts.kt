package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndPictureAndNumAttempts(
    @Embedded
    var person: Person = Person(),
    @Embedded
    var picture: PersonPicture? = null,
    var numAttempts: Int = 0,
    var isCompleted: Boolean = false,
    var isSuccessful: Boolean? = null,
    var maxScore: Float? = null,
    var maxProgress: Int? = null,
)


object AttemptsPersonListConst
{
    const val SORT_FIRST_NAME_ASC = 1

    const val SORT_FIRST_NAME_DESC = 2

    const val SORT_LAST_NAME_ASC = 3

    const val SORT_LAST_NAME_DESC = 4


    const val SORT_BY_SCORE_ASC = 5
    const val SORT_BY_SCORE_DESC = 6

    const val SORT_BY_COMPLETION_ASC = 7
    const val SORT_BY_COMPLETION_DESC = 8

    const val SORT_BY_RECENT_ATTEMPT_ASC = 9
    const val SORT_BY_RECENT_ATTEMPT_DESC = 10

}