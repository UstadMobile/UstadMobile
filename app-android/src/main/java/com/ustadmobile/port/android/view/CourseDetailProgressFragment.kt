package com.ustadmobile.port.android.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.CourseDetailProgressUiState
import com.ustadmobile.lib.db.entities.Person
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
            .defaultScreenPadding()
    ) {

        item {
            Row (
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.End
            ) {
                CheckBoxTitle(text = stringResource(id = R.string.discussion_board),)
                CheckBoxTitle(text = stringResource(id = R.string.module))
                CheckBoxTitle(text = stringResource(id = R.string.video))
                CheckBoxTitle(text = stringResource(id = R.string.clazz_assignment))
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
            )
        }
    }
}

@Composable
private fun CheckBoxTitle(
    text: String
){
    Text(text = text,
        modifier = Modifier
            .rotate(-90F)
            .height(IntrinsicSize.Max)
            .width(45.dp)
    )
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