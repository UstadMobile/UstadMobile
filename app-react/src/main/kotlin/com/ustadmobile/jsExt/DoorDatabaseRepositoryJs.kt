package com.ustadmobile.jsExt

import com.ustadmobile.door.*
import kotlin.reflect.KClass

class DoorDatabaseRepositoryJs(override val db: DoorDatabase, override val config: RepositoryConfig): DoorDatabaseRepository {

    override var connectivityStatus: Int
        get() = 0
        set(value) {}

    override val dbPath: String
        get() = db::class.js.name

    override val tableIdMap: Map<String, Int>
        get() = mapOf()

    override suspend fun activeMirrors(): List<MirrorEndpoint> {
        return listOf()
    }

    override suspend fun addMirror(mirrorEndpoint: String, initialPriority: Int): Int {
        return 0
    }

    override fun <T : Any> addSyncListener(entityClass: KClass<T>, syncListener: SyncListener<T>) {}

    override fun addTableChangeListener(listener: TableChangeListener) {}

    override fun addWeakConnectivityListener(listener: RepositoryConnectivityListener) {}

    override suspend fun dispatchUpdateNotifications(tableId: Int) {}

    override fun <T : Any> handleSyncEntitiesReceived(
        entityClass: KClass<T>,
        entitiesIncoming: List<T>
    ) {}

    override fun handleTableChanged(tableName: String) {}

    override suspend fun removeMirror(mirrorId: Int) {}

    override fun <T : Any> removeSyncListener(
        entityClass: KClass<T>,
        syncListener: SyncListener<T>
    ) {}

    override fun removeTableChangeListener(listener: TableChangeListener) {}

    override fun removeWeakConnectivityListener(listener: RepositoryConnectivityListener) {}

    override suspend fun updateMirrorPriorities(newPriorities: Map<Int, Int>) {}
}