package com.ustadmobile.core.domain.xapi

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.model.ActivityEntities
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity

/**
 * UseCase to handle storing xAPI activities. This can be used from the statement endpoint (e.g.
 * to handle when a statement includes an activity or reference to an activity as statement object,
 * substatement object, or via contextActivities properties), or for the activity profile resource
 * itself.
 */
class StoreActivitiesUseCase(
    private val db: UmAppDatabase,
    repo: UmAppDatabase?,
) {

    private val dbOrRepo = repo ?: db

    private val ActivityEntity.isIdOnly: Boolean
        get() {
            return actType == null
                    && actMoreInfo == null
                    && actInteractionType == ActivityEntity.TYPE_UNSET
                    && actCorrectResponsePatterns == null
        }

    suspend operator fun invoke(
        activityEntities: List<ActivityEntities>
    ) {
        val timeNow = systemTimeInMillis()
        dbOrRepo.withDoorTransactionAsync {
            val activities = activityEntities.map {
                it.activityEntity.copy(
                    actLct = timeNow
                )
            }
            dbOrRepo.activityEntityDao().insertOrIgnoreAsync(activities)

            /**
             * A statement might have an object where the objectType is activity and it only includes
             * the id (e.g. it does not include the definition).
             *
             * We shouldn't attempt to update the canonical definition if that's the case. Spec says:
             * An LRS SHOULD NOT make significant changes to its canonical definition for the Activity based on an updated definition e.g. changes to correct responses.
             */
            activities.filter { !it.isIdOnly }.forEach {
                dbOrRepo.activityEntityDao().updateIfMoreInfoChanged(
                    activityUid = it.actUid,
                    actMoreInfo = it.actMoreInfo,
                    actLct = timeNow
                )

                /**
                 * Where an activity is not id only, then it might be that this is the first time we
                 * have seen the definition e.g. if it was recorded for the first time when only the
                 * id was present. If that is the case, then we should now record the
                 * canonical definition.
                 */
                dbOrRepo.activityEntityDao().updateIfNotYetDefined(
                    actUid = it.actUid,
                    actType = it.actType,
                    actMoreInfo = it.actMoreInfo,
                    actInteractionType = it.actInteractionType,
                    actCorrectResponsePatterns = it.actCorrectResponsePatterns,
                )
            }

            val allLangMapEntries = activityEntities.flatMap {
                it.activityLangMapEntries
            }

            /*
             * To avoid 'significantly changing' the the canonical definition, interaction entities
             * will only be inserted if no other interaction entities exist for that activity.
             */
            val activityInteractionEntities = activityEntities.flatMap {
                it.activityInteractionEntities
            }
            val activityUidsWithExistingInteractions = db.activityInteractionDao()
                .findActivityUidsWithInteractionEntitiesAsync(
                    activityUids = activityInteractionEntities.map { it.aieActivityUid }.distinct().toList()
                ).toSet()

            dbOrRepo.activityInteractionDao().insertOrIgnoreAsync(
                entities = activityInteractionEntities.filter {
                    it.aieActivityUid !in activityUidsWithExistingInteractions
                }
            )

            /**
             * On handling lang map entities:
             *   Entities for the name and description property will always be upserted e.g. as per
             *   the spec activities canonical definition update can include changing spelling etc.
             *
             *   Entities for langmaps that are part of interaction properties should only be inserted
             *   if those interaction entities exist.
             */
            val (nameAndDescriptionLangMapEntities, interactionLangMapEntities) =
                allLangMapEntries.partition {
                    it.almeAieHash == 0L
                }

            dbOrRepo.activityLangMapEntryDao().upsertList(nameAndDescriptionLangMapEntities)
            interactionLangMapEntities.forEach {
                dbOrRepo.activityLangMapEntryDao().upsertIfInteractionEntityExists(
                    almeActivityUid = it.almeActivityUid,
                    almeAieHash = it.almeAieHash,
                    almeValue = it.almeValue,
                    almeLastMod = timeNow,
                    almeLangCode = it.almeLangCode,
                    almeHash = it.almeHash,
                )
            }

            allLangMapEntries.forEach {
                dbOrRepo.activityLangMapEntryDao().updateIfChanged(
                    almeActivityUid = it.almeActivityUid,
                    almeHash = it.almeHash,
                    almeValue = it.almeValue,
                    almeLastMod = timeNow,
                )
            }

            dbOrRepo.activityExtensionDao().upsertListAsync(
                activityEntities.flatMap { it.activityExtensionEntities }
            )

            activityEntities.mapNotNull { it.statementContextActivityJoin }
                .takeIf { it.isNotEmpty() }
                ?.also {
                    dbOrRepo.statementContextActivityJoinDao().insertOrIgnoreListAsync(it)
                }

        }
    }
}