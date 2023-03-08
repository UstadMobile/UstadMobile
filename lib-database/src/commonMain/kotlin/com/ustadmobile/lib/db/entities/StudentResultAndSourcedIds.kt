package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

data class StudentResultAndSourcedIds(
    @Embedded
    var studentResult: StudentResult = StudentResult(),

    var cbSourcedId: String? = null,
)

