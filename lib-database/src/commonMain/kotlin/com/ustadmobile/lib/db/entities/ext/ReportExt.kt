package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Report

@ShallowCopy
expect fun Report.shallowCopy(
    block: Report.() -> Unit
): Report

