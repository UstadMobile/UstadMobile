package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity

fun List<CourseBlock>.findParentModule(
    courseBlock: CourseBlock
): Long {
    //Modules don't have parent modules
    if(courseBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        return 0

    val index = indexOfFirst { it.cbUid == courseBlock.cbUid }
    val prevCourseBlock = getOrNull(index - 1) ?: return 0

    return if(prevCourseBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        prevCourseBlock.cbUid
    else if(prevCourseBlock.cbIndentLevel >= 1)
        prevCourseBlock.cbModuleParentBlockUid
    else
        0
}

/**
 * Update the parent module uid and index on all items in the list. The parent module is the
 * previous items module if it is either a module itself, or the same as the previous item if the
 * indent >= 1
 */
fun List<CourseBlockWithEntity>.updateParentModuleUidsAndIndex(): List<CourseBlockWithEntity> {
    return toMutableList().mapIndexed { index, block ->
        val parentModUid = findParentModule(block)
        if(block.cbModuleParentBlockUid != parentModUid || block.cbIndex != index){
            block.shallowCopyWithEntity {
                cbModuleParentBlockUid = parentModUid
                cbIndex = index
            }
        }else {
            block
        }
    }.toList()
}

/**
 * Auto indent the given course block. Rules are:
 * 1. If course block is module, don't indent
 * 2. If previous course block is a module, then indent = 1
 * 3. If previous course block is indented, then indent = same as previous block
 */
fun List<CourseBlockWithEntity>.autoIndent(
    index: Int
) : List<CourseBlockWithEntity> {
    //Do not indent a module block
    val block = this[index]
    if(block.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        return this

    val prevBlock = getOrNull(index - 1) ?: return this
    val autoIndent = if(prevBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
        1
    }else {
        prevBlock.cbIndentLevel
    }

    return if(block.cbIndentLevel != autoIndent) {
        toMutableList().apply {
            this[index] = block.shallowCopyWithEntity { cbIndentLevel = autoIndent }
        }.toList()
    }else {
        this
    }
}

