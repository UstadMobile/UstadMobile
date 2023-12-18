package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import kotlinx.serialization.Serializable

/**
 * CourseBlock and all associated entities that are used as part of the editing process within
 * ClazzEdit
 */
@Serializable
data class CourseBlockAndEditEntities(
    val courseBlock: CourseBlock,

    val contentEntry: ContentEntry? = null,

    //If content has been selected for import e.g. by link or file upload, then the parameters
    // e.g. pluginId, compression settings, are stored here.
    val contentJobItem: ContentJobItem? = null,

    val contentJob: ContentJob? = null,

    val contentEntryLang: Language? = null,

    val assignment: ClazzAssignment? = null,

    val assignmentCourseGroupSetName: String? = null,

    val assignmentPeerAllocations: List<PeerReviewerAllocation> = emptyList(),
)
