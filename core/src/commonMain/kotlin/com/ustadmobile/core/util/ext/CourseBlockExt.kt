package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.CourseBlock

/**
 * Determine the latest possible time that this CourseBlock can be submitted: applies the grace
 * period if set, otherwise uses the deadline.
 */
fun CourseBlock.lastPossibleSubmissionTime(): Long {
    if(cbGracePeriodDate.isDateSet() && cbGracePeriodDate > cbDeadlineDate)
        return cbGracePeriodDate
    else
        return cbDeadlineDate
}


/**
 * Sum the max score if this block is a module
 *
 * @param allBlocks All CourseBlocks in the course.
 * @return If the receiver CourseBlock is a module, then sum the max score for all blocks in the
 * module. Otherwise, return the maxScore for the given block.
 */
fun CourseBlock.maxScoreSummedIfModule(
    allBlocks: List<CourseBlock>
): Float?{
    if(cbType != CourseBlock.BLOCK_MODULE_TYPE)
        return cbMaxPoints

    var totalModPoints = 0f
    var numModBlocks = 0
    allBlocks.forEach { block ->
        if(block.cbModuleParentBlockUid == cbUid) {
            numModBlocks++
            totalModPoints += (block.cbMaxPoints ?: 0f)
        }
    }

    return if(numModBlocks > 0 && totalModPoints > 0)
        totalModPoints
    else
        null
}
