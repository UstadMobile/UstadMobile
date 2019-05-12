package com.ustadmobile.lib.db.entities;


public class UmAccount {

    private long personUid;

    private String username;

    private String auth;

    private String endpointUrl;

    public UmAccount(long personUid, String username, String auth, String endpointUrl) {
        this.personUid = personUid;
        this.username = username;
        this.auth = auth;
        this.endpointUrl = endpointUrl;
    }

    public long getPersonUid() {
        return personUid;
    }

    public void setPersonUid(long personUid) {
        this.personUid = personUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}