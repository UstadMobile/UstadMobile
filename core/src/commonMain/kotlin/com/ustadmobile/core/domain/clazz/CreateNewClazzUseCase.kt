package com.ustadmobile.core.domain.clazz

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.util.randomString

/**
 * Use case to initialize a new course (aka "clazz").
 */
class CreateNewClazzUseCase(
    private val repoOrDb: UmAppDatabase
) {

    suspend operator fun invoke(
        clazz: Clazz,
    ) : Long {
        return repoOrDb.withDoorTransactionAsync {
            clazz.takeIf { it.clazzCode == null }?.clazzCode =
                randomString(Clazz.CLAZZ_CODE_DEFAULT_LENGTH)

            val clazzUid = repoOrDb.clazzDao().insertAsync(clazz)
            repoOrDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpClazzUid = clazz.clazzUid,
                    cpToEnrolmentRole = ClazzEnrolment.ROLE_TEACHER,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                )
            )
            repoOrDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpClazzUid = clazz.clazzUid,
                    cpToEnrolmentRole = ClazzEnrolment.ROLE_STUDENT,
                    cpPermissionsFlag = CoursePermission.STUDENT_DEFAULT_PERMISSIONS,
                )
            )

            clazzUid
        }
    }

}