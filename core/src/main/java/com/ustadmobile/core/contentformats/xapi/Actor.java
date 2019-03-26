package com.ustadmobile.core.contentformats.xapi;

import java.util.List;

public class Actor {

    private String name;

    private String mbox;

    private String mbox_sha1sum;

    private String openid;

    private String objectType;

    private List<Actor> members;

    private Account account;

    private class Account {

        private String name;

        private String homePage;
    }
}
