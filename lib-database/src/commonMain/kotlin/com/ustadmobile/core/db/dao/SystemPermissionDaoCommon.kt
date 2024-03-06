package com.ustadmobile.core.db.dao

object SystemPermissionDaoCommon {

    const val SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1 = """
        EXISTS(SELECT 1
                 FROM SystemPermission
                WHERE :accountPersonUid != 0 
                  AND SystemPermission.spToPersonUid = :accountPersonUid
                  AND (SystemPermission.spPermissionsFlag &
    """

    const val SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2 = """
        ) > 0
                  AND NOT SystemPermission.spIsDeleted)
    """


    const val SELECT_SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL = """
        SELECT $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
          :permission
        $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2  
    """


}