package com.ustadmobile.lib.db.entities;

public class GroupWithMemberCount extends PersonGroup {

    int memberCount;

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
