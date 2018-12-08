package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;


@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonDao implements SyncableDao<Person, PersonDao> {

    public static final long SESSION_LENGTH = 28L * 24L * 60L * 60L * 1000L;// 28 days

    protected class PersonUidAndPasswordHash {
        String passwordHash;

        long personUid;

        public String getPasswordHash() {
            return passwordHash;
        }

        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }

        public long getPersonUid() {
            return personUid;
        }

        public void setPersonUid(long personUid) {
            this.personUid = personUid;
        }
    }

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public void login(String username, String password, UmCallback<UmAccount> callback) {
        findUidAndPasswordHash(username, new UmCallback<PersonUidAndPasswordHash>() {
            @Override
            public void onSuccess(PersonUidAndPasswordHash person) {
                if(person == null) {
                    callback.onSuccess(null);
                }else if(!person.getPasswordHash().equals(password)) {
                    callback.onSuccess(null);
                }else {
                    AccessToken accessToken = new AccessToken(person.getPersonUid(),
                            System.currentTimeMillis() + SESSION_LENGTH);
                    insertAccessToken(accessToken);
                    callback.onSuccess(new UmAccount(
                            person.getPersonUid(), username,
                            accessToken.getToken(), null));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    public boolean authenticate(String token, long personUid) {
        return isValidToken(token, personUid);
    }

    @UmQuery("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token and accessTokenPersonUid = :personUid)")
    public abstract boolean isValidToken(String token, long personUid);

    @UmInsert
    public abstract void insertAccessToken(AccessToken token);


    @UmQuery("SELECT personUid, passwordHash FROM Person WHERE username = :username")
    public abstract void findUidAndPasswordHash(String username,
                                                UmCallback<PersonUidAndPasswordHash> callback);

}
