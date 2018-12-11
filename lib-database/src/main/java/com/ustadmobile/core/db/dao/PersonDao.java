package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;


@UmDao(readPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
public abstract class PersonDao implements SyncableDao<Person, PersonDao> {


    @Override
    @UmInsert
    public abstract long insert(Person entity);

    @UmUpdate
    public abstract void update(Person entity);

    @UmUpdate
    public abstract void updateAsync(Person entity, UmCallback<Integer> resultObject);

    @UmInsert
    @Override
    public abstract void insertAsync(Person entity, UmCallback<Long> resultObject);

    @Override
    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract Person findByUid(long uid);

    @UmQuery("SELECT * From Person where username = :username")
    public abstract void findByUsernameAsync(String username, UmCallback<Person> resultObject);

    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Person> person);

    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract UmLiveData<Person> findByUidLive(long uid);

    @UmQuery("SELECT * From Person")
    public abstract UmProvider<Person> findAllPeopleAsync();

    @UmQuery("SELECT Person.* , (0) AS clazzUid, " +
            " (0) AS attendancePercentage, " +
            " (0) AS clazzMemberRole, " +
            " (0) AS enrolled FROM Person WHERE Person.active = 1 ")
    public abstract UmProvider<PersonWithEnrollment> findAllPeopleWithEnrollment();

    @UmQuery("SELECT * From Person WHERE username = :username AND passwordHash = :passwordHash")
    public abstract Person authenticateHash(String username, String passwordHash);

    @UmQuery("SELECT * From Person WHERE username = :username AND passwordHash = :passwordHash")
    public abstract void authenticateHashAsync(String username, String passwordHash,
                                               UmCallback<Person> person);

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

    @UmQuery("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token " +
            " and accessTokenPersonUid = :personUid)")
    public abstract boolean isValidToken(String token, long personUid);

    @UmInsert
    public abstract void insertAccessToken(AccessToken token);


    @UmQuery("SELECT personUid, passwordHash FROM Person WHERE username = :username")
    public abstract void findUidAndPasswordHash(String username,
                                                UmCallback<PersonUidAndPasswordHash> callback);


}
