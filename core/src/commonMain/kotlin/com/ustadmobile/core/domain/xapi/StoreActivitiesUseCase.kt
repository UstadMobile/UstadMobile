package com.ustadmobile.core.domain.xapi

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.ActivityEntities
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity

class StoreActivitiesUseCase(
    private val dbOrRepo: UmAppDatabase
) {

    private val ActivityEntity.isIdOnly: Boolean
        get() {
            return actType == null && actMoreInfo == null && actInteractionType == null
                    && actCorrectResponsePatterns == null
        }

    suspend operator fun invoke(
        activityEntities: List<ActivityEntities>
    ) {
        val timeNow = systemTimeInMillis()
        dbOrRepo.withDoorTransactionAsync {
            val activities = activityEntities.map { it.activityEntity }
            dbOrRepo.activityEntityDao.insertOrIgnoreAsync(activities)

            //A statement might have an object where the objectType is activity and it only includes
            //the id (e.g. it does not include the definition).
            //We shouldn't attempt to update the canonical definition if that's the case.
            activities.filter { !it.isIdOnly }.forEach {
                dbOrRepo.activityEntityDao.updateIfChanged(
                    activityUid = it.actUid,
                    actType = it.actType,
                    actInteractionType = it.actInteractionType,
                    actMoreInfo = it.actMoreInfo,
                    actCorrectResponsePatterns = it.actCorrectResponsePatterns,
                    actLct = timeNow
                )
            }
            val allLangMapEntries = activityEntities.flatMap {
                it.activityLangMapEntries
            }
            dbOrRepo.activityLangMapEntryDao.insertOrIgnoreList(allLangMapEntries)
            allLangMapEntries.forEach {
                dbOrRepo.activityLangMapEntryDao.updateIfChanged(
                    almeActivityUid = it.almeActivityUid,
                    almeHash = it.almeHash,
                    almeValue = it.almeValue,
                    almeLastMod = timeNow,
                )
            }
        }
    }
}