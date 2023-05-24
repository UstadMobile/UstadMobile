package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class AddOrUpdateCourseBlockUseCaseTest {

    @Test
    fun givenCourseBlockListEndingWithModule_whenBlockAdded_thenShouldBeIndentedWithParentModuleSet() {
        val existingList = listOf(
            CourseBlockWithEntity().apply {
                cbUid = 1
                cbType = CourseBlock.BLOCK_MODULE_TYPE
            }
        )

        val newList = AddOrUpdateCourseBlockUseCase().invoke(
            existingList, 1, CourseBlock().apply {
                cbUid = 2
                cbType = CourseBlock.BLOCK_TEXT_TYPE
            }
        )

        assertEquals(2, newList.last().cbUid)
        assertEquals(1, newList.last().cbClazzUid)
        assertEquals(1, newList.last().cbIndentLevel)
        assertEquals(existingList.first().cbUid, newList.last().cbModuleParentBlockUid)
    }

    @Test
    fun givenCourseBlockListEndingWithItemInModule_whenBlockAdded_thenShouldMatchIndent() {
        val existingList = listOf(
            CourseBlockWithEntity().apply {
                cbUid = 1
                cbType = CourseBlock.BLOCK_MODULE_TYPE
            },
            CourseBlockWithEntity().apply {
                cbUid = 2
                cbType = CourseBlock.BLOCK_TEXT_TYPE
                cbModuleParentBlockUid = 1
                cbIndentLevel = 2
            }
        )

        val newList = AddOrUpdateCourseBlockUseCase().invoke(
            existingList, 1, CourseBlock().apply {
                cbUid = 3
                cbType = CourseBlock.BLOCK_TEXT_TYPE
            }
        )

        assertEquals(3, newList.last().cbUid)
        assertEquals(1, newList.last().cbClazzUid)
        assertEquals(existingList.last().cbIndentLevel, newList.last().cbIndentLevel)
        assertEquals(existingList.first().cbUid, newList.last().cbModuleParentBlockUid)
    }

}