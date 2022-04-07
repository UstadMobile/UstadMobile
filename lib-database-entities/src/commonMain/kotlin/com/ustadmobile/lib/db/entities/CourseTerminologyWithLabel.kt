package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class CourseTerminologyWithLabel : CourseTerminology() {

    var label: TerminologyEntry? = null

}