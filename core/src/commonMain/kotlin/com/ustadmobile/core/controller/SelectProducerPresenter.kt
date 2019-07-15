package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SaleItemDetailView.Companion.ARG_SALE_ITEM_UID
import com.ustadmobile.core.view.SelectProducerView
import com.ustadmobile.core.view.SelectProducerView.Companion.ARG_PRODUCER_UID
import com.ustadmobile.core.view.SelectSaleProductView
import com.ustadmobile.lib.db.entities.Person

/**
 * Presenter for SelectProducer view
 */
class SelectProducerPresenter(context: Any,
                              arguments: Map<String, String>?,
                              view: SelectProducerView)
    : UstadBaseController<SelectProducerView>(context, arguments!!, view) {


    internal var repository: UmAppDatabase
    private val providerDao: PersonDao
    private var saleItemUid: Long = 0

    private var idToOrderInteger: HashMap<Long, Int>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        providerDao = repository.personDao

        if (arguments!!.containsKey(ARG_SALE_ITEM_UID)) {
            saleItemUid = (arguments!!.get(ARG_SALE_ITEM_UID)!!.toLong())
        } else {
            //Create a new SaleItem? - shouldn't happen.
            //throw exception.
        }


    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        idToOrderInteger = HashMap<Long, Int>()
        updateSortSpinnerPreset()

    }

    fun getAndSetProvider(sortCode: Int) {
        when (sortCode) {
            SORT_ORDER_NAME_ASC -> {
                //Get provider
                val umProvider = providerDao.findAllPeopleNameAscProvider()
                view.setListProvider(umProvider)
            }
            SORT_ORDER_NAME_DESC -> {
                //Get provider
                val umProvider = providerDao.findAllPeopleNameDescProvider()
                view.setListProvider(umProvider)
            }
            SORT_ORDER_MOST_USED -> {
            }
            else -> {
                //Get provider
                val umProvider = providerDao.findAllPeopleProvider()
                view.setListProvider(umProvider)
            }
        }
    }

    fun handleClickProducer(personUid: Long) {
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_PRODUCER_UID, personUid.toString())
        args.put(ARG_SALE_ITEM_UID, saleItemUid.toString())
        impl.go(SelectSaleProductView.VIEW_NAME, args, context)
        view.finish()
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()
        val impl = UstadMobileSystemImpl.instance

        idToOrderInteger = HashMap<Long, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sorT_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_NAME_DESC)
        presetAL.add(impl.getString(MessageID.sort_by_most_used, context))
        idToOrderInteger!!.put(presetAL.size.toLong(), SORT_ORDER_MOST_USED)


        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSpinner(sortPresets)
    }

    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    fun handleChangeSortOrder(order: Long) {
        var order = order
        order = order + 1

        if (idToOrderInteger!!.containsKey(order)) {
            val sortCode = idToOrderInteger!!.get(order)
            if (sortCode != null) {
                getAndSetProvider(sortCode)
            }
        }

    }

    companion object {
        private val SORT_ORDER_NAME_ASC = 1
        private val SORT_ORDER_NAME_DESC = 2
        private val SORT_ORDER_MOST_USED = 3
    }
}
