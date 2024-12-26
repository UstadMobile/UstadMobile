package com.ustadmobile.core.domain.localsharing.listneighbors

import kotlinx.coroutines.flow.Flow

interface ListLocalSharingNeighborsUseCase {

    class LocalSharingNeighbor(
        val addr: String,
        val pingTime: Int,
    )

    operator fun invoke(): Flow<List<LocalSharingNeighbor>>

}