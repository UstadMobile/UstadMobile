package com.ustadmobile.core.domain.localsharing.listneighbors

import kotlinx.coroutines.flow.Flow

interface ListLocalSharingNeighborsUseCase {

    class LocalSharingNeighbor(
        val uid: Long,
        val addr: String,
        val name: String,
        val pingTime: Int,
    )

    operator fun invoke(): Flow<List<LocalSharingNeighbor>>

}