package com.ustadmobile.core.db

/**
 * Represents the data layer for a given endpoint
 *
 * @param localDb the local UmAppDatabase
 * @param repository if the associated endpoint is a remote endpoint, then the repository, otherwise
 *        null. This will be non-null on Web, Desktop (except local accounts), and Android (except
 *        local accounts). On the server this will be null. It will also be null if the user creates
 *        a local (only) account on Android or Desktop.
 */
class UmAppDataLayer(
    val localDb: UmAppDatabase,
    val repository: UmAppDatabase?
) {
    fun requireRepository(message: String? = null) = repository
        ?: throw IllegalStateException("Repo required: not available: ${message ?: ""}")

    /**
     * Returns the repository if non-null, otherwise fallback to using the local db
     */
    val repositoryOrLocalDb: UmAppDatabase
        get() = repository ?: localDb
}
