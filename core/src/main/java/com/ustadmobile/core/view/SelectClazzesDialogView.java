package com.ustadmobile.core.view;


import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;

/**
 * SelectClazzesDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
public interface SelectClazzesDialogView extends UstadView {

    String VIEW_NAME = "SelectClazzesDialog";


    /**
     * For Android: closes the activity.
     */
    void finish();

    /**
     * Clazz list provider to the view.
     * @param clazzListProvider The provider
     */
    void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider);

}
