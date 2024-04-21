package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import com.ustadmobile.lib.db.entities.Language
import kotlinx.serialization.Serializable

@Serializable
data class CourseBlockAndDbEntities(
    @Embedded
    var courseBlock: CourseBlock? = null,

    @Embedded
    var courseBlockPicture: CourseBlockPicture? = null,

    @Embedded
    var contentEntry: ContentEntry? = null,

    @Embedded
    var contentEntryLang: Language? = null,

    @Embedded
    var assignment: ClazzAssignment? = null,

    var assignmentCourseGroupSetName: String? = null,

)