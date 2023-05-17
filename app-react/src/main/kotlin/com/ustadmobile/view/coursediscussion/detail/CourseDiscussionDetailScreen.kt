package com.ustadmobile.view.coursediscussion.detail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.coursediscussion.detail.CourseDiscussionDetailUiState
import com.ustadmobile.core.viewmodel.coursediscussion.detail.CourseDiscussionDetailViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import kotlinx.html.currentTimeMillis
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import react.router.useLocation

external interface CourseDiscussionDetailProps: Props {
    var uiState: CourseDiscussionDetailUiState
    var onClickPost: (DiscussionPostWithDetails) -> Unit
    var onDeleteClick: (DiscussionPostWithDetails) -> Unit
    var onClickAddItem: () -> Unit
}

val CourseDiscussionDetailComponent2 = FC<CourseDiscussionDetailProps> { props ->

    val strings = useStringsXml()

    val infiniteQueryResult = usePagingSource(
        props.uiState.posts, true, 50
    )

    val muiAppState = useMuiAppState()

    Container {


        Typography {
            variant = TypographyVariant.caption
            + strings[MessageID.description]
        }

        Typography{
            UstadRawHtml {
                html = (props.uiState.courseBlock?.cbDescription.toString())
            }
        }

        maxWidth = "lg"

        Stack {
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)
        }

        Box{
            sx {
                height = 10.px
            }
        }

        Typography {
            variant = TypographyVariant.h6
            + strings[MessageID.posts]
            align = TypographyAlign.left
        }

        VirtualList {
            style = jso {
                height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                width = 100.pct
                contain = Contain.strict
                overflowY = Overflow.scroll
            }

            content = virtualListContent {

                infiniteQueryPagingItems(
                    items = infiniteQueryResult,
                    key = { it.discussionPostUid.toString() }
                ) { item ->

                    ListItem.create{

                        ListItemButton {
                            onClick = {
                                item?.also { props.onClickPost(it) }
                            }

                            ListItemIcon {
                                UstadPersonAvatar {
                                    personUid = item?.discussionPostStartedPersonUid ?: 0
                                }
                            }

                            ListItemText {
                                primary =
                                    ReactNode("${item?.authorPersonFirstNames} ${item?.authorPersonLastName}")
                                secondary = ReactNode("${item?.discussionPostTitle}")

                            }

                        }

                }
            }

            Container {
                VirtualListOutlet()
            }
        }

        }
    }
}


val CourseDiscussionDetailScreen = FC<Props>{
    val location = useLocation()
    val viewModel = useUstadViewModel{di, savedStateHandle ->
        CourseDiscussionDetailViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: CourseDiscussionDetailUiState by viewModel.uiState.collectAsState(
        CourseDiscussionDetailUiState()
    )
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab{
        fabState = appState.fabState
    }

    CourseDiscussionDetailComponent2{
        this.uiState = uiState
        onClickPost = viewModel::onClick
        onClickAddItem = viewModel::onClickAdd
        onDeleteClick = viewModel::onClickDeleteEntry
    }
}

val CourseDiscussionDetailPreview = FC<Props> {

    val postAsList = listOf(
        DiscussionPostWithDetails().apply {
            discussionPostTitle = "Question about Homework A4"
            discussionPostMessage = "How is marketting different from sales?"
            discussionPostVisible = true
            authorPersonFirstNames = "Ahmed"
            authorPersonLastName = "Ismail"
            postRepliesCount = 5
            postLatestMessageTimestamp = currentTimeMillis()
            postLatestMessage = "Its very different, check section 43"

        },
        DiscussionPostWithDetails().apply {
            discussionPostTitle = "Introductions"
            discussionPostMessage = "I am your supervisor for this module. Ask me anything."
            discussionPostVisible = true
            authorPersonFirstNames = "Bilal"
            authorPersonLastName = "Zaik"
            postRepliesCount = 16
            postLatestMessageTimestamp = currentTimeMillis()
            postLatestMessage = "Can we have an extra class on TSA?"

        }
    )


    CourseDiscussionDetailComponent2 {
        uiState = CourseDiscussionDetailUiState(

            courseBlock = CourseBlock().apply {
                cbTitle = "Sales and Marketting Discussion"
                cbDescription =
                    "This discussion group is for conversations and posts about Sales and Marketting course"

            },
            posts = { ListPagingSource(postAsList) },


            )
    }
}