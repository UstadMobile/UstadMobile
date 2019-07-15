package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SalePaymentDetailView
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_DEFAULT_VALUE
import com.ustadmobile.core.view.SalePaymentDetailView.Companion.ARG_SALE_PAYMENT_UID
import com.ustadmobile.lib.db.entities.SalePayment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for SalePaymentDetail view
 */
class SalePaymentDetailPresenter(context: Any,
                                 arguments: Map<String, String>?,
                                 view: SalePaymentDetailView)
    : UstadBaseController<SalePaymentDetailView>(context, arguments!!, view) {


    internal var repository: UmAppDatabase
    private val paymentDao: SalePaymentDao

    private var paymentUid: Long = 0
    private var currentPayment: SalePayment? = null

    private var balanceDue: Long = 0


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        paymentDao = repository.salePaymentDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(ARG_SALE_PAYMENT_UID)) {
            paymentUid = (arguments[ARG_SALE_PAYMENT_UID]!!.toLong())
            if (arguments.containsKey(ARG_SALE_PAYMENT_DEFAULT_VALUE)) {
                balanceDue = (arguments[ARG_SALE_PAYMENT_DEFAULT_VALUE]!!.toLong())
            }
            initFromSalePaymentUid(paymentUid)
        } else {
            //Should not happen  \'.'/
        }

    }

    fun initFromSalePaymentUid(uid: Long) {
        GlobalScope.launch {
            try{
                val result = paymentDao.findByUidAsync(uid)
                currentPayment = result
                view.runOnUiThread(Runnable { view.updateSalePaymentOnView(currentPayment!!) })
                if (balanceDue > 0) {
                    view.runOnUiThread(Runnable { view.updateDefaultValue(balanceDue) })
                }
            }catch(e:Exception){
                println(e.message)
            }
        }
    }

    fun handleAmountUpdated(amount: Long) {
        currentPayment!!.salePaymentPaidAmount = amount
    }

    fun handleDateUpdated(date: Long) {
        currentPayment!!.salePaymentPaidDate = date
    }

    fun handleClickSave() {
        currentPayment!!.salePaymentActive = true
        currentPayment!!.salePaymentDone = true
        GlobalScope.launch {
            try{
                paymentDao.updateAsync(currentPayment!!)
                view.finish()
            }catch (e:Exception){
                println(e.message)
            }
        }
    }

}
