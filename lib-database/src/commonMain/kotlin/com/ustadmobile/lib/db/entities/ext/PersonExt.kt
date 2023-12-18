package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.Person

@ShallowCopy
expect fun Person.shallowCopy(
    block: Person.() -> Unit
): Person


