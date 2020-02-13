package com.ustadmobile.staging.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CustomFieldPersonListView
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.staging.core.view.CustomFieldPersonDetailView
import com.ustadmobile.staging.core.view.CustomFieldPersonDetailView.Companion.ARG_CUSTOM_FIELD_UID_FOR_PERSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for CustomFieldList view
 */
class CustomFieldPersonListPresenter(context: Any, arguments: Map<String, String>?,
                                     view: CustomFieldPersonListView,
                                     val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<CustomFieldPersonListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, CustomField>? = null
    internal var repository: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)

    private val customFieldDao: CustomFieldDao

    init {

        //Get provider Dao
        customFieldDao = repository.customFieldDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get provider
        generateProvider()

    }

    private fun generateProvider() {
        var tableId = Person.TABLE_ID
        umProvider = customFieldDao.findAllCustomFieldsProviderForEntity(tableId)
        view.setListProvider(umProvider!!)

    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(CustomFieldPersonDetailView.VIEW_NAME, args, context)
    }

    fun handleClickEditCustomField(customFieldUid: Long) {
        //Go to custom field detail
        val args = HashMap<String, String>()
        args.put(ARG_CUSTOM_FIELD_UID_FOR_PERSON, customFieldUid.toString())
        impl.go(CustomFieldPersonDetailView.VIEW_NAME, args, context)
    }

    fun handleClickDeleteCustomField(customFieldUid: Long) {
        GlobalScope.launch {
            customFieldDao.deleteCustomField(customFieldUid)
        }
    }

    companion object {

    }

}
