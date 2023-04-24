package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.core.viewmodel.PersonWithResults
import com.ustadmobile.core.viewmodel.StudentResult
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.pct
import csstype.px
import csstype.deg
import csstype.Padding
import csstype.Color
import csstype.Height
import csstype.JustifyContent
import csstype.translatex
import csstype.Overflow
import csstype.rotate
import csstype.TextOverflow
import js.core.jso
import mui.icons.material.CheckBoxOutlineBlank
import mui.icons.material.CheckBoxOutlined
import mui.material.Container
import mui.material.Stack
import mui.material.Typography
import mui.material.Icon
import mui.material.StackDirection
import mui.material.Box
import mui.system.responsive
import mui.system.sx
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.create
import react.useState
import kotlin.random.Random

external interface CourseDetailProgressProps : Props {

    var uiState: CourseDetailProgressUiState

    var onClickStudent: (Person) -> Unit

}

private val resultList = (0..10).map {

    val randomBoolean = Random.nextBoolean()
    StudentResult(
        personUid = 0,
        courseBlockUid = it.toLong(),
        clazzUid = 0,
        completed = randomBoolean
    )
}

val personList = (0..150).map {
    PersonWithResults(
        results = resultList,
        person = Person().apply {
            personUid = 0
            firstNames = "Person $it"
            lastName = "Simpson"
        }
    )
}

val strings = useStringsXml()
private val courseBlockList = (0..10).map {
    CourseBlock().apply {
        cbUid = it.toLong()
        cbTitle = strings[MessageID.discussion_board]
    }
}

val CourseDetailProgressScreenPreview = FC<Props> { props ->

    CourseDetailProgressScreenComponent2 {

        + props

        uiState = CourseDetailProgressUiState(
            students = { ListPagingSource(personList) },
            courseBlocks = courseBlockList
        )
    }
}


val CourseDetailProgressScreenComponent2 = FC<CourseDetailProgressProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.students, true, 50
    )

    val muiAppState = useMuiAppState()

    var scrollX by useState { 0 }

    Container {
        sx {
            width = 100.pct
            overflowX = Overflow.scroll
        }
        onScroll = { event ->
            scrollX = event.target.unsafeCast<HTMLElement>().scrollLeft.toInt()
        }

        Stack {
            direction = responsive(StackDirection.row)

            Box {
                Typography {
                    sx {
                        width = 315.px
                    }
                }
            }

            Container {
                sx {
                    marginLeft = 240.px
                }

                Stack {
                    direction = responsive(StackDirection.row)
                    justifyContent = JustifyContent.end

                    props.uiState.courseBlocks.forEachIndexed { index, item ->

                        Typography {

                            sx {
                                width = 60.px
                                transform = rotate(270.deg)
                                textOverflow = TextOverflow.ellipsis
                                overflowInline =  Overflow.clip
                                padding = Padding(vertical = 10.px, horizontal = 0.px)
                                if (scrollX > (index*60)){
                                    color = Color("#00000000")
                                }
                            }
                            + item.cbTitle
                        }
                    }
                }
            }

        }

        VirtualList {

            style = jso {
                height = "calc(100vh - ${muiAppState.appBarHeight+100}px)".unsafeCast<Height>()
                width = 100.pct
                contain = Contain.strict
                overflowY = Overflow.scroll
            }

            content = virtualListContent {

                infiniteQueryPagingItems(
                    items = infiniteQueryResult,
                    key = { it.person.personUid.toString() }
                ) { person ->

                    Stack.create {
                        direction = responsive(StackDirection.row)

                        Stack {
                            direction = responsive(StackDirection.row)
                            spacing = responsive(10.px)

                            sx {
                                transform = translatex(scrollX.px)
                            }

                            onClick = {
                                person?.also { props.onClickStudent(it.person) }
                            }

                            UstadPersonAvatar {

                                personUid = person?.person?.personUid ?: 0
                            }

                            Typography {
                                sx {
                                    width = 240.px
                                }
                                + "${person?.person?.fullName()}"
                            }
                        }

                        Container {
                            sx {
                                marginLeft = 240.px
                            }

                            Stack {
                                direction = responsive(StackDirection.row)
                                justifyContent = JustifyContent.end

                                props.uiState.courseBlocks.forEachIndexed { index, item ->

                                    val bool = person?.results?.first {
                                        it.courseBlockUid == item.cbUid
                                    }

                                    Icon {

                                        sx {
                                            width = 60.px
                                            if (scrollX > (index*60)){
                                                color = Color("#00000000")
                                            }
                                        }
                                        if (bool?.completed == true){
                                            + CheckBoxOutlined.create()
                                        } else {
                                            + CheckBoxOutlineBlank.create()
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }


            Container {
                sx {
                    width = 100.pct
                    overflowX = Overflow.hidden
                }
                VirtualListOutlet()
            }
        }
    }

}