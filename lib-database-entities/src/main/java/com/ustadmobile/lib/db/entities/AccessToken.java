package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

import java.util.UUID;

@UmEntity
public class AccessToken {

    @UmPrimaryKey
    private String token;

    private long accessTokenPersonUid;

    private long expires;

    public AccessToken() {

    }

    public AccessToken(long personUid, long expires) {
        token = UUID.randomUUID().toString();
        this.accessTokenPersonUid = personUid;
        this.expires = expires;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAccessTokenPersonUid() {
        return accessTokenPersonUid;
    }

    public void setAccessTokenPersonUid(long accessTokenPersonUid) {
        this.accessTokenPersonUid = accessTokenPersonUid;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }
}
