package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.PersonWithAccount

@ShallowCopy
expect fun PersonWithAccount.shallowCopy(
    block: PersonWithAccount.() -> Unit,
): PersonWithAccount
