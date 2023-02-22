package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class PeerReviewerAllocationList(val allocations: List<PeerReviewerAllocation>?)