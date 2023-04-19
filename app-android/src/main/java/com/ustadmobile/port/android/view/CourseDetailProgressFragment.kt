package com.ustadmobile.port.android.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.paging.compose.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.PersonWithResults

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CourseDetailProgressScreen(
    uiState: CourseDetailProgressUiState = CourseDetailProgressUiState(),
    onClickStudent: (Person) -> Unit = {},
) {

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    // Must provide a factory to pagingSourceFactory that will
    // https://issuetracker.google.com/issues/241124061
    val pager = remember(uiState.students) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.students
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val scrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        stickyHeader {


            Box(Modifier
                .fillMaxWidth()
                .defaultItemPadding(),
                contentAlignment = Alignment.TopEnd
            ){

                Box(
                    modifier = Modifier.width(120.dp)
                        .height(120.dp)
                ) {
                    Row(modifier = Modifier.horizontalScroll(scrollState)
                        .height(120.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
//                        uiState.results.forEach { result ->
//                            Text(modifier = Modifier
//                                .vertical()
//                                    .rotate(-90f)
//                                .height(25.dp),
//                                overflow = TextOverflow.Ellipsis,
//                                text = result)
//                        }
                    }
                }
            }
        }

        items(
            items = lazyPagingItems,
            key = { it.person.personUid }
        ) { student ->

            ListItem(
                modifier = Modifier.clickable {
                    student?.also { onClickStudent(it.person) }
                },
                icon = {
                    UstadPersonAvatar(personUid = 0)
                },
                text = { Text("${student?.person?.fullName()}") },
                trailing = {
                    Box(modifier = Modifier.width(120.dp)) {
                        Row(modifier = Modifier
                            .horizontalScroll(scrollState)) {

                            student?.results?.forEach { result ->
                                Icon(
                                    Icons.Outlined.CheckBox,
                                    contentDescription = "",
                                    modifier = Modifier.defaultMinSize()
                                )
                            }
                        }
                    }
                }
            )

        }

    }
}

//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .defaultScreenPadding(),
//    ) {
//
//        stickyHeader {
//
////            LazyRow(
////                modifier = Modifier
////                    .defaultItemPadding()
////                    .padding(start = 40.dp),
////                state = stateRowX,
////                userScrollEnabled = false
////            ) {
////                items(
////                    items = uiState.results,
////                ){ result ->
////                    CheckBoxTitle(text = result)
////                }
////            }
//
//        }
//
//        items(
//            items = uiState.students,
//            key = { student -> student.personUid }
//        ){ student ->
//            ListItem(
//                modifier = Modifier.clickable {
//                    onClickStudent(student)
//                },
//                icon = {
//                    UstadPersonAvatar(personUid = 0)
//                },
//                text = { Text(student.fullName()) },
//                trailing = {
//
////                    LazyRow(
////                        state = stateRowY,
////                        userScrollEnabled = false
////                    ) {
////                        items(
////                            items = uiState.results,
////                        ){ _ ->
////                            Icon(
////                                Icons.Outlined.CheckBox,
////                                contentDescription = "",
////                                modifier = Modifier.defaultMinSize()
////                            )
////                        }
////                    }
//                }
//            )
//        }
//    }
//}

@Composable
private fun CheckBoxTitle(
    text: String
){
    Text(
        modifier = Modifier.vertical()
            .rotate(-90f)
            .height(22.dp),
        text = text)
}

private fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

@Composable
@Preview
fun CourseDetailProgressScreenPreview() {

    val uiState = CourseDetailProgressUiState(
        students = {
            ListPagingSource(listOf(
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 1
                        firstNames = "Bart"
                        lastName = "Simpson"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 2
                        firstNames = "Shelly"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 3
                        firstNames = "Tracy"
                        lastName = "Mackleberry"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 4
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 5
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 6
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 7
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 8
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 9
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 10
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 11
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                ),
                PersonWithResults(
                    results = listOf(),
                    person = Person().apply {
                        personUid = 12
                        firstNames = "Nelzon"
                        lastName = "Muntz"
                    }
                )
            ))
        },
    )

    CourseDetailProgressScreen(uiState)
}