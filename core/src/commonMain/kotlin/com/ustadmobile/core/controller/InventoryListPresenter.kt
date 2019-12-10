package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.InventoryDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.InventoryListView
import com.ustadmobile.lib.db.entities.Inventory

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class InventoryListPresenter(context: Any,
                             arguments: Map<String, String>?,
                             view: InventoryListView,
                             val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                             private val repository: UmAppDatabase =
                                     UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<InventoryListView>(context, arguments!!, view) {


    private var idToOrderInteger: MutableMap<Long, Int>? = null

    private var currentSortOrder = 0

    private lateinit var rvDao: InventoryDao

    private lateinit var factory: DataSource.Factory<Int, Inventory>

    var searchQuery: String = "%%"

    fun setQuerySearch(query:String){
        searchQuery = "%$query%"
    }

    init {
        //Initialise Daos, etc here.
        rvDao = repository.rvDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap()
        //Update sort presets
        updateSortSpinnerPreset()
        getAndSetProvider(currentSortOrder)
    }


    fun handleSortChanged(order: Long) {
        var order = order
        order = order + 1

        if (idToOrderInteger!!.containsKey(order)) {
            currentSortOrder = idToOrderInteger!![order]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    private fun getAndSetProvider(sortCode: Int) {
        //TODO:
        //factory = rvDao.filter(sortCode)
        //view.setWEListFactory(factory)
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()
        val impl = UstadMobileSystemImpl.instance

        idToOrderInteger = HashMap()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_DESC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_asc, context))

        val sortPresets = SaleListPresenter.arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

    fun updateProviders(){
        getAndSetProvider(currentSortOrder)
    }


}
