package com.ustadmobile.core.impl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.UmAccount;

public class UmAccountManager {

    private static volatile UmAccount activeAccount;

    private static final String PREFKEY_PERSON_ID = "umaccount.personid";

    private static final String PREFKEY_USERNAME = "umaccount.username";

    private static final String PREFKEY_ACCESS_TOKEN = "umaccount.accesstoken";

    private static final String PREFKEY_ENDPOINT_URL = "umaccount.endpointurl";

    public static synchronized UmAccount getActiveAccount(Object context, UstadMobileSystemImpl impl) {
        if(activeAccount == null) {
            long personUid = Long.parseLong(impl.getAppPref(PREFKEY_PERSON_ID, "0", context));
            if(personUid == 0)
                return null;

            activeAccount = new UmAccount(personUid, impl.getAppPref(PREFKEY_USERNAME, context),
                    impl.getAppPref(PREFKEY_ACCESS_TOKEN, context),
                    impl.getAppPref(PREFKEY_ENDPOINT_URL, context));
        }

        return activeAccount;
    }

    public static synchronized long getActivePersonUid(Object context, UstadMobileSystemImpl impl) {
        UmAccount activeAccount = getActiveAccount(context, impl);
        return activeAccount != null ? activeAccount.getPersonUid() : 0L;
    }

    public static synchronized long getActivePersonUid(Object context) {
        return getActivePersonUid(context, UstadMobileSystemImpl.getInstance());
    }

    public static UmAccount getActiveAccount(Object context) {
        return getActiveAccount(context, UstadMobileSystemImpl.getInstance());
    }

    public static synchronized void setActiveAccount(UmAccount account, Object context,
                                                     UstadMobileSystemImpl impl) {
        activeAccount = account;
        if(account != null) {
            impl.setAppPref(PREFKEY_PERSON_ID, String.valueOf(account.getPersonUid()), context);
            impl.setAppPref(PREFKEY_USERNAME, account.getUsername(), context);
            impl.setAppPref(PREFKEY_ACCESS_TOKEN, account.getAuth(), context);
            impl.setAppPref(PREFKEY_ENDPOINT_URL, account.getEndpointUrl(), context);
        }else {
            impl.setAppPref(PREFKEY_PERSON_ID, "0", context);
            impl.setAppPref(PREFKEY_USERNAME, null, context);
            impl.setAppPref(PREFKEY_ACCESS_TOKEN, null, context);
            impl.setAppPref(PREFKEY_ENDPOINT_URL, null, context);
        }
    }

    public static void setActiveAccount(UmAccount account, Object context) {
        setActiveAccount(account, context, UstadMobileSystemImpl.getInstance());
    }

    public static UmAppDatabase getRepositoryForActiveAccount(Object context) {
        if(activeAccount == null)
            return UmAppDatabase.getInstance(context).getRepository(
                    UstadMobileSystemImpl.getInstance().getAppConfigString("apiUrl",
                            "http://localhost", context), "");

        UmAccount activeAccount =getActiveAccount(context);
        return UmAppDatabase.getInstance(context).getRepository(activeAccount.getEndpointUrl(),
                activeAccount.getAuth());
    }

    public static String getActiveEndpoint(Object context) {
        UmAccount activeAccount = getActiveAccount(context);
        if(activeAccount != null) {
            return activeAccount.getEndpointUrl();
        }else {
            return UstadMobileSystemImpl.getInstance().getAppConfigString("apiUrl",
                    "http://localhost", context);
        }
    }

}
