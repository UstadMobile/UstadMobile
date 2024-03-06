package com.ustadmobile.libuicompose.view.clazz.permissionlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.libuicompose.components.UstadPermissionListItem
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.libuicompose.util.compose.courseTerminologyEntryResource

@Composable
fun CoursePermissionListItem(
    coursePermission: CoursePermissionAndListDetail?,
    permissionLabels: List<Pair<StringResource, Long>>,
    modifier: Modifier = Modifier,
    courseTerminologyEntries: List<TerminologyEntry>,
    onClickDelete: ((CoursePermission) -> Unit)? = null,
) {
    UstadPermissionListItem(
        modifier = modifier,
        value = coursePermission?.coursePermission?.cpPermissionsFlag ?: 0L,
        permissionLabels = permissionLabels,
        toPerson = coursePermission?.person,
        toPersonPicture = coursePermission?.personPicture,
        headlineContent = {
            val toRole = coursePermission?.coursePermission?.cpToEnrolmentRole ?: 0
            Text(
                text = if(toRole != 0) {
                    courseTerminologyEntryResource(
                        courseTerminologyEntries,
                        when(toRole) {
                            ClazzEnrolment.ROLE_TEACHER -> MR.strings.teachers_literal
                            else -> MR.strings.students
                        }
                    )
                }else {
                    coursePermission?.person?.fullName() ?: ""
                }
            )
        },
        onClickDelete = onClickDelete?.let { onClickDeleteFn ->
            {
                coursePermission?.coursePermission?.also(onClickDeleteFn)
            }
        }
    )
}