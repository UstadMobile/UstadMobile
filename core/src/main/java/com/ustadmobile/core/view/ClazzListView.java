package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * ClassList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzListView extends UstadView {

    String VIEW_NAME = "ClassList";

    String ARGS_CLAZZLOG_UID = "clazzloguid";
    String TAG_STATUS = "status";
    String ARG_LOGDATE = "logdate";

    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider);

}
