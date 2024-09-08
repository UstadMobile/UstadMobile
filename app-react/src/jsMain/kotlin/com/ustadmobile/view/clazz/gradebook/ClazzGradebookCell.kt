package com.ustadmobile.view.clazz.gradebook

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.gradebook.displayMarkFor
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.ThemeContext
import mui.material.Box
import mui.material.CircularProgress
import mui.material.CircularProgressVariant
import mui.material.Typography
import mui.system.sx
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.useRequiredContext
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.pct
import web.cssom.px
import web.cssom.translate
import mui.icons.material.Check as CheckIcon
import mui.icons.material.Close as CloseIcon
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.maxScoreSummedIfModule
import com.ustadmobile.core.viewmodel.clazz.gradebook.aggregateIfModule
import mui.material.Tooltip
import react.ReactNode

private const val DEFAULT_COLUMN_WIDTH = 56

private const val DEFAULT_COLUMN_HEIGHT = 56


external interface ClazzGradebookCellProps: Props {

    var blockUid: Long

    var blockStatuses: List<BlockStatus>

    var blocks: List<CourseBlock>

    var width: Int?

    var height: Int?

    var scoreMargin: Int?

    var showMaxScore: Boolean?

}

val ClazzGradebookCell = FC<ClazzGradebookCellProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val scoreMarginVal = (props.scoreMargin ?: 8)
    val strings = useStringProvider()
    val widthVal = props.width ?: DEFAULT_COLUMN_WIDTH
    val heightVal = props.height ?: DEFAULT_COLUMN_HEIGHT

    val block = props.blocks.firstOrNull { it.cbUid == props.blockUid }
    val blockStatus = props.blockStatuses.aggregateIfModule(props.blockUid, props.blocks)
    val maxPoints = block?.maxScoreSummedIfModule(allBlocks = props.blocks)

    Box {
        sx {
            position = Position.relative
            width = widthVal.px
            height = heightVal.px
            textAlign = TextAlign.center
        }

        val displayMark = blockStatus?.displayMarkFor(maxPoints)
        val progress = blockStatus?.sProgress

        when {
            displayMark != null -> {
                val markColor = blockStatus.sScoreScaled?.let { scoreScaled ->
                    block?.takeIf { blockStatus.sIsCompleted }?.colorForMark(theme, scoreScaled)
                }

                val scoreMargin = 8
                Typography {
                    sx {
                        textAlign = TextAlign.center
                        lineHeight = (heightVal - (scoreMarginVal*2)).px
                        width = (widthVal - (scoreMarginVal *2)).px
                        margin = scoreMargin.px
                        backgroundColor = markColor?.main
                        color = markColor?.contrastText
                    }

                    + displayMark
                }
            }

            blockStatus?.sIsCompleted == true || blockStatus?.sIsSuccess == true -> {
                Tooltip {
                    title = ReactNode(strings[MR.strings.completed])

                    CheckIcon {
                        sx {
                            position = Position.absolute
                            top = 50.pct
                            left = 50.pct
                            transform =  translate((-50).pct, (-50).pct)
                        }

                        ariaLabel = strings[MR.strings.completed]
                    }
                }

            }

            blockStatus?.sIsSuccess == false -> {
                Tooltip {
                    title = ReactNode(strings[MR.strings.failed])

                    CloseIcon {
                        sx {
                            position = Position.absolute
                            top = 50.pct
                            left = 50.pct
                            transform =  translate((-50).pct, (-50).pct)
                        }

                        ariaLabel = strings[MR.strings.failed]
                    }
                }
            }

            progress != null -> {
                CircularProgress {
                    sx {
                        width = widthVal.px
                        height = heightVal.px
                        padding = 8.px
                    }
                    variant = CircularProgressVariant.determinate
                    value = progress
                }
            }

            else -> {
                Typography {
                    sx {
                        textAlign = TextAlign.center
                        lineHeight = heightVal.px
                    }

                    + "-"
                }
            }
        }

    }
}
