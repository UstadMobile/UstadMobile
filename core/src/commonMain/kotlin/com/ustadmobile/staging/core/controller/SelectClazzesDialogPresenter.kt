package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ArgumentUtil
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLASSES_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.SelectClazzesDialogView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The SelectClazzesDialog Presenter.
 */
class SelectClazzesDialogPresenter(context: Any, arguments: Map<String, String>?,
                                   view: SelectClazzesDialogView) :
        UstadBaseController<SelectClazzesDialogView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var clazzWithEnrollmentUmProvider: DataSource.Factory<Int, ClazzWithNumStudents>? = null
    private var locations: List<Long>? = null
    var clazzes: HashMap<String, Long>? = null
    var selectedClazzesList: List<Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        if (arguments!!.containsKey(ARG_LOCATIONS_SET)) {
            locations = ArgumentUtil.convertCSVStringToLongList(arguments.get(ARG_LOCATIONS_SET)!!)
        }

        if (arguments!!.containsKey(ARG_CLASSES_SET)) {
            selectedClazzesList = ArgumentUtil.convertCSVStringToLongList(arguments.get(ARG_CLASSES_SET)!!)
        }

    }

    fun addToClazzes(clazzUid: Clazz) {
        if (!clazzes!!.containsKey(clazzUid.clazzName)) {
            clazzes!![clazzUid.clazzName!!] = clazzUid.clazzUid
        }
    }

    fun removeFromClazzes(clazzUid: Clazz) {
        if (clazzes!!.containsKey(clazzUid.clazzName)) {
            clazzes!!.remove(clazzUid.clazzName)
        }
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        clazzes = HashMap()

        //Find the provider
        if (locations != null && !locations!!.isEmpty()) {
            GlobalScope.launch {
                clazzWithEnrollmentUmProvider = repository.clazzDao.findAllClazzesInLocationList(locations!!)
                view.setClazzListProvider(clazzWithEnrollmentUmProvider!!)
            }
        } else {
            clazzWithEnrollmentUmProvider = repository.clazzDao
                    .findAllClazzes()
            view.setClazzListProvider(clazzWithEnrollmentUmProvider!!)
        }


    }

    fun handleCommonPressed(arg: Any) {
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish()
    }
}
