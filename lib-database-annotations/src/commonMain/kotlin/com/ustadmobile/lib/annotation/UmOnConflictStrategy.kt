package com.ustadmobile.lib.database.annotation

/**
 * Created by mike on 1/24/18.
 */
annotation class UmOnConflictStrategy {
    companion object {

        //Same as Android Room OnConflictStrategy

        /**
         * OnConflict strategy constant to replace the old data and continue the transaction.
         */
        const val REPLACE = 1
        /**
         * OnConflict strategy constant to rollback the transaction.
         */
        const val ROLLBACK = 2
        /**
         * OnConflict strategy constant to abort the transaction.
         */
        const val ABORT = 3
        /**
         * OnConflict strategy constant to fail the transaction.
         */
        const val FAIL = 4
        /**
         * OnConflict strategy constant to ignore the conflict.
         */
        const val IGNORE = 5
    }

}
