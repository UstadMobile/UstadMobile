package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.core.util.ext.asCourseBlockWithEntity
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity

/**
 * Handle when a new course block is added to the list, or when an existing course block has been
 * updated (e.g. when the user returns to ClazzEdit after editing a courseblock).
 *
 * If this is a new item being added to the list, the parent block module id will be set and the
 * indent level will be set.
 */
class AddOrUpdateCourseBlockUseCase {

    operator fun invoke(
        currentList: List<CourseBlockWithEntity>,
        clazzUid: Long,
        addOrUpdateBlock: CourseBlock,
        assignment: ClazzAssignment? = null,
        assignmentPeerReviewAllocations: List<PeerReviewerAllocation>?,
    ) : List<CourseBlockWithEntity> {
        val courseBlockWithEntity = addOrUpdateBlock.asCourseBlockWithEntity(
            assignment = assignment,
            assignmentPeerReviewAllocations = assignmentPeerReviewAllocations,
        )
        val currentIndex = currentList.indexOfFirst {
            it.cbUid == addOrUpdateBlock.cbUid
        }

        val courseBlockMutableList = currentList.toMutableList()
        val newCourseBlockList = if(currentIndex >= 0) {
            courseBlockMutableList.apply {
                this[currentIndex] = courseBlockWithEntity
            }
        }else {
            courseBlockMutableList.apply {
                add(courseBlockWithEntity.shallowCopyWithEntity {
                    cbClazzUid = clazzUid
                })
            }
            .autoIndent(courseBlockMutableList.size - 1)
            .updateParentModuleUidsAndIndex()
        }

        return newCourseBlockList.toList()
    }

}