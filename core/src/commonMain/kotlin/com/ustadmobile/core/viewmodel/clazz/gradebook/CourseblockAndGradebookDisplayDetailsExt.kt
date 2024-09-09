package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.lib.db.composites.CourseBlockAndGradebookDisplayDetails
import com.ustadmobile.lib.db.entities.CourseBlock

val CourseBlockAndGradebookDisplayDetails.thumbnailUri: String?
    get() = courseBlockPicture?.cbpThumbnailUri ?: contentEntryPicture2?.cepThumbnailUri


/**
 * Get the maximum score for a given CourseBlock in the list. If the CourseBlock is a module,
 * return the sum of maxScore for all its module blocks.
 */
fun List<CourseBlockAndGradebookDisplayDetails>.maxScoreForBlock(
    block: CourseBlockAndGradebookDisplayDetails
): Float? {
    val blockVal = block.block

    return if(blockVal?.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
        val numBlocksWithScore = count {
            it.block?.cbModuleParentBlockUid == blockVal.cbUid && it.block?.cbMaxPoints != null
        }

        if(numBlocksWithScore == 0)
            null
        else
            sumOf { blockAndGradebookDetails ->
                blockAndGradebookDetails.block?.cbMaxPoints?.takeIf {
                    blockAndGradebookDetails.block?.cbModuleParentBlockUid == blockVal.cbUid
                }?.toDouble() ?: 0.toDouble()
            }.toFloat()
    }else{
        blockVal?.cbMaxPoints
    }
}


