package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonWithEnrollment;

@UmDao
public abstract class PersonDao implements BaseDao<Person>{

    @Override
    @UmInsert
    public abstract long insert(Person entity);

    @UmUpdate
    public abstract int update(Person entity);

    @UmUpdate
    public abstract void updateAsync(Person entity, UmCallback<Integer> result);

    @UmInsert
    @Override
    public abstract void insertAsync(Person entity, UmCallback<Long> result);

    @Override
    @UmQuery("SELECT * From Person WHERE personUid = :uid")
    public abstract Person findByUid(long uid);

    @UmQuery("SELECT * From Person where username = :username")
    public abstract void findByUsernameAsync(String username, UmCallback<Person> result);

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

}
