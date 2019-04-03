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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Actor actor = (Actor) o;

        if (name != null ? !name.equals(actor.name) : actor.name != null) return false;
        if (mbox != null ? !mbox.equals(actor.mbox) : actor.mbox != null) return false;
        if (mbox_sha1sum != null ? !mbox_sha1sum.equals(actor.mbox_sha1sum) : actor.mbox_sha1sum != null)
            return false;
        if (openid != null ? !openid.equals(actor.openid) : actor.openid != null) return false;
        if (objectType != null ? !objectType.equals(actor.objectType) : actor.objectType != null)
            return false;
        if (member != null ? !member.equals(actor.member) : actor.member != null) return false;
        return account != null ? account.equals(actor.account) : actor.account == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (mbox != null ? mbox.hashCode() : 0);
        result = 31 * result + (mbox_sha1sum != null ? mbox_sha1sum.hashCode() : 0);
        result = 31 * result + (openid != null ? openid.hashCode() : 0);
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        result = 31 * result + (member != null ? member.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);
        return result;
    }
}
