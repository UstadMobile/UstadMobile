package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.SelectClazzesDialogView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLASSES_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET


/**
 * The SelectClazzesDialog Presenter.
 */
class SelectClazzesDialogPresenter(context: Any, arguments: Map<String, String>?,
                                   view: SelectClazzesDialogView) :
        UstadBaseController<SelectClazzesDialogView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var clazzWithEnrollmentUmProvider: UmProvider<ClazzWithNumStudents>? = null
    private var locations: List<Long>? = null
    var clazzes: HashMap<String, Long>? = null
    var selectedClazzesList: List<Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        if (arguments!!.containsKey(ARG_LOCATIONS_SET)) {
            val locationsArray = arguments!!.get(ARG_LOCATIONS_SET) as LongArray
            locations = ReportOverallAttendancePresenter.convertLongArray(locationsArray)
        }

        if (arguments!!.containsKey(ARG_CLASSES_SET)) {
            val clazzesSelected = arguments!!.get(ARG_CLASSES_SET) as LongArray
            selectedClazzesList = ReportOverallAttendancePresenter.convertLongArray(clazzesSelected)
        }

    }

    fun addToClazzes(clazzUid: Clazz) {
        if (!clazzes!!.containsKey(clazzUid.clazzName)) {
            clazzes!![clazzUid.clazzName] = clazzUid.clazzUid
        }
    }

    fun removeFromClazzes(clazzUid: Clazz) {
        if (clazzes!!.containsKey(clazzUid.clazzName)) {
            clazzes!!.remove(clazzUid.clazzName)
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        clazzes = HashMap()

        //Find the provider
        if (locations != null && !locations!!.isEmpty()) {
            clazzWithEnrollmentUmProvider = repository.clazzDao
                    .findAllClazzesInLocationList(locations!!)
        } else {
            clazzWithEnrollmentUmProvider = repository.clazzDao
                    .findAllClazzes()
        }
        view.setClazzListProvider(clazzWithEnrollmentUmProvider!!)

    }

    fun handleCommonPressed(arg: Any) {
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish()
    }
}
