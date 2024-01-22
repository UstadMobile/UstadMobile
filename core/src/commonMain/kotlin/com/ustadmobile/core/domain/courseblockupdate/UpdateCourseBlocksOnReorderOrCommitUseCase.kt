package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities

class UpdateCourseBlocksOnReorderOrCommitUseCase {

    operator fun invoke(
        currentList: List<CourseBlockAndEditEntities>,
        autoIndentIndex: Int = -1,
    ): List<CourseBlockAndEditEntities> {
        val listIndented = if(autoIndentIndex >= 0) {
            currentList.autoIndent(autoIndentIndex)
        }else {
            currentList
        }

        return listIndented.updateParentModuleUidsAndIndex()
    }

}