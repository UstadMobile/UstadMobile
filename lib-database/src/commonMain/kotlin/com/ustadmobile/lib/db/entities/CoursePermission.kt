package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.core.db.PermissionFlags.COURSE_ATTENDANCE_VIEW
import com.ustadmobile.core.db.PermissionFlags.COURSE_EDIT
import com.ustadmobile.core.db.PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT
import com.ustadmobile.core.db.PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT
import com.ustadmobile.core.db.PermissionFlags.COURSE_MODERATE
import com.ustadmobile.core.db.PermissionFlags.COURSE_VIEW
import com.ustadmobile.core.db.PermissionFlags.COURSE_VIEW_MEMBERS
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import kotlinx.serialization.Serializable

/**
 * @param cpToEnrolmentRole permissions will be given to anyone who is a member of the course with
 *        the specified role.
 * @param cpToPersonUid permissions will be given to a specific personUid
 * @param cpToGroupUid permissions will be given to a specific group (placeholder, reserved for future use)
 *
 */
@Entity(
    indices = arrayOf(
        Index("cpClazzUid", name = "idx_coursepermission_clazzuid")
    )
)
@ReplicateEntity(
    tableId = CoursePermission.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(
        name = "coursepermission_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
@Serializable
data class CoursePermission(
    @PrimaryKey(autoGenerate = true)
    var cpUid: Long = 0,

    @ReplicateEtag
    @ReplicateLastModified
    var cpLastModified: Long = 0,

    var cpClazzUid: Long = 0,

    var cpToEnrolmentRole: Int= 0,

    var cpToPersonUid: Long = 0,

    var cpToGroupUid: Long = 0,

    var cpPermissionsFlag: Long = 0,

    var cpIsDeleted: Boolean = false,
) {

    companion object {

        const val TABLE_ID = 10012


        const val TEACHER_DEFAULT_PERMISSIONS = COURSE_VIEW or
                COURSE_EDIT or
                COURSE_VIEW_MEMBERS or
                COURSE_MODERATE or
                COURSE_MANAGE_STUDENT_ENROLMENT or
                COURSE_MANAGE_TEACHER_ENROLMENT or
                COURSE_ATTENDANCE_VIEW


        const val STUDENT_DEFAULT_PERMISSIONS = COURSE_VIEW or
                COURSE_VIEW_MEMBERS


    }
}
