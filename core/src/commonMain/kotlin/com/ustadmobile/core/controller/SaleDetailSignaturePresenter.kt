package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SaleDetailSignatureView
import com.ustadmobile.core.view.SaleDetailView.Companion.ARG_SALE_UID
import com.ustadmobile.lib.db.entities.Sale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for SaleDetailSignature view
 */
class SaleDetailSignaturePresenter(context: Any,
                                   arguments: Map<String, String?>,
                                   view: SaleDetailSignatureView)
    : UstadBaseController<SaleDetailSignatureView>(context, arguments, view) {

    internal var repository: UmAppDatabase
    private var currentSignSvg: String? = null
    private var currentSale: Sale? = null
    private val saleDao: SaleDao
    private var currentSaleUid = 0L

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        saleDao = repository.getSaleDao()
        if (arguments.containsKey(ARG_SALE_UID)) {
            currentSaleUid = (arguments.get(ARG_SALE_UID)!!.toLong())
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (currentSaleUid != 0L) {
            GlobalScope.launch {
                val result = saleDao.findByUidAsync(currentSaleUid)
                currentSale = result
                view.updateSaleOnView(currentSale!!)
            }
        }
    }

    fun handleClickAccept() {
        if (currentSale != null) {
            if (currentSignSvg != null && !currentSignSvg!!.isEmpty()) {
                currentSale!!.saleSignature = currentSignSvg
                GlobalScope.launch {
                    try {
                        val res = saleDao.updateAsync(currentSale!!)
                        view.finish()
                    }catch(e:Exception){
                        println(e.message)
                    }
                }
            }
        }
    }

    fun updateSignatureSvg(signSvg: String) {
        currentSignSvg = signSvg
    }
}
