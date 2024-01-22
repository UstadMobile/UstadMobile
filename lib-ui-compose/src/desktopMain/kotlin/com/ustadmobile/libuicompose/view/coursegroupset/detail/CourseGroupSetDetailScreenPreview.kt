package com.ustadmobile.libuicompose.view.coursegroupset.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailUiState
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet

@Composable
@Preview
fun CourseGroupSetDetailScreenPreview() {
    CourseGroupSetDetailScreen(
        uiState = CourseGroupSetDetailUiState(
            courseGroupSet = CourseGroupSet().apply {
                cgsName = "Group 1"
                cgsTotalGroups = 4
            },
            membersList = listOf(
                CourseGroupMemberAndName(
                    personUid = 1,
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Bart Simpson"
                ),
                CourseGroupMemberAndName(
                    personUid = 2,
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Shelly Mackleberry"
                ),
                CourseGroupMemberAndName(
                    personUid = 3,
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 2
                    },
                    name = "Tracy Mackleberry"
                ),
                CourseGroupMemberAndName(
                    personUid = 4,
                    cgm = CourseGroupMember().apply {
                        cgmGroupNumber = 1
                    },
                    name = "Nelzon Muntz"
                )
            )
        )
    )
}