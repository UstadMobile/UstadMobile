package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ReportFilterWithDisplayDetails : ReportFilter() {

    @Embedded
    var person: Person? = null

    @Embedded
    var verb: VerbEntity? = null

    var xlangMapDisplay: String? = null

     @Embedded
     var contentEntry: ContentEntry? = null

}