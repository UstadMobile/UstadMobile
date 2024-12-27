package com.ustadmobile.core.domain.localsharing.listneighbors

import com.ustadmobile.libcache.db.UstadCacheDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListLocalSharingNeighborsUseCaseCommonJvm(
    private val ustadCacheDb: UstadCacheDb,
): ListLocalSharingNeighborsUseCase {

    override fun invoke(): Flow<List<ListLocalSharingNeighborsUseCase.LocalSharingNeighbor>> {
        return ustadCacheDb.neighborCacheDao.allNeighborsAsFlow().map { neighborList ->
            neighborList.map {
                ListLocalSharingNeighborsUseCase.LocalSharingNeighbor(
                    uid = it.neighborUid,
                    addr = it.neighborIp,
                    pingTime = it.neighborPingTime,
                    name = it.neighborDeviceName,
                )
            }
        }
    }

}
