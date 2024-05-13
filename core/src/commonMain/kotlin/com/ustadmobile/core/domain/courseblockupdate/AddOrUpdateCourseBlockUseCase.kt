package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities

/**
 * Handle when a new course block is added to the list, or when an existing course block has been
 * updated (e.g. when the user returns to ClazzEdit after editing a courseblock).
 *
 * If this is a new item being added to the list, the parent block module id will be set and the
 * indent level will be set.
 */
class AddOrUpdateCourseBlockUseCase {

    operator fun invoke(
        currentList: List<CourseBlockAndEditEntities>,
        clazzUid: Long,
        addOrUpdateBlock: CourseBlockAndEditEntities,
    ) : List<CourseBlockAndEditEntities> {
        val currentIndex = currentList.indexOfFirst {
            it.courseBlock.cbUid == addOrUpdateBlock.courseBlock.cbUid
        }

        val courseBlockMutableList = currentList.toMutableList()
        val newCourseBlockList = if(currentIndex >= 0) {
            courseBlockMutableList.apply {
                this[currentIndex] = addOrUpdateBlock
            }
        }else {
            courseBlockMutableList.add(
                addOrUpdateBlock.copy(
                    courseBlock = addOrUpdateBlock.courseBlock.copy(
                        cbClazzUid = clazzUid
                    )
                )
            )
            courseBlockMutableList.autoIndent(courseBlockMutableList.size - 1)
                .updateParentModuleUidsAndIndex()
        }

        return newCourseBlockList.toList()
    }

}