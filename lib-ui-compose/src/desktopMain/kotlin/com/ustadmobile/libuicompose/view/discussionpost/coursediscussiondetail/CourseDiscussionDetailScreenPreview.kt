package com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.lib.db.composites.CourseBlockAndPicture


@Composable
@Preview
fun CourseDiscussionDetailScreenPreview(){

    CourseDiscussionDetailScreen(
        uiState = CourseDiscussionDetailUiState(

            courseBlock = CourseBlockAndPicture(
                block = CourseBlock().apply{
                    cbTitle = "Discussions on Module 4: Statistics and Data Science"
                    cbDescription = "Here Any discussion related to Module 4 of Data Science chapter goes here."
                }
            ),
        )
    )
}
