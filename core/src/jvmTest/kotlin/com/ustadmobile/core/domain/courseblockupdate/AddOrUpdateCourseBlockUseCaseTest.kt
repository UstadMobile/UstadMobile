package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlin.test.Test
import kotlin.test.assertEquals

class AddOrUpdateCourseBlockUseCaseTest {

    @Test
    fun givenCourseBlockListEndingWithModule_whenBlockAdded_thenShouldBeIndentedWithParentModuleSet() {
        val existingList = listOf(
            CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbUid = 1
                    cbClazzUid = 1
                    cbType = CourseBlock.BLOCK_MODULE_TYPE
                }
            )
        )

        val newList = AddOrUpdateCourseBlockUseCase().invoke(
            currentList = existingList,
            addOrUpdateBlock = CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbUid = 2
                    cbClazzUid = 1
                    cbType = CourseBlock.BLOCK_TEXT_TYPE
                }
            ),
        )

        assertEquals(2, newList.last().courseBlock.cbUid)
        assertEquals(1, newList.last().courseBlock.cbClazzUid)
        assertEquals(1, newList.last().courseBlock.cbIndentLevel)
        assertEquals(existingList.first().courseBlock.cbUid, newList.last().courseBlock.cbModuleParentBlockUid)
    }

    @Test
    fun givenCourseBlockListEndingWithItemInModule_whenBlockAdded_thenShouldMatchIndent() {
        val existingList = listOf(
            CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbUid = 1
                    cbType = CourseBlock.BLOCK_MODULE_TYPE
                    cbClazzUid = 1
                }
            ),
            CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbUid = 2
                    cbType = CourseBlock.BLOCK_TEXT_TYPE
                    cbModuleParentBlockUid = 1
                    cbIndentLevel = 2
                    cbClazzUid = 1
                }
            )
        )

        val newList = AddOrUpdateCourseBlockUseCase().invoke(
            currentList = existingList,
            addOrUpdateBlock = CourseBlockAndEditEntities(
                courseBlock = CourseBlock().apply {
                    cbUid = 3
                    cbType = CourseBlock.BLOCK_TEXT_TYPE
                    cbClazzUid = 1
                }
            )
        )

        assertEquals(3, newList.last().courseBlock.cbUid)
        assertEquals(1, newList.last().courseBlock.cbClazzUid)
        assertEquals(existingList.last().courseBlock.cbIndentLevel, newList.last().courseBlock.cbIndentLevel)
        assertEquals(existingList.first().courseBlock.cbUid, newList.last().courseBlock.cbModuleParentBlockUid)
    }

}