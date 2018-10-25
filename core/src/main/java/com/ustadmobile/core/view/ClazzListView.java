package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * ClassList Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface ClazzListView extends UstadView {

    String VIEW_NAME = "ClassList";

    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider);

}
