package com.ustadmobile.view.discussionpost.detail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState2
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.core.jso
import mui.material.Box
import mui.material.Container
import mui.material.List
import react.FC
import react.Props
import react.create

external interface DiscussionPostDetailProps: Props {
    var uiState: DiscussionPostDetailUiState2
    var onClickPostReply: () -> Unit
    var onReplyChanged: (String) -> Unit
}

val DiscussionPostDetailComponent2 = FC<DiscussionPostDetailProps> { props ->

    val muiAppState = useMuiAppState()

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = props.uiState.discussionPosts,
        placeholdersEnabled = true
    )

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
                key = { it.discussionPost?.discussionPostUid?.toString() ?: "" }
            ) { postItem ->
                Box.create {
                    DiscussionPostListItem {
                        discussionPost = postItem
                    }

                    //If this is the start post, put the
                    if(postItem?.discussionPost?.discussionPostReplyToPostUid == 0L) {
                        DiscussionPostReply {
                            reply = props.uiState.replyText
                            onClickPostReplyButton = props.onClickPostReply
                            onReplyChanged = props.onReplyChanged
                        }
                    }
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}

val DiscussionPostDetailPreview = FC<Props> {
    DiscussionPostDetailComponent2 {
        uiState = DiscussionPostDetailUiState2(
            discussionPosts = {
                ListPagingSource(listOf(
                    DiscussionPostAndPosterNames(
                        discussionPost = DiscussionPost().apply {
                            discussionPostTitle = "Submitting an assignment"
                            discussionPostVisible = true
                            discussionPostStartedPersonUid = 1
                            discussionPostReplyToPostUid = 0
                            discussionPostUid = 1
                            discussionPostMessage = "How can I get the best grade?"
                            discussionPostStartDate = systemTimeInMillis()
                        },
                        firstNames = "M",
                        lastName = "Nasruddin",
                    ),
                    DiscussionPostAndPosterNames(
                        discussionPost = DiscussionPost().apply {
                            discussionPostReplyToPostUid = 42
                            discussionPostTitle = null
                            discussionPostVisible = true
                            discussionPostStartedPersonUid = 1
                            discussionPostUid = 2
                            discussionPostMessage = "Use ChatGPT"
                            discussionPostStartDate = systemTimeInMillis()
                        },
                        firstNames = "Cheaty",
                        lastName = "McCheatface",
                    ),
                    DiscussionPostAndPosterNames(
                        discussionPost = DiscussionPost().apply {
                            discussionPostReplyToPostUid = 42
                            discussionPostVisible = true
                            discussionPostStartedPersonUid = 1

                            discussionPostUid = 3
                            discussionPostMessage = "Use BARD"
                            discussionPostStartDate = systemTimeInMillis()
                        },
                        firstNames = "Chester",
                        lastName = "Cheetah",
                    ),
                    DiscussionPostAndPosterNames(
                        discussionPost = DiscussionPost().apply {
                            discussionPostVisible = true
                            discussionPostStartedPersonUid = 1
                            discussionPostReplyToPostUid = 42
                            discussionPostUid = 4
                            discussionPostMessage = "Ask Jeeves"
                            discussionPostStartDate = systemTimeInMillis()
                        },
                        firstNames = "Uncle",
                        lastName = "Brandon",
                    ),
                ))
            },
            loggedInPersonUid = 1
        )
    }
}



val DiscussionPostDetailScreen = FC<Props>{

    val viewModel = useUstadViewModel{ di, savedStateHandle ->
        DiscussionPostDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal: DiscussionPostDetailUiState2 by viewModel.uiState.collectAsState(
        DiscussionPostDetailUiState2()
    )

    DiscussionPostDetailComponent2{
        uiState = uiStateVal
        onReplyChanged = viewModel::onChangeReplyText
        onClickPostReply = viewModel::onClickPostReply
    }
}

