package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonAuth

@Dao
@UmRepository
abstract class PersonAuthDao : BaseDao<PersonAuth> {
    companion object {

        const val ENCRYPTED_PASS_PREFIX = "e:"

        const val PLAIN_PASS_PREFIX = "p:"

    }


}
