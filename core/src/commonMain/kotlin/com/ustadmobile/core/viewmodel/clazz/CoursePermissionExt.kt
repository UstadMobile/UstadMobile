package com.ustadmobile.core.viewmodel.clazz

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.core.viewmodel.UstadViewModel
import kotlinx.serialization.json.Json
import org.kodein.di.direct
import org.kodein.di.instance

fun CoursePermission.titleStringResource(): StringResource? {
    return when(cpToEnrolmentRole) {
        ClazzEnrolment.ROLE_STUDENT -> MR.strings.students
        ClazzEnrolment.ROLE_TEACHER -> MR.strings.teachers_literal
        else -> null
    }
}

suspend fun UstadViewModel.getTitleForCoursePermission(entity: CoursePermission?) : String {
    val json: Json = di.direct.instance()

    val roleStringResource = entity?.titleStringResource()
    return if(roleStringResource != null) {
        val terminology = activeRepo.localFirstThenRepoIfNull {
            it.courseTerminologyDao().findByUidAsync(entity.cpClazzUid)
        }

        val terminologyEntries = terminology?.toTerminologyEntries(
            json = json,
            systemImpl = systemImpl
        ) ?: emptyList()

        terminologyEntries.firstOrNull { it.stringResource == roleStringResource }
            ?.term ?: systemImpl.getString(roleStringResource)
    }else {
        activeRepo.localFirstThenRepoIfNull {
            it.personDao().findByUidAsync(entity?.cpToPersonUid ?: 0)?.personFullName()
        } ?: ""
    }
}
