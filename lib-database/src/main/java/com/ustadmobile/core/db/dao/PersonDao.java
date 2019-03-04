package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.AccessToken;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;
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

    //For debugging
    @UmQuery("SELECT * FROM Person")
    public abstract List<Person> findAll();

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

    @UmQuery("SELECT * From Person where username = :username")
    public abstract Person findByUsername(String username);

    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<Person> person);

    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract UmLiveData<Person> findByUidLive(long uid);

    @UmQuery("SELECT * From Person")
    public abstract UmProvider<Person> findAllPeopleAsync();

    @UmQuery("SELECT Person.* , (0) AS clazzUid, " +
            " (0) AS attendancePercentage, " +
            " (0) AS clazzMemberRole, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            " PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
            " DESC LIMIT 1) AS personPictureUid, " +
            " (0) AS enrolled FROM Person WHERE Person.active = 1 ")
    public abstract UmProvider<PersonWithEnrollment> findAllPeopleWithEnrollment();


    @UmQuery("SELECT Person.* , (0) AS clazzUid, " +
            " (0) AS attendancePercentage, " +
            " (0) AS clazzMemberRole, " +
            " (SELECT PersonPicture.personPictureUid FROM PersonPicture WHERE " +
            " PersonPicture.personPicturePersonUid = Person.personUid ORDER BY picTimestamp " +
            " DESC LIMIT 1) AS personPictureUid, " +
            " (0) AS enrolled FROM Person WHERE Person.active = 1 " +
            " AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery " )
    public abstract UmProvider<PersonWithEnrollment> findAllPeopleWithEnrollmentBySearch(String searchQuery);

    @UmQuery("SELECT * FROM Person WHERE admin = 1")
    public abstract List<Person> findAllAdminsAsList();

    protected static final String ENTITY_LEVEL_PERMISSION_CONDITION1 = " Person.personUid = :accountPersonUid OR" +

            "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) OR " +
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

    protected boolean checkUserAuth(String passwordProvided, String passwordHash) {
        if (passwordHash == null) {
            return false;
        } else if (passwordHash.startsWith(PersonAuthDao.PLAIN_PASS_PREFIX)
                && passwordHash.substring(2).equals(passwordProvided)) {
            return true;
        }else if(passwordHash.startsWith(PersonAuthDao.ENCRYPTED_PASS_PREFIX)) {
            return PersonAuthDao.authenticateEncryptedPassword(passwordProvided,
                    passwordHash.substring(2));
        }else {
            return PersonAuthDao.authenticateEncryptedPassword(passwordProvided,
                    passwordHash);
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

    @UmRestAccessible
    public String createAdmin() {
        Person adminPerson = findByUsername("admin");
        if(adminPerson == null) {
            adminPerson = new Person();
            adminPerson.setAdmin(true);
            adminPerson.setUsername("admin");
            adminPerson.setPersonUid(getAndIncrementPrimaryKey());
            adminPerson.setFirstNames("Admin");
            adminPerson.setLastName("Admin");

            insert(adminPerson);

            PersonAuth adminPersonAuth = new PersonAuth(adminPerson.getPersonUid(),
                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                            PersonAuthDao.encryptPassword("irZahle2"));
            insertPersonAuth(adminPersonAuth);

            return "Created";
        }else {
            return "Already created";
        }
    }

    @UmQuery("SELECT * FROM Clazz")
    public abstract List<Clazz> findAllClazzes();

    @UmQuery("SELECT * FROM Role WHERE roleName=:roleName")
    public abstract Role findRoleByName(String roleName);

    @UmInsert
    public abstract long insertEntityRole(EntityRole entityRole);



    @UmRestAccessible
    protected void setUserPassword(String adminUsername, String adminPassword, String userUsername,
                                     String userPassword, UmCallback<String> callback) {
        //validate the admin
        Person adminPerson = findByUsername(adminUsername);
        if(adminPerson == null || !adminPerson.isAdmin()) {
            callback.onFailure(new IllegalArgumentException("Admin user not found or not admin"));
            return;
        }

        findUidAndPasswordHash(adminUsername, new UmCallback<PersonUidAndPasswordHash>() {
            @Override
            public void onSuccess(PersonUidAndPasswordHash result) {
                if(result == null) {
                    callback.onFailure(new IllegalArgumentException("no auth object found for admin"));
                }else if(checkUserAuth(adminPassword, result.getPasswordHash())){
                    Person userPerson = findByUsername(userUsername);
                    if(userPerson == null) {
                        callback.onFailure(new IllegalArgumentException("Username not found"));
                    }else {
                        replacePersonAuth(new PersonAuth(userPerson.getPersonUid(),
                                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                                        PersonAuthDao.encryptPassword(userPassword)));
                        callback.onSuccess("Changed password for user: " + userUsername);
                    }
                }else {
                    callback.onFailure(new IllegalArgumentException("Invalid authentication for user"));
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

    @UmQuery("SELECT EXISTS(SELECT token FROM AccessToken WHERE token = :token " +
            " and accessTokenPersonUid = :personUid)")
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

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void replacePersonAuth(PersonAuth personAuth);

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


    @UmInsert
    public abstract void insertPersonGroup(PersonGroup personGroup, UmCallback<Long> callback);

    @UmInsert
    public abstract void insertPersonGroupMember(PersonGroupMember personGroupMember,
                                                 UmCallback<Long> callback);


    /**
     * Creates actual person and assigns it to a group for permissions' sake. Use this
     * instead of direct insert.
     *
     * @param person    The person entity
     * @param callback  The callback.
     */
    public void createPersonAsync(Person person, UmCallback<Long> callback){
        insertAsync(person, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long personUid) {
                person.setPersonUid(personUid);

                PersonGroup personGroup = new PersonGroup();
                personGroup.setGroupName(person.getFirstNames()!= null?person.getFirstNames():""
                                + "'s group");
                insertPersonGroup(personGroup, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long personGroupUid) {
                        personGroup.setGroupUid(personGroupUid);

                        PersonGroupMember personGroupMember = new PersonGroupMember();
                        personGroupMember.setGroupMemberPersonUid(personUid);
                        personGroupMember.setGroupMemberGroupUid(personGroupUid);
                        insertPersonGroupMember(personGroupMember, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long personGroupMemberUid) {
                                personGroupMember.setGroupMemberUid(personGroupMemberUid);
                                callback.onSuccess(personUid);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                callback.onFailure(exception);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public class PersonWithGroup{
        long personUid;
        long personGroupUid;

        PersonWithGroup(long personid, long groupid){
            this.personUid = personid;
            this.personGroupUid = groupid;
        }

        public long getPersonUid() {
            return personUid;
        }

        public void setPersonUid(long personUid) {
            this.personUid = personUid;
        }

        public long getPersonGroupUid() {
            return personGroupUid;
        }

        public void setPersonGroupUid(long personGroupUid) {
            this.personGroupUid = personGroupUid;
        }
    }

    /**
     * Crate person
     *
     * @param person
     * @param callback
     */
    public void createPersonWithGroupAsync(Person person, UmCallback<PersonWithGroup> callback){
        insertAsync(person, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long personUid) {
                person.setPersonUid(personUid);

                PersonGroup personGroup = new PersonGroup();
                personGroup.setGroupName(person.getFirstNames()!= null?person.getFirstNames():""
                        + "'s group");
                insertPersonGroup(personGroup, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long personGroupUid) {
                        personGroup.setGroupUid(personGroupUid);

                        PersonGroupMember personGroupMember = new PersonGroupMember();
                        personGroupMember.setGroupMemberPersonUid(personUid);
                        personGroupMember.setGroupMemberGroupUid(personGroupUid);
                        insertPersonGroupMember(personGroupMember, new UmCallback<Long>() {
                            @Override
                            public void onSuccess(Long personGroupMemberUid) {
                                personGroupMember.setGroupMemberUid(personGroupMemberUid);
                                PersonWithGroup personWithGroup =
                                        new PersonWithGroup(personUid, personGroupUid);
                                callback.onSuccess(personWithGroup);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                callback.onFailure(exception);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    @UmQuery("SELECT Count(*) FROM Person")
    public abstract long countAll();

}

