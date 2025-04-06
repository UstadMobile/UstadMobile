package com.ustadmobile.lib.db.entities.respect

import androidx.room.Embedded

data class RespectLessonAndApp(
    @Embedded
    var lesson: RespectLesson = RespectLesson(),
    @Embedded
    var app: RespectApp = RespectApp()
)
