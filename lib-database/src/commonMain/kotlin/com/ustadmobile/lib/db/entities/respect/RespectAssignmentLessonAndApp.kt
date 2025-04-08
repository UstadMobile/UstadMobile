package com.ustadmobile.lib.db.entities.respect

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class RespectAssignmentLessonAndApp(
    @Embedded
    var app: RespectApp = RespectApp(),
    @Embedded
    var lesson: RespectLesson = RespectLesson(),
    @Embedded
    var assignment: RespectAssignment = RespectAssignment(),
)
