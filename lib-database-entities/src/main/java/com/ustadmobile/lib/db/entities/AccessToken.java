package com.ustadmobile.lib.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;


import java.util.UUID;

@UmEntity
@Entity
public class AccessToken {

    @UmPrimaryKey
    @PrimaryKey
    @NonNull
    private String token;

    private long accessTokenPersonUid;

    private long expires;

    public AccessToken() {
        token = "";
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
