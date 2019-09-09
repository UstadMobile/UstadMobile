package com.ustadmobile.core.view


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

/**
 * SelectClazzesDialog Core View extends Core UstadView. Will be implemented
 * on various implementations.
 */
interface SelectClazzesDialogView : UstadView {


    /**
     * For Android: closes the activity.
     */
    fun finish()

    /**
     * Clazz list provider to the view.
     * @param clazzListProvider The provider
     */
    fun setClazzListProvider(clazzListProvider: DataSource.Factory<Int, ClazzWithNumStudents>)

    companion object {

        val VIEW_NAME = "SelectClazzesDialog"
    }

}
