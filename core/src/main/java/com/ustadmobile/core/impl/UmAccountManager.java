package com.ustadmobile.core.impl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.UmAccount;

public class UmAccountManager {

    private static volatile UmAccount activeAccount;

    private static final String PREFKEY_PERSON_ID = "umaccount.personid";

    private static final String PREFKEY_USERNAME = "umaccount.username";

    private static final String PREFKEY_ACCESS_TOKEN = "umaccount.accesstoken";

    private static final String PREFKEY_ENDPOINT_URL = "umaccount.endpointurl";

    private static final String PREFKEY_FINGERPRINT_USERNAME = "umaccount.fingerprintusername";

    private static final String PREFKEY_FINGERPRINT_PERSON_ID = "umaccount.fingerprintpersonid";

    private static final String PREFKEY_PASSWORD_HASH = "umaccount.passwordhash";

    private static final String PREFKEY_PASSWORD_HASH_PERSONUID = "umaccount.passwordhashpersonuid";

    private static final String PREFKEY_PASSWORD_HASH_USERNAME = "umaccount.passwordhashusername";

    private static final String PREFKEY_FINGERPRIT_ACCESS_TOKEN = "umaccount.fingerprintaccesstoken";


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

    public static synchronized void setFingerprintUsername(String username, Object context,
                                                           UstadMobileSystemImpl impl){
        impl.setAppPref(PREFKEY_FINGERPRINT_USERNAME, username, context);
    }
    public static synchronized String getFingerprintUsername(Object context,
                                                             UstadMobileSystemImpl impl){
        return impl.getAppPref(PREFKEY_FINGERPRINT_USERNAME, context);
    }

    public static synchronized void setFingerprintPersonId(Long personId, Object context,
                                                           UstadMobileSystemImpl impl){
        impl.setAppPref(PREFKEY_FINGERPRINT_PERSON_ID, String.valueOf(personId), context);
    }
    public static synchronized String getFingerprintPersonId(Object context,
                                                             UstadMobileSystemImpl impl){
        return impl.getAppPref(PREFKEY_FINGERPRINT_PERSON_ID, context);
    }

    public static synchronized String getFingerprintAuth(Object context,
                                                       UstadMobileSystemImpl impl){
        return impl.getAppPref(PREFKEY_FINGERPRIT_ACCESS_TOKEN, context);
    }

    public static synchronized void setFringerprintAuth(String auth, Object context,
                                                        UstadMobileSystemImpl impl){
        impl.setAppPref(PREFKEY_FINGERPRIT_ACCESS_TOKEN, auth, context);
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

    public static void updateCredCache(String username, Long personUid, String passwordHash, Object context,
                                       UstadMobileSystemImpl impl){
        impl.setAppPref(PREFKEY_PASSWORD_HASH, passwordHash, context);
        impl.setAppPref(PREFKEY_PASSWORD_HASH_PERSONUID, String.valueOf(personUid), context);
        impl.setAppPref(PREFKEY_PASSWORD_HASH_USERNAME, username, context);
    }

    public static boolean checkCredCache(String username, String passwordHash, Object context,
                                      UstadMobileSystemImpl impl){
        if(username.equals(impl.getAppPref(PREFKEY_PASSWORD_HASH_USERNAME, context))){
            if(passwordHash.equals(impl.getAppPref(PREFKEY_PASSWORD_HASH, context))){
                return true;
            }
        }
        return false;
    }

    public static void updatePasswordHash(String password, Object context, UstadMobileSystemImpl impl){
        impl.setAppPref(PREFKEY_PASSWORD_HASH, password,context);
    }

    public static Long getCachedPersonUid(Object context, UstadMobileSystemImpl impl){
        return Long.parseLong(impl.getAppPref(PREFKEY_PASSWORD_HASH_PERSONUID, context));
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
