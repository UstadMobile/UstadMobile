package com.ustadmobile.centralappconfigdb.datasource

import com.ustadmobile.centralappconfigdb.sqlite.CentralAppConfigDb
import com.ustadmobile.xxhashkmp.XXStringHasher

class CentralAppConfigDbDataSourceSqlDelight(
    private val centralAppConfigDb: CentralAppConfigDb,
    private val xxStringHasher: XXStringHasher
): CentralAppConfigDbDataSource {

    override val learningSpaceDataSource: LearningSpaceDataSource by lazy {
        LearningSpaceInfoDataSourceSqlDelight(centralAppConfigDb.learningSpaceQueries, xxStringHasher)
    }

    companion object {

        /**
         * Recommended default filename for SQLite database
         */
        const val CENTRAL_APP_CONFIG_DB_DEFAULT_FILENAME = "centralappconfig.db"
    }

}
