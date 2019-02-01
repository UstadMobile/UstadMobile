package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.util.List;
import java.util.Random;

import static com.ustadmobile.core.db.dao.PersonAuthDao.ENCRYPTED_PASS_PREFIX;
import static com.ustadmobile.core.db.dao.PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION1;
import static com.ustadmobile.core.db.dao.PersonDao.ENTITY_LEVEL_PERMISSION_CONDITION2;


@UmDao(
selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_SELECT
        + ENTITY_LEVEL_PERMISSION_CONDITION2,
updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_PERSON_UPDATE
        + ENTITY_LEVEL_PERMISSION_CONDITION2)
@UmRepository
public abstract class PersonDao implements SyncableDao<Person, PersonDao> {

    protected static final String ENTITY_LEVEL_PERMISSION_CONDITION1 = " Person.personUid = :accountPersonUid OR" +
            "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) = 1 OR " +
            "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
            "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
            " AND (" +
            "(EntityRole.ertableId = " + Person.TABLE_ID +
            " AND EntityRole.erEntityUid = Person.personUid) " +
            "OR " +
            "(EntityRole.ertableId = " + Clazz.TABLE_ID +
            " AND EntityRole.erEntityUid IN (SELECT DISTINCT clazzMemberClazzUid FROM ClazzMember WHERE clazzMemberPersonUid = Person.personUid))" +
            "OR" +
            "(EntityRole.ertableId = " + Location.TABLE_ID +
            " AND EntityRole.erEntityUid IN " +
                "(SELECT locationAncestorAncestorLocationUid FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid " +
                "IN (SELECT personLocationLocationUid FROM PersonLocationJoin WHERE personLocationPersonUid = Person.personUid)))" +
            ") AND (Role.rolePermissions & ";

    protected static final String ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)";

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

    @UmInsert
    public abstract List<Long> insertListAndGetIds(List<Person> personList);


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


    /**
     * Checks if a user has the given permission over a given person in the database
     *
     * @param accountPersonUid the personUid of the person who wants to perform the operation
     * @param personUid the personUid of the person object in the database to perform the operation on
     * @param permission permission to check for
     * @param callback result callback
     */
    @UmQuery("SELECT 1 FROM Person WHERE Person.personUid = :personUid AND (" +
            ENTITY_LEVEL_PERMISSION_CONDITION1 + " :permission " + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") ")
    public abstract void personHasPermission(long accountPersonUid, long personUid, long permission,
                                    UmCallback<Boolean> callback);

    @UmQuery("SELECT Person.* FROM PERSON Where Person.username = :username")
    public abstract Person findByUsername(String username);

    @UmQuery("SELECT Count(*) FROM Person")
    public abstract long countAll();
}
