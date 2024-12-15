package com.ustadmobile.lib.rest.domain.learningspace

import com.ustadmobile.centralappconfigdb.adapters.asEntity
import com.ustadmobile.centralappconfigdb.adapters.asLearningSpaceConfigAndInfo
import com.ustadmobile.xxhashkmp.XXStringHasher
import com.ustadmobile.centralappconfigdb.model.LearningSpaceConfigAndInfo
import com.ustadmobile.centralappconfigdb.model.LearningSpaceInfo
import com.ustadmobile.centralappconfigdb.sqlite.CentralAppConfigDb
import java.util.concurrent.ConcurrentHashMap

/**
 * Server side repository for learning spaces. The primary purpose of the repository is to cache
 * all learning spaces in memory and ensure that any updates are persisted.
 */
class LearningSpaceServerRepo(
    private val centralAppConfigDb: CentralAppConfigDb,
    private val xxStringHasher: XXStringHasher,
) {

    private val learningSpaces: MutableMap<String, LearningSpaceConfigAndInfo> = ConcurrentHashMap()

    init {
        learningSpaces.putAll(
            centralAppConfigDb.learningSpaceQueries.selectAll().executeAsList().map {
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
        centralAppConfigDb.learningSpaceQueries.update(
            name = learningSpace.info.name,
            description = learningSpace.info.description,
            uid = xxStringHasher.hash(learningSpace.info.url)
        )
    }

    fun delete(learningSpaceUrl:String) {
        TODO("Update")
    }

    fun add(learningSpace: LearningSpaceConfigAndInfo) {
        centralAppConfigDb.learningSpaceQueries.insertFullObject(
            learningSpace.asEntity(xxStringHasher.hash(learningSpace.info.url))
        )
        learningSpaces[learningSpace.info.url] = learningSpace
    }

    fun getAll(): List<LearningSpaceInfo> {
        return learningSpaces.values.map { it.info }
    }

}