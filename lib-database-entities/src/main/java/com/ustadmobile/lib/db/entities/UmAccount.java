package com.ustadmobile.lib.db.entities;


public class UmAccount {

    private static volatile UmAccount activeAccount;

    private long personUid;

    private String username;

    private String accessToken;

    private String endpointUrl;

    private static final String PREFKEY_PERSON_ID = "umaccount.personid";

    private static final String PREFKEY_USERNAME = "umaccount.username";

    private static final String PREFKEY_ACCESS_TOKEN = "umaccount.accesstoken";

    private static final String PREFKEY_ENDPOINT_URL = "umaccount.endpointurl";

    public UmAccount(long personUid, String username, String accessToken, String endpointUrl) {
        this.personUid = personUid;
        this.username = username;
        this.accessToken = accessToken;
        this.endpointUrl = endpointUrl;
    }

//    public static synchronized UmAccount getActiveAccount(Object context, UstadMobileSystemImpl impl) {
//        if(activeAccount == null) {
//            long personUid = Long.parseLong(impl.getAppPref(PREFKEY_PERSON_ID, "0", context));
//            if(personUid == 0)
//                return null;
//
//            activeAccount = new UmAccount(personUid, impl.getAppPref(PREFKEY_USERNAME, context),
//                    impl.getAppPref(PREFKEY_ACCESS_TOKEN, context),
//                    impl.getAppPref(PREFKEY_ENDPOINT_URL, context));
//        }
//
//        return activeAccount;
//    }
//
//    public static UmAccount getActiveAccount(Object context) {
//        return getActiveAccount(context, UstadMobileSystemImpl.getInstance());
//    }
//
//    public static synchronized void setActiveAccount(UmAccount account, Object context,
//                                              UstadMobileSystemImpl impl) {
//        activeAccount = account;
//        if(account != null) {
//            impl.setAppPref(PREFKEY_PERSON_ID, String.valueOf(account.getPersonUid()), context);
//            impl.setAppPref(PREFKEY_USERNAME, account.getUsername(), context);
//            impl.setAppPref(PREFKEY_ACCESS_TOKEN, account.getAccessToken(), context);
//            impl.setAppPref(PREFKEY_ENDPOINT_URL, account.getEndpointUrl(), context);
//        }else {
//            impl.setAppPref(PREFKEY_PERSON_ID, "0", context);
//            impl.setAppPref(PREFKEY_USERNAME, null, context);
//            impl.setAppPref(PREFKEY_ACCESS_TOKEN, null, context);
//            impl.setAppPref(PREFKEY_ENDPOINT_URL, null, context);
//        }
//    }
//
//    public static void setActiveAccount(UmAccount account, Object context) {
//        setActiveAccount(account, context, UstadMobileSystemImpl.getInstance());
//    }

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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}
