package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.SaleItemReminderDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AddReminderDialogView
import com.ustadmobile.core.view.SaleItemDetailView
import com.ustadmobile.lib.db.entities.SaleItemReminder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Presenter for AddReminderDialog view
 **/
class AddReminderDialogPresenter(context: Any,
                                     arguments: Map<String, String>?,
                                     view: AddReminderDialogView,
                                     val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                                     private val repository: UmAppDatabase =
                                             UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<AddReminderDialogView>(context, arguments!!, view) {


    private var saleItemUid = 0L

    init {
        //Initialise Daos, etc here.
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (arguments.containsKey(SaleItemDetailView.ARG_SALE_ITEM_UID)) {
            saleItemUid = arguments[SaleItemDetailView.ARG_SALE_ITEM_UID].toString().toLong()
        }

    }


    fun handleAddReminder(days: Int) {
        val reminder = SaleItemReminder(days, saleItemUid, true)
        val reminderDao = repository.saleItemReminderDao
        GlobalScope.launch {
            val result =
                    reminderDao.findBySaleItemUidAndDaysAsync(saleItemUid, days)
            if (result.size > 0) {
                //It has it already. Skipp it.
            } else {
                reminderDao.insertAsync(reminder)
            }
        }
    }

}
