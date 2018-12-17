package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.Random;

import static com.ustadmobile.core.db.dao.PersonAuthDao.ENCRYPTED_PASS_PREFIX;


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
                }else if(person.getPasswordHash().startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                        && !person.getPasswordHash().substring(2).equals(password)) {
                    callback.onSuccess(null);
                }else if(person.getPasswordHash().startsWith(ENCRYPTED_PASS_PREFIX)
                        && !PersonAuthDao.authenticateEncryptedPassword(password,
                            person.getPasswordHash().substring(2))) {
                    callback.onSuccess(null);
                }else {
                    onSuccessCreateAccessToken(person.getPersonUid(), username, callback);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @UmRestAccessible
    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
    public void register(Person newPerson, String password, UmCallback<UmAccount> callback) {
        findUidAndPasswordHash(newPerson.getUsername(), new UmCallback<PersonUidAndPasswordHash>() {
            @Override
            public void onSuccess(PersonUidAndPasswordHash result) {
                if(result == null) {
                    //OK to go ahead and create
                    long personUid = getAndIncrementPrimaryKey();
                    newPerson.setPersonUid(personUid);
                    insert(newPerson);
                    PersonAuth newPersonAuth = new PersonAuth(newPerson.getPersonUid(),
                            ENCRYPTED_PASS_PREFIX + PersonAuthDao.encryptPassword(password));
                    insertPersonAuth(newPersonAuth);
                    onSuccessCreateAccessToken(newPerson.getPersonUid(), newPerson.getUsername(),
                            callback);
                }else {
                    callback.onFailure(new IllegalArgumentException("Username already exists"));
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    protected long getAndIncrementPrimaryKey() {
        if(getDeviceBits() == 0)
            insertDeviceBits(new SyncDeviceBits(new Random().nextInt()));

        if(getSequenceNum() == 0)
            insertSyncablePk(new SyncablePrimaryKey(Person.TABLE_ID, 1));

        long primaryKey = getPrimaryKey();
        incrementPrimaryKey();
        return primaryKey;
    }

    @UmQuery("UPDATE SyncablePrimaryKey SET sequenceNumber = sequenceNumber + 1 WHERE tableId = " + Person.TABLE_ID)
    protected abstract void incrementPrimaryKey();

    @UmQuery("SELECT (((SELECT deviceBits FROM SyncDeviceBits WHERE id = " + SyncDeviceBits.PRIMARY_KEY + ") << 32) \n" +
            "           | (SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = " + Person.TABLE_ID +" )) AS newPrimaryKey")
    protected abstract long getPrimaryKey();

    @UmQuery("SELECT sequenceNumber FROM SyncablePrimaryKey WHERE tableId = " + Person.TABLE_ID)
    protected abstract int getSequenceNum();

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    protected abstract void insertSyncablePk(SyncablePrimaryKey syncablePrimaryKey);

    @UmQuery("SELECT deviceBits FROM SyncDeviceBits WHERE id = " + SyncDeviceBits.PRIMARY_KEY)
    public abstract long getDeviceBits();

    @UmInsert
    public abstract void insertDeviceBits(SyncDeviceBits deviceBits);

    protected void onSuccessCreateAccessToken(long personUid, String username, UmCallback<UmAccount> callback) {
        AccessToken accessToken = new AccessToken(personUid,
                System.currentTimeMillis() + SESSION_LENGTH);
        insertAccessToken(accessToken);
        callback.onSuccess(new UmAccount(personUid, username, accessToken.getToken(),
                null));
    }

    public boolean authenticate(String token, long personUid) {
        return isValidToken(token, personUid);
    }

    @UmQuery("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token and accessTokenPersonUid = :personUid)")
    public abstract boolean isValidToken(String token, long personUid);

    @UmInsert
    public abstract void insertAccessToken(AccessToken token);


    @UmQuery("SELECT Person.personUid, PersonAuth.passwordHash " +
            " FROM Person LEFT JOIN PersonAuth ON Person.personUid = PersonAuth.personAuthUid " +
            "WHERE Person.username = :username")
    public abstract void findUidAndPasswordHash(String username,
                                                UmCallback<PersonUidAndPasswordHash> callback);

    @UmInsert
    public abstract void insertPersonAuth(PersonAuth personAuth);

}
