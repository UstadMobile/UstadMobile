package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture

data class CoursePermissionAndListDetail(
    @Embedded
    var coursePermission: CoursePermission? = null,

    @Embedded
    var person: Person? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

)

