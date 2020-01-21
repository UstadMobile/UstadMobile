package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView.Companion.ARG_PRODUCT_SELECTED_SET
import com.ustadmobile.lib.db.entities.SaleProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The SelectMultipleTreeDialog Presenter.
 */
class SelectMultipleProductTypeTreeDialogPresenter(context: Any, arguments:
                            Map<String, String>?,view: SelectMultipleProductTypeTreeDialogView)
    : CommonEntityHandlerPresenter<SelectMultipleProductTypeTreeDialogView>
        (context, arguments!!, view) {

    val selectedOptions: HashMap<String, Long>

    private var selectedProductTypeUidsList: List<Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {

        if (arguments!!.containsKey(ARG_PRODUCT_SELECTED_SET)) {
            val productTypesArrayString = arguments!!.get(ARG_PRODUCT_SELECTED_SET).toString()
            selectedProductTypeUidsList = convertCSVStringToLongList(productTypesArrayString)
        }

        selectedOptions = HashMap()
        getTopProductTypes()

    }

    fun convertCSVStringToLongList(csString:String):List<Long> {
        val list = ArrayList<Long>()
        for (s in csString.split((",").
                toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()){
            val p = s.trim()
            list.add(p as Long)
        }

        return list
    }

    private fun getTopProductTypes() {
        val parentJoinDao = repository.saleProductParentJoinDao
        val thisP=this
        GlobalScope.launch {
            val resultLive = parentJoinDao.findTopSaleProductsLive()
            GlobalScope.launch(Dispatchers.Main) {
                resultLive.observeWithPresenter(thisP, thisP::handleProductTypes)
            }
        }
    }

    private fun handleProductTypes(types:List<SaleProduct>?){
        view.runOnUiThread(Runnable {
            view.populateTopProductType(types!!)
        })
    }

    fun handleClickPrimaryActionButton() {
        view.finish()
    }


    fun getSelectedProductTypeUidsList(): List<Long> {

        if (selectedProductTypeUidsList == null){
            return ArrayList()
        }

        return selectedProductTypeUidsList as List<Long>


    }

    override fun entityChecked(entityName: String, entityUid: Long, checked: Boolean) {
        if (checked) {
            if (entityUid != null) {
                selectedOptions.put(entityName, entityUid)
            }
        } else {
            selectedOptions.remove(entityName)
        }
    }

    companion object {

        internal fun convertLongArray(array: LongArray): ArrayList<Long> {
            val result = ArrayList<Long>(array.size)
            for (item in array)
                result.add(item)
            return result
        }
    }

}
