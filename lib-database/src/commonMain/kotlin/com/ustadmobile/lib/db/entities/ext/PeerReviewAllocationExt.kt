package com.ustadmobile.lib.db.entities.ext

import com.ustadmobile.door.annotation.ShallowCopy
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation

@ShallowCopy
expect fun PeerReviewerAllocation.shallowCopy(
    block: PeerReviewerAllocation.() -> Unit,
): PeerReviewerAllocation
