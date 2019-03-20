package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.AuditLog;
import com.ustadmobile.lib.db.entities.AuditLogWithNames;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

import java.util.List;

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
public abstract class AuditLogDao implements SyncableDao<AuditLog, AuditLogDao> {

    @UmInsert
    public abstract long insert(AuditLog entity);

    @UmInsert
    public abstract void insertAsync(AuditLog entity, UmCallback<Long> resultObject);

    @UmUpdate
    public abstract void update(AuditLog entity);

    @UmUpdate
    public abstract void updateAsync(AuditLog entity, UmCallback<Integer> resultObject);

    @UmQuery("SELECT * FROM AuditLog WHERE auditLogUid = :uid")
    public abstract AuditLog findByUid(long uid);

    @UmQuery("SELECT * FROM AuditLog WHERE auditLogUid = :uid")
    public abstract void findByUidAsync(long uid, UmCallback<AuditLog> resultObject);

    @UmQuery("SELECT * FROM AuditLog")
    public abstract UmProvider<AuditLog> findAllAuditLogs();

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithName();


    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime " +
            " AND Clazz.clazzUid IN (:clazzes) ")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByClazz(long fromTime,
                                                    long toTime, List<Long> clazzes);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime AND Actor.personUid IN (:actors)" )
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByActors(long fromTime,
                                                    long toTime, List<Long> actors);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime AND Person.personUid IN (:people)" )
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByPeople(long fromTime,
                                                                                         long toTime, List<Long> people);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime  " +
            " AND Clazz.clazzUid IN (:clazzes) " +
            " AND Actor.personUid IN (:actors)")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByActorsAndClazzes
            (long fromTime,long toTime, List<Long> actors, List<Long> clazzes);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime  " +
            " AND Person.personUid in (:people) " +
            " AND Actor.personUid IN (:actors)")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByActorsAndPeople
            (long fromTime,long toTime, List<Long> actors, List<Long> people);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime  " +
            " AND Clazz.clazzUid IN (:clazzes) " +
            " AND Person.personUid IN (:people)")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByPeopleAndClazzes
            (long fromTime,long toTime, List<Long> people, List<Long> clazzes);

    @UmQuery("SELECT AuditLog.*, Actor.firstNames || ' ' || Actor.lastName AS actorName, " +
            "Clazz.clazzName AS clazzName, Person.firstNames || ' ' || Person.lastName AS personName " +
            "FROM AuditLog LEFT JOIN Person AS Actor ON AuditLog.auditLogActorPersonUid = Actor.personUid " +
            "LEFT JOIN Clazz ON AuditLog.auditLogEntityUid = Clazz.clazzUid " +
            "LEFT JOIN Person ON AuditLog.auditLogEntityUid = Person.personUid " +
            "WHERE auditLogDate > :fromTime " +
            " AND auditLogDate < :toTime " +
            " AND Clazz.clazzUid IN (:clazzes) " +
            " AND Actor.personUid IN (:actors) " +
            " AND Person.personUid in (:people) ")
    public abstract UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeople
            (long fromTime,long toTime, List<Long> actors, List<Long> clazzes, List<Long> people);

    public UmProvider<AuditLogWithNames> findAllAuditLogsWithNameFilter(long fromTime, long toTime,
            List<Long> locations, List<Long> clazzes, List<Long> people, List<Long> actors){

        if( clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()){
            return findAllAuditLogsWithName();
        }else if(!clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByActorsAndClazzesAndPeople(fromTime, toTime,
                    actors, clazzes, people);
        }else if(!clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByPeopleAndClazzes(fromTime, toTime, people, clazzes);
        }else if(clazzes.isEmpty() && !people.isEmpty() && !actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByActorsAndPeople(fromTime, toTime, actors, people);
        }else if(!clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByActorsAndClazzes(fromTime, toTime, actors, clazzes);
        }else if( clazzes.isEmpty() && people.isEmpty() && !actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByActors(fromTime, toTime, actors);
        }else if(clazzes.isEmpty() && !people.isEmpty() && actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByPeople(fromTime, toTime, people);
        }else if(!clazzes.isEmpty() && people.isEmpty() && actors.isEmpty()){
            return findAllAuditLogsWithNameFilterByClazz(fromTime, toTime, clazzes);
        }else{
            return findAllAuditLogsWithName();
        }

    }


}
