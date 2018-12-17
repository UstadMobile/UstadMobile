package com.ustadmobile.lib.db;

public interface UmDbWithAuthenticator {

    boolean validateAuth(long userUid, String auth);

}
