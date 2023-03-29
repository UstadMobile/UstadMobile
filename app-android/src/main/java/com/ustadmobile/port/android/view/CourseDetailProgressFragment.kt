package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterialApi::class)
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

        item {
            Row (
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier.vertical().rotate(-90f),
                    text = stringResource(id = R.string.discussion_board))
                Text(
                    modifier = Modifier.vertical().rotate(-90f),
                    text = stringResource(id = R.string.module))
                Text(
                    modifier = Modifier.vertical().rotate(-90f),
                    text = stringResource(id = R.string.video))
                Text(
                    modifier = Modifier.vertical().rotate(-90f),
                    text = stringResource(id = R.string.clazz_assignment))
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
                    Icon(
                        painter = painterResource(R.drawable.ic_person_black_24dp),
                        contentDescription = "",
                        modifier = Modifier.defaultAvatarSize()
                    )
                },
                text = { Text(student.fullName()) },
                trailing = {
                    AndroidView(factory = {  context ->
                        val view = LayoutInflater.from(context).inflate(
                            R.layout.item_clazz_log_attendance_status_toggle_buttons,
                            null, false
                        )

                        buttonsIdMap.forEach { (status, buttonId) ->
                            val button = view.findViewById<Button>(buttonId)
                            button.isEnabled = uiState.fieldsEnabled

                            button.setOnClickListener {

                            }
                        }

                        view
                    },
                        update = {

                        }
                    )
                }
            )
        }
    }
}

private val buttonsIdMap = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
    ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button
)

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
            }
        )
    )

    CourseDetailProgressScreen(uiState)
}