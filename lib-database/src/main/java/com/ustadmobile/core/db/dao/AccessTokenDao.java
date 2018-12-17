package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;

@UmDao
public abstract class AccessTokenDao {

    @UmQuery("SELECT EXISTS(SELECT token FROM AccessToken WHERE accessTokenPersonUid = :personUid AND token = :token)")
    public abstract boolean isValidToken(long personUid, String token);

}
