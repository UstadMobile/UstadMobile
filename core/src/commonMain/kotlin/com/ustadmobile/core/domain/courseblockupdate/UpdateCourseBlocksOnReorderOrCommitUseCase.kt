package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.entities.CourseBlockWithEntity

class UpdateCourseBlocksOnReorderOrCommitUseCase {

    operator fun invoke(
        currentList: List<CourseBlockWithEntity>,
        autoIndentIndex: Int = -1,
    ): List<CourseBlockWithEntity> {
        val listIndented = if(autoIndentIndex >= 0) {
            currentList.autoIndent(autoIndentIndex)
        }else {
            currentList
        }

        return listIndented.updateParentModuleUidsAndIndex()
    }

}