package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDiscussionDetailUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.view.person.list.demoPersonList
import csstype.*
import js.core.jso
import kotlinx.html.currentTimeMillis
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import kotlin.js.Date

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

        Typography {
            + props.uiState.courseBlock?.cbDescription.toString()
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
                        ListItemButton{
                            onClick = {
                                item?.also{props.onClickPost(it)}
                            }
                        }

                        ListItemIcon{
                            UstadPersonAvatar{
                                personUid = item?.discussionPostStartedPersonUid?: 0
                            }
                        }

                        ListItemText{
                            primary = ReactNode("${item?.authorPersonFirstNames} ${item?.authorPersonLastName}")
                            secondary = ReactNode("${item?.discussionPostTitle}")

                        }
                    //discussionPostTitle


                        /*
                    ListItem {
                        disablePadding = true


                        ListItemButton {
                            ListItemIcon {
                                UstadBlankIcon { }
                            }

                            onClick = {
                                props.onClickPost(item)
                            }

                            val dateFormatted =
                                useMemo(dependencies = arrayOf(item.postLatestMessageTimestamp)) {
                                    Date(item.postLatestMessageTimestamp ?: 0L).toLocaleDateString()
                                }



                            UstadDetailField {
                                icon = AccountCircle.create()
                                valueText = ReactNode(
                                    item.authorPersonFirstNames + " " + item.authorPersonLastName
                                        ?: ""
                                )
                                labelText = item.discussionPostTitle ?: ""


                            }


                        }
                    }
                    */





                }
            }

            Container {
                VirtualListOutlet()
            }
        }

        /*
        //Change that to FAB
        List {
            //Add post:
            ListItem {
                disablePadding = true

                ListItemButton {
                    onClick = {
                        props.onClickAddItem()
                    }

                    ListItemIcon {
                        Add {}
                    }

                    ListItemText {
                        +(strings[MessageID.post])
                    }
                }
            }

            //Posts:

            props.uiState.posts.forEach { item ->
                ListItem {
                    disablePadding = true
//                    secondaryAction = IconButton.create {
//                        onClick = {
//                            props.onDeleteClick(item)
//                        }
//                        Delete {}
//                    }

                    ListItemButton {
                        ListItemIcon {
                            UstadBlankIcon { }
                        }

                        onClick = {
                            props.onClickPost(item)
                        }

                        val dateFormatted = useMemo(dependencies = arrayOf(item.postLatestMessageTimestamp)) {
                            Date(item.postLatestMessageTimestamp ?: 0L).toLocaleDateString()
                        }



                        UstadDetailField{
                            icon = AccountCircle.create()
                            valueText = ReactNode(item.authorPersonFirstNames + " " + item.authorPersonLastName?: "")
                            labelText = item.discussionPostTitle?:""


                        }

//                        UstadMessageField{
//                            icon = AccountCircle.create()
//                            thirdTextIcon = Message.create()
//                            secondText = item.discussionPostTitle?:""
//                            firstText = item.authorPersonFirstNames + " " + item.authorPersonLastName?: ""
//                            thirdText = item.postLatestMessage ?: ""
//                            fourthText = dateFormatted
//                            fifthText = item.postRepliesCount.toString() + " replies"
//                            secondaryActionContent = null
//
//                        }

                    }
                }
            }


            */
        }
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