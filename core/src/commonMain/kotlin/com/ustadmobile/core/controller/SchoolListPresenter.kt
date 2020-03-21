package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.view.ClazzListView.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.view.SchoolEditView
import com.ustadmobile.core.view.SchoolEditView.Companion.ARG_SCHOOL_NEW
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School

/**
 *  Presenter for SchoolListPresenter view
 **/
class SchoolListPresenter(context: Any,
                          arguments: Map<String, String>?,
                          view: SchoolListView,
                          val impl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                          val repository: UmAppDatabase =
                                  UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<SchoolListView>(context, arguments!!, view) {

    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var currentSortOrder = 0

    private var schoolDao: SchoolDao = repository.schoolDao
    private var searchQuery = "%%"

    private lateinit var factory: DataSource.Factory<Int, School>

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap()
        //Update sort presets
        updateSortSpinnerPreset()
        getAndSetProvider()
    }

    /**
     * Upon clicking search -> should open up search experience.
     */
    fun handleSearchQuery(searchBit: String) {
        searchQuery = searchBit
    }

    fun handleSortChanged(order: Long) {
        var theOrder = order
        theOrder += 1

        if (idToOrderInteger!!.containsKey(theOrder)) {
            currentSortOrder = idToOrderInteger!![theOrder]!!
            getAndSetProvider()
        }
    }

    private fun getAndSetProvider() {
        factory = schoolDao.findAllSchoolsAndSort(searchQuery, currentSortOrder)
        view.setListProvider(factory)
    }

    /**
     * Updates the sort by drop down (spinner) on the School list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {

        idToOrderInteger = HashMap()
        idToOrderInteger!![1L] = SORT_ORDER_NAME_ASC
        idToOrderInteger!![2L] = SORT_ORDER_NAME_DESC

        val sortOptions = listOf(MessageID.sort_by_name_asc, MessageID.sort_by_name_desc)
                .map { impl.getString(it, context) }
        view.setSortOptions(sortOptions.toTypedArray())
    }

    fun handleClickAddSchool(){
        val args = mapOf(ARG_SCHOOL_NEW to "true")
        impl.go(SchoolEditView.VIEW_NAME, args, view.viewContext)
    }

}
