package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.appconfigdb.SystemDb
import com.ustadmobile.appconfigdb.composites.LearningSpaceConfigAndInfo
import com.ustadmobile.door.ext.withDoorTransaction
import java.util.concurrent.ConcurrentHashMap

/**
 * Server side repository for learning spaces. The primary purpose of the repository is to cache
 * all learning spaces in memory and ensure that any updates are persisted.
 */
class LearningSpaceServerRepo(
    private val systemDb: SystemDb,
) {

    private val learningSpaces: MutableMap<String, LearningSpaceConfigAndInfo> = ConcurrentHashMap()

    init {
        learningSpaces.putAll(
            systemDb.learningSpaceConfigDao().findAllLearningSpaceConfigAndInfo().associateBy {
                it.config.lscUrl
            }
        )
    }

    fun findByUrl(url: String): LearningSpaceConfigAndInfo? {
        return learningSpaces[url]
    }

    fun update(learningSpace: LearningSpaceConfigAndInfo) {
        systemDb.withDoorTransaction {
            systemDb.learningSpaceConfigDao().update(learningSpace.config)
            systemDb.learningSpaceInfoDao().update(learningSpace.info)
            learningSpaces[learningSpace.config.lscUrl] = learningSpace
        }
    }

    fun add(learningSpace: LearningSpaceConfigAndInfo) {
        systemDb.withDoorTransaction {
            systemDb.learningSpaceConfigDao().insert(learningSpace.config)
            systemDb.learningSpaceInfoDao().insert(learningSpace.info)
            learningSpaces[learningSpace.config.lscUrl] = learningSpace
        }

    }

}