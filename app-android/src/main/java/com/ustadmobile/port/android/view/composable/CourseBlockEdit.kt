package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.ext.shallowCopy

private data class CourseBlockEditData(
    val caStartDateError: String? = null
)

@Composable
fun CourseBlockEdit(
    courseBlock: CourseBlock,
    onCourseBlockChange: (CourseBlock) -> Unit
){

    val data = CourseBlockEditData()

    Row{
        UstadTextEditField(
            value = courseBlock.cbTitle ?: "",
            label = stringResource(id = R.string.dont_show_before).addOptionalSuffix(),
            error = data.caStartDateError,
            enabled = uiState.fieldsEnabled,
            onValueChange = {
                onCourseBlockChange(courseBlock.shallowCopy{
                    cbTitle = it
                })
            }
        )
    }
}

@Composable
@Preview
private fun CourseBlockEditPreview() {
    CourseBlockEdit(CourseBlock(), {})
}
