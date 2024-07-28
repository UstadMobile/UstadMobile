package com.ustadmobile.view.clazz.gradebook

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.gradebook.displayMarkFor
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.mui.components.ThemeContext
import mui.material.Box
import mui.material.CircularProgress
import mui.material.CircularProgressVariant
import mui.material.Stack
import mui.material.Typography
import mui.system.responsive
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
import mui.material.Tooltip
import react.ReactNode

external interface ClazzGradebookCellProps: Props {

    var blockStatus: BlockStatus?

    var block: CourseBlock?

    var width: Int

    var height: Int

    var scoreMargin: Int?

}

val ClazzGradebookCell = FC<ClazzGradebookCellProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val scoreMarginVal = (props.scoreMargin ?: 8)
    val strings = useStringProvider()

    Box {
        sx {
            position = Position.relative
            width = props.width.px
            height = props.height.px
            textAlign = TextAlign.center
        }

        val displayMark = props.blockStatus?.displayMarkFor(props.block)
        val progress = props.blockStatus?.sProgress

        when {
            displayMark != null -> {
                val markColor = props.blockStatus?.sScoreScaled?.let {
                    props.block?.colorForMark(theme, it)
                }

                val scoreMargin = 8
                Typography {
                    sx {
                        textAlign = TextAlign.center
                        lineHeight = (props.height - (scoreMarginVal*2)).px
                        width = (props.width - (scoreMarginVal *2)).px
                        margin = scoreMargin.px
                        backgroundColor = markColor?.main
                        color = markColor?.contrastText
                    }

                    + displayMark
                }
            }

            props.blockStatus?.sIsCompleted == true || props.blockStatus?.sIsSuccess == true -> {
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

            props.blockStatus?.sIsSuccess == false -> {
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
                        width = props.width.px
                        height = props.height.px
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
                        lineHeight = props.height.px
                    }

                    + "-"
                }
            }
        }

    }
}

val ClazzGradebookPreview = FC<Props> {
    Stack {
        spacing = responsive(2)

        //Marked
        ClazzGradebookCell {
            blockStatus = BlockStatus(
                sIsCompleted = true,
                sProgress = 100,
                sScoreScaled = 0.8f
            )
            block = CourseBlock(
                cbMaxPoints = 10f,
            )
            width = 56
            height = 56
        }

        //Completed without mark
        ClazzGradebookCell {
            blockStatus = BlockStatus(
                sIsCompleted = true,
                sProgress = 100,
            )
            block = CourseBlock(
                cbMaxPoints = null,
            )
            width = 56
            height = 56
        }

        //Progressed without mark
        ClazzGradebookCell {
            blockStatus = BlockStatus(
                sProgress = 60,
            )
            block = CourseBlock(cbMaxPoints = null)
            width = 56
            height = 56
        }

        //Empty
        ClazzGradebookCell {
            blockStatus = BlockStatus()
            block = CourseBlock(cbMaxPoints = null)
            width = 56
            height = 56
        }
    }

}
