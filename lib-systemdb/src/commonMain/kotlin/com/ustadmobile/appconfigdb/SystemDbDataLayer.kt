package com.ustadmobile.appconfigdb


class SystemDbDataLayer(
    val localDb: SystemDb,
    val repository: SystemDb?
) {
    fun requireRepository(message: String? = null) = repository
        ?: throw IllegalStateException("Repo required: not available: ${message ?: ""}")

    /**
     * Returns the repository if non-null, otherwise fallback to using the local db
     */
    val repositoryOrLocalDb: SystemDb
        get() = repository ?: localDb
}
