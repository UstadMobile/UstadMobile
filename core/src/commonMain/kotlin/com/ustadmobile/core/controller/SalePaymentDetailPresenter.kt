package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.SalePaymentDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.observeWithPresenter
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

    private fun initFromSalePaymentUid(uid: Long) {
        val thisP = this
        GlobalScope.launch {
            val resultLive = paymentDao.findByUidLive(uid)
            view.runOnUiThread(Runnable {
                resultLive.observeWithPresenter(thisP, thisP::updatePaymentOnView)
            })
        }
    }

    private fun updatePaymentOnView(salePayment:SalePayment?){
        if(salePayment!= null){
            currentPayment = salePayment
            val amountL = currentPayment!!.salePaymentPaidAmount
            val amount = amountL.toInt()

            if (balanceDue > 0 &&  amount == 0) {
                view.runOnUiThread(Runnable {
                    view.updateDefaultValue(balanceDue)
                })
            }

            if(balanceDue > 0 && amount > 0){
                view.updateDefaultValue(amountL)
                view.updateMaxValue(amount + balanceDue)
            }

            view.runOnUiThread(Runnable {
                view.updateSalePaymentOnView(currentPayment!!)
            })

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
