package com.ustadmobile.port.android.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
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
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.PersonWithResults
import com.ustadmobile.core.viewmodel.StudentResult
import com.ustadmobile.lib.db.entities.CourseBlock
import kotlin.random.Random

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

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopEnd
            ){

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(90.dp)
                ) {
                    Row(modifier = Modifier
                        .horizontalScroll(scrollState)
                        .height(90.dp)
                        .background(Color.White),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        uiState.courseBlocks.forEach { courseBlock ->
                            Text(modifier = Modifier
                                .vertical()
                                .rotate(-90f)
                                .height(25.dp)
                                .width(60.dp),
                                overflow = TextOverflow.Ellipsis,
                                text = courseBlock.cbTitle ?: "")
                        }
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

                            uiState.courseBlocks.forEach { courseBlock ->

                                val bool = student?.results?.first {
                                    it.courseBlockUid == courseBlock.cbUid
                                }

                                Icon(
                                    imageVector = if (bool?.completed == true){
                                        Icons.Outlined.CheckBox
                                    } else {
                                        Icons.Outlined.CheckBoxOutlineBlank
                                    },
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

private fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

private val courseBlockList = (0..10).map {
    CourseBlock().apply {
        cbUid = it.toLong()
        cbTitle = "discussion_board"
    }
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
            personUid = it.toLong()
            firstNames = "Person $it"
            lastName = "Simpson"
        }
    )
}

@Composable
@Preview
fun CourseDetailProgressScreenPreview() {

    val uiState = CourseDetailProgressUiState(
        students = { ListPagingSource(personList) },
        courseBlocks = courseBlockList
    )

    CourseDetailProgressScreen(uiState)
}