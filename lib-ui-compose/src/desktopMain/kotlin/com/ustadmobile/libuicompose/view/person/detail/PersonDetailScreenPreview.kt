package com.ustadmobile.libuicompose.view.person.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailUiState
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance

@Composable
@Preview
fun PersonDetailScreenPreview() {
    PersonDetailScreen(
        uiState = PersonDetailUiState(
            person = PersonWithPersonParentJoin().apply {
                firstNames = "Bob Jones"
                phoneNum = "0799999"
                emailAddr = "Bob@gmail.com"
                gender = 2
                username = "Bob12"
                dateOfBirth = 1352958816
                personOrgId = "123"
                personAddress = "Herat"
            },
            chatVisible = true,
            clazzes = listOf(
                ClazzEnrolmentWithClazzAndAttendance().apply {
                    clazz = Clazz().apply {
                        clazzName = "Jetpack Compose Class"
                    }
                },
                ClazzEnrolmentWithClazzAndAttendance().apply {
                    clazz = Clazz().apply {
                        clazzName = "React Class"
                    }
                },
            )
        )
    )
}