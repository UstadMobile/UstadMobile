package com.ustadmobile.view.clazz.permissionlist

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.mui.components.UstadPermissionListItem
import dev.icerock.moko.resources.StringResource
import react.FC
import react.Props
import com.ustadmobile.core.MR
import react.ReactNode

external interface CoursePermissionListItemProps : Props{
    var coursePermission: CoursePermissionAndListDetail?
    var permissionLabels: List<Pair<StringResource, Long>>
    var courseTerminologyEntries: List<TerminologyEntry>
}


val CoursePermissionListItem = FC<CoursePermissionListItemProps> { props ->
    val strings = useStringProvider()
    val toRole = props.coursePermission?.coursePermission?.cpToEnrolmentRole ?: 0
    val toTerminology = if(toRole == ClazzEnrolment.ROLE_TEACHER)
        MR.strings.teachers_literal
    else
        MR.strings.students

    val terminologyStr = courseTerminologyResource(props.courseTerminologyEntries,
        strings, toTerminology)

    val headlineText = if(toRole != 0) {
        terminologyStr
    }else {
        props.coursePermission?.person?.fullName() ?: ""
    }

    UstadPermissionListItem {
        permissionLabels = props.permissionLabels
        primary = ReactNode(headlineText)
        value = props.coursePermission?.coursePermission?.cpPermissionsFlag ?: 0
        toPerson = props.coursePermission?.person
        toPersonPicture = props.coursePermission?.personPicture
    }
}
