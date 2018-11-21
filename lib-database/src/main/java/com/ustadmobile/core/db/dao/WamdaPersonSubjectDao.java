package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.WamdaPersonSubject;
import com.ustadmobile.lib.db.entities.WamdaPersonSubjectInfo;

import java.util.List;

@UmDao
public abstract class WamdaPersonSubjectDao {

    @UmInsert
    public abstract void insert(WamdaPersonSubject personSubject, UmCallback<Long> callback);

    @UmQuery("DELETE FROM WamdaPersonSubject WHERE subjectUid = :subjectUid")
    public abstract void deleteBySubjectUid(long subjectUid);

    @UmQuery("SELECT WamdaSubject.*," +
            "(SELECT COUNT(*) FROM WamdaPersonSubject " +
            "WHERE WamdaPersonSubject.subjectUid = WamdaSubject.subjectUid " +
            "AND WamdaPersonSubject.personUid = :personUid) as selected " +
            "FROM WamdaSubject ORDER BY WamdaSubject.subjectName")
    public abstract UmProvider<WamdaPersonSubjectInfo> findByPersonUidAsync(long personUid);

    @UmQuery("SELECT WamdaSubject.*," +
            "(SELECT COUNT(*) FROM WamdaPersonSubject " +
            "WHERE WamdaPersonSubject.subjectUid = WamdaSubject.subjectUid " +
            "AND WamdaPersonSubject.personUid = :personUid) as selected " +
            "FROM WamdaSubject ORDER BY WamdaSubject.subjectName")
    public abstract List<WamdaPersonSubjectInfo> findByPersonUid(long personUid);
}
