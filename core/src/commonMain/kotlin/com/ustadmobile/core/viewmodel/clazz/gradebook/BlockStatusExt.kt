package com.ustadmobile.core.viewmodel.clazz.gradebook

import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock

private val COMPLETABLE_BLOCK_TYPES = listOf(CourseBlock.BLOCK_CONTENT_TYPE, CourseBlock.BLOCK_ASSIGNMENT_TYPE)

private fun CourseBlock.isCompleteable(): Boolean {
    return cbType in COMPLETABLE_BLOCK_TYPES
}

/**
 * If the given blockUid is a module, then add up scores.
 */
fun List<BlockStatus>.aggregateIfModule(
    blockUid: Long,
    blocks: List<CourseBlock>
): BlockStatus? {
    val block = blocks.firstOrNull { it.cbUid == blockUid } ?: return null
    return if(block.cbType == CourseBlock.BLOCK_MODULE_TYPE) {
        val moduleBlockCount = blocks.count { it.cbModuleParentBlockUid == blockUid }
        if(moduleBlockCount == 0)
            return null

        //If the module contains score-able blocks, then we add up scores. We do not aggregate
        // progress info on non-score-eable blocks.
        val moduleBlockUids = blocks.filter { it.cbModuleParentBlockUid == blockUid }
            .associateBy { it.cbUid }

        var numBlocksWithScoreResult = 0
        var numBlocksWithScore = 0
        var numModsComplete = 0
        var numBlocksSuccessSet = 0
        var numBlocksSuccess = 0
        var numBlocksFailed = 0
        var totalPointsScored = 0f
        var maxPointsInModule = 0f
        var numCompleteableBlocksInModule = 0

        this.forEach { blockStatus ->
            if(blockStatus.sCbUid in moduleBlockUids.keys) {
                val blockForStatus = moduleBlockUids[blockStatus.sCbUid]

                if(blockForStatus?.isCompleteable() == true)
                    numCompleteableBlocksInModule++

                blockForStatus?.cbMaxPoints?.also {
                    maxPointsInModule += it
                    numBlocksWithScore++
                }

                blockStatus.sScoreScaled?.also {
                    totalPointsScored += (it * (blockForStatus?.cbMaxPoints ?: 0f))
                    numBlocksWithScoreResult++
                }

                if(blockStatus.sIsCompleted)
                    numModsComplete++

                blockStatus.sIsSuccess?.also { isSuccess ->
                    numBlocksSuccessSet++

                    if(isSuccess)
                        numBlocksSuccess++
                    else
                        numBlocksFailed++
                }
            }
        }

        val isModuleComplete = numCompleteableBlocksInModule > 0 &&
                numCompleteableBlocksInModule == numModsComplete

        BlockStatus(
            sPersonUid = this.firstOrNull()?.sPersonUid ?: 0,
            sCbUid = blockUid,
            sScoreScaled = (totalPointsScored / maxPointsInModule),
            sIsCompleted = isModuleComplete,
            sIsSuccess = when {
                isModuleComplete && numBlocksSuccess == numBlocksSuccessSet -> true
                isModuleComplete && numBlocksFailed > 0 -> false
                else -> null
            }
        )
    }else {
        firstOrNull { it.sCbUid == blockUid }
    }
}

fun BlockStatus.markFor(
    maxPoints: Float?
) : Float? {
    val scaledMarkVal = this.sScoreScaled
    return if(scaledMarkVal != null && maxPoints != null) {
        (scaledMarkVal * maxPoints)
    }else {
        null
    }
}


fun BlockStatus.displayMarkFor(
    maxPoints: Float?
) : String? {
    return markFor(maxPoints)?.toDisplayString()
}
