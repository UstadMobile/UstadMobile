package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.List;

@UmDao
public abstract class PersonDetailPresenterFieldDao {

    @UmQuery("SELECT * FROM PersonDetailPresenterField ORDER BY fieldIndex")
    public abstract void findAllPersonDetailPresenterFields(UmCallback<List<PersonDetailPresenterField>> callback);

}
