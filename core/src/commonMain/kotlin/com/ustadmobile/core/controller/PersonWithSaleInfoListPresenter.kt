package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.core.view.PersonWithSaleInfoListView;
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import kotlinx.coroutines.Dispatchers.Main

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class PersonWithSaleInfoListPresenter(context: Any,
                      arguments: Map<String, String>?,
                      view: PersonWithSaleInfoListView,
                      val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                      private val repository: UmAppDatabase =
                                      UmAccountManager.getRepositoryForActiveAccount(context),
                      private val saleDao: SaleDao = repository.saleDao)
    : UstadBaseController<PersonWithSaleInfoListView>(context, arguments!!, view) {

    //Use lateinit - these should never be null
    private var personDao: PersonDao

    private var idToOrderInteger: MutableMap<Long, Int>? = null
    private var currentSortOrder = 0

    private var personUid: Long =0
    private var leUid: Long =0
    private lateinit var factory : DataSource.Factory<Int, PersonWithSaleInfo>

    init {
        personDao = repository.personDao
    }

    val thisP=this

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        personUid = (arguments[PersonWithSaleInfoListView.ARG_LE_UID]!!.toLong())
        idToOrderInteger = HashMap()

        //Update sort presets
        updateSortSpinnerPreset()

        GlobalScope.launch {
            val personLive = personDao.findByUidLive(personUid)
            GlobalScope.launch(Main){
                personLive.observeWithPresenter(thisP, thisP::observePerson)
            }
        }
    }

    private fun observePerson(person: Person?){
        if(person!=null) {
            leUid = person.personUid
            //Get assigned people
            getAndSetProvider(currentSortOrder)
        }
    }

    /**
     * Upon clicking every Women entrepreneur -> should go to PersonWithSaleInfoDetail
     */
    fun handleClickWE(weUid:Long) = systemImpl.go(PersonWithSaleInfoDetailView.VIEW_NAME,
                    mapOf(ARG_WE_UID to weUid.toString()), context)

    /**
     * Upon clicking search -> should open up search experience.
     */
    fun handleSearchQuery(searchBit:String){
        if(searchBit == null || searchBit.isEmpty()){
            factory = saleDao.getMyWomenEntrepreneurs(leUid)
        }else {
            factory = saleDao.getMyWomenEntrepreneursSearch(leUid, "%$searchBit%")
        }
        view.setWEListFactory(factory)
    }

    fun handleSortChanged(order: Long){
        //TODO: Check
        var order = order
        order = order + 1

        if (idToOrderInteger!!.containsKey(order)) {
            currentSortOrder = idToOrderInteger!![order]!!
            getAndSetProvider(currentSortOrder)
        }
    }

    private fun getAndSetProvider(sortCode: Int) {
        factory = saleDao.getMyWomenEntrepreneurs(leUid, sortCode)
        view.setWEListFactory(factory)
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
        presetAL.add(impl.getString(MessageID.sort_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_NAME_DESC)

        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_ascending, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_AMOUNT_ASC)
        presetAL.add(impl.getString(MessageID.sale_list_sort_by_total_descending, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SaleDao.SORT_ORDER_AMOUNT_DESC)

        val sortPresets = SaleListPresenter.arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

}
