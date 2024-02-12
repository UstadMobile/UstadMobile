package com.ustadmobile.view.epubcontent

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.mui.components.ThemeContext
import emotion.react.css
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
import mui.material.Box
import mui.material.List
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemSecondaryAction
import mui.material.ListItemText
import mui.material.Tooltip
import mui.system.sx
import react.FC
import react.ReactNode
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML
import react.useRequiredContext
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.vw

val EpubTocListComponent = FC<EpubContentProps> { props ->
    val theme by useRequiredContext(ThemeContext)
    val muiAppState = useMuiAppState()
    val strings = useStringProvider()

    List {
        id = "epub_toc_list"

        sx {
            width = EpubArea.NavAreaWidth
            maxWidth = 90.vw
        }

        Box {
            sx {
                paddingTop = muiAppState.appBarHeight.px
                textAlign = TextAlign.center
            }

            props.uiState.coverImageUrl?.also { coverUrl ->
                ReactHTML.img {
                    src = coverUrl
                    css {
                        maxWidth = 300.px
                        maxHeight = 300.px
                        paddingTop = theme.spacing(2)
                    }
                }
            }

        }

        props.uiState.tableOfContentToDisplay.forEach { tocItem ->
            ListItem {
                key = "toc_${tocItem.uid}"

                ListItemButton {
                    sx {
                        paddingLeft = theme.spacing(2 + (tocItem.indentLevel * 2))
                    }
                    onClick = {
                        props.onClickTocItem(tocItem)
                    }

                    ListItemText {
                        primary = ReactNode(tocItem.label)
                    }
                }

                if (tocItem.hasChildren) {
                    ListItemSecondaryAction {
                        val collapsed = tocItem.uid in props.uiState.collapsedTocUids
                        val text =
                            strings[if (collapsed) MR.strings.expand else MR.strings.collapse]
                        Tooltip {
                            title = ReactNode(text)

                            IconButton {
                                onClick = {
                                    props.onClickToggleTogItem(tocItem)
                                }
                                ariaLabel = text

                                if (collapsed) {
                                    KeyboardArrowDown()
                                } else {
                                    KeyboardArrowUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}