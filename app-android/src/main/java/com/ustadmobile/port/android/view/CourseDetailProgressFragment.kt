package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.widget.Button
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.port.android.util.ext.defaultAvatarSize
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CourseDetailProgressScreen(
    uiState: CourseDetailProgressUiState = CourseDetailProgressUiState(),
    onClickStudent: (Person) -> Unit = {},
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
    ) {

        stickyHeader {
            Row (
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                CheckBoxTitle(text = stringResource(id = R.string.discussion_board))
                CheckBoxTitle(text = stringResource(id = R.string.module))
                CheckBoxTitle(text = stringResource(id = R.string.video))
                CheckBoxTitle(text = stringResource(id = R.string.clazz_assignment))
                CheckBoxTitle(text = "")
            }
        }

        items(
            items = uiState.students,
            key = { student -> student.personUid }
        ){ student ->
            ListItem(
                modifier = Modifier.clickable {
                    onClickStudent(student)
                },
                icon = {
                    UstadPersonAvatar(personUid = 0)
                },
                text = { Text(student.fullName()) },
                trailing = {
                    Row {
                        (0..3).forEach { _ ->

                            Icon(
                                Icons.Outlined.CheckBox,
                                contentDescription = "",
                                modifier = Modifier.defaultMinSize()
                            )

                        }
                    }
                }
            )
        }
    }
}

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
        students = listOf(
            Person().apply {
                personUid = 1
                firstNames = "Bart"
                lastName = "Simpson"
            },
            Person().apply {
                personUid = 2
                firstNames = "Shelly"
                lastName = "Mackleberry"
            },
            Person().apply {
                personUid = 3
                firstNames = "Tracy"
                lastName = "Mackleberry"
            },
            Person().apply {
                personUid = 4
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 5
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 6
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 7
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 8
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 9
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 10
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 11
                firstNames = "Nelzon"
                lastName = "Muntz"
            },
            Person().apply {
                personUid = 12
                firstNames = "Nelzon"
                lastName = "Muntz"
            }
        )
    )

    CourseDetailProgressScreen(uiState)
}