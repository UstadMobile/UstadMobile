package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.PersonAndDisplayDetail

@ShallowCopy
expect fun PersonAndDisplayDetail.shallowCopy(
    block: PersonAndDisplayDetail.() -> Unit,
): PersonAndDisplayDetail
