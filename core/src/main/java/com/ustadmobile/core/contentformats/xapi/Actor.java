package com.ustadmobile.core.contentformats.xapi;

import java.util.List;

public class Actor {

    private String name;

    private String mbox;

    private String mbox_sha1sum;

    private String openid;

    private String objectType;

    private List<Actor> member;

    private Account account;

    public class Account {

        private String name;

        private String homePage;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHomePage() {
            return homePage;
        }

        public void setHomePage(String homePage) {
            this.homePage = homePage;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMbox() {
        return mbox;
    }

    public void setMbox(String mbox) {
        this.mbox = mbox;
    }

    public String getMbox_sha1sum() {
        return mbox_sha1sum;
    }

    public void setMbox_sha1sum(String mbox_sha1sum) {
        this.mbox_sha1sum = mbox_sha1sum;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public List<Actor> getMembers() {
        return member;
    }

    public void setMembers(List<Actor> members) {
        this.member = members;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
