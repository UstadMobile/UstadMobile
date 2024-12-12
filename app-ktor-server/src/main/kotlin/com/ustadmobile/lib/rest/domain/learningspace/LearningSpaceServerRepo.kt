package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.appconfigdb.adapters.asEntity
import com.ustadmobile.appconfigdb.adapters.asLearningSpaceConfigAndInfo
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.systemdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.systemdb.model.LearningSpaceInfo
import com.ustadmobile.systemdb.sqlite.SystemDb
import java.util.concurrent.ConcurrentHashMap

/**
 * Server side repository for learning spaces. The primary purpose of the repository is to cache
 * all learning spaces in memory and ensure that any updates are persisted.
 */
class LearningSpaceServerRepo(
    private val systemDb: SystemDb,
    private val xxStringHasher: XXStringHasher,
) {

    private val learningSpaces: MutableMap<String, LearningSpaceConfigAndInfo> = ConcurrentHashMap()

    init {
        learningSpaces.putAll(
            systemDb.learningSpaceQueries.selectAll().executeAsList().map {
                it.asLearningSpaceConfigAndInfo()
            }.associateBy {
                it.config.url
            }
        )
    }

    fun findByUrl(url: String): LearningSpaceConfigAndInfo? {
        return learningSpaces[url]
    }

    fun update(learningSpace: LearningSpaceConfigAndInfo) {
        systemDb.learningSpaceQueries.update(
            name = learningSpace.info.name,
            description = learningSpace.info.description,
            uid = xxStringHasher.hash(learningSpace.info.url)
        )
    }

    fun delete(learningSpaceUrl:String) {
        TODO("Update")
    }

    fun add(learningSpace: LearningSpaceConfigAndInfo) {
        systemDb.learningSpaceQueries.insertFullObject(
            learningSpace.asEntity(xxStringHasher.hash(learningSpace.info.url))
        )
    }

    fun getAll(): List<LearningSpaceInfo> {
        return learningSpaces.values.map { it.info }
    }

}