package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
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

    val courseBlockPicture: CourseBlockPicture? = null,

    val contentEntry: ContentEntry? = null,

    val contentEntryPicture: ContentEntryPicture2? = null,

    //If content has been selected for import e.g. by link or file upload, then the parameters
    // e.g. pluginId, compression settings, are stored here.
    val contentJobItem: ContentEntryImportJob? = null,

    val contentJob: ContentJob? = null,

    val contentEntryLang: Language? = null,

    val assignment: ClazzAssignment? = null,

    val assignmentCourseGroupSetName: String? = null,

    val assignmentPeerAllocations: List<PeerReviewerAllocation> = emptyList(),
) {

    fun asContentEntryAndJob(): ContentEntryAndContentJob = ContentEntryAndContentJob(
        entry = contentEntry,
        contentJob = contentJob,
        contentJobItem = contentJobItem,
        picture = contentEntryPicture,
    )

}
