package com.ustadmobile.core.domain.courseblockupdate

import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.CourseBlock

fun List<CourseBlockAndEditEntities>.findParentModule(
    forBlock: CourseBlockAndEditEntities
): Long {
    //Modules don't have parent modules
    if(forBlock.courseBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        return 0

    val index = indexOfFirst { it.courseBlock.cbUid == forBlock.courseBlock.cbUid }
    val prevBlock = getOrNull(index - 1) ?: return 0

    return if(prevBlock.courseBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        prevBlock.courseBlock.cbUid
    else if(prevBlock.courseBlock.cbIndentLevel >= 1)
        prevBlock.courseBlock.cbModuleParentBlockUid
    else
        0
}

/**
 * Update the parent module uid and index on all items in the list. The parent module is the
 * previous items module if it is either a module itself, or the same as the previous item if the
 * indent >= 1
 */
fun List<CourseBlockAndEditEntities>.updateParentModuleUidsAndIndex(): List<CourseBlockAndEditEntities> {
    return toMutableList().mapIndexed { index, block ->
        val parentModUid = findParentModule(block)
        if(block.courseBlock.cbModuleParentBlockUid != parentModUid || block.courseBlock.cbIndex != index){
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbModuleParentBlockUid = parentModUid,
                    cbIndex = index
                )
            )
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
fun List<CourseBlockAndEditEntities>.autoIndent(
    index: Int
) : List<CourseBlockAndEditEntities> {
    //Do not indent a module block
    val block = this[index].courseBlock
    if(block.cbType == CourseBlock.BLOCK_MODULE_TYPE)
        return this

    val prevBlock = getOrNull(index - 1)?.courseBlock ?: return this
    val autoIndent = if(prevBlock.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
        1
    }else {
        prevBlock.cbIndentLevel
    }

    return if(block.cbIndentLevel != autoIndent) {
        toMutableList().apply {
            this[index] = this[index].copy(
                courseBlock = this[index].courseBlock.copy(
                    cbIndentLevel = autoIndent
                )
            )
        }.toList()
    } else {
        this
    }
}

