package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class CourseBlockWithCompleteEntity : CourseBlock() {

    @Embedded
    var assignment: ClazzAssignmentWithMetrics? = null

    @Embedded
    var entry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer? = null

    @Embedded
    var courseDiscussion: CourseDiscussion? = null

    var expanded: Boolean = true


}