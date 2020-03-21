package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CustomFieldDetailView
import com.ustadmobile.core.view.CustomFieldDetailView.Companion.ARG_CUSTOM_FIELD_UID
import com.ustadmobile.core.view.CustomFieldListView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for CustomFieldList view
 */
class CustomFieldListPresenter(context: Any, arguments: Map<String, String>?,
                               view: CustomFieldListView,
                               val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<CustomFieldListView>(context, arguments!!, view) {

    private var umProvider: DataSource.Factory<Int, CustomField>? = null
    internal var repository: UmAppDatabase

    private val customFieldDao: CustomFieldDao
    private var entityTypePresets: Array<String>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        customFieldDao = repository.customFieldDao


    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        entityTypePresets = arrayOf(impl.getString(MessageID.clazz, context), impl.getString(MessageID.person, context))
        view.setEntityTypePresets(entityTypePresets!!)

        //Get provider
        generateProvider(ENTITY_TYPE_CLASS)

    }

    fun handleEntityTypeChange(type: Int) {

        generateProvider(type)
    }

    private fun generateProvider(type: Int) {
        var tableId = 0
        when (type) {
            ENTITY_TYPE_CLASS -> tableId = Clazz.TABLE_ID
            ENTITY_TYPE_PERSON -> tableId = Person.TABLE_ID
            else -> {
            }
        }
        umProvider = customFieldDao.findAllCustomFieldsProviderForEntity(tableId)
        view.setListProvider(umProvider!!)

    }

    fun handleClickPrimaryActionButton() {

        val args = HashMap<String, String>()
        impl.go(CustomFieldDetailView.VIEW_NAME, args, context)
    }

    fun handleClickEditCustomField(customFieldUid: Long) {
        //Go to custom field detail
        val args = HashMap<String, String>()
        args.put(ARG_CUSTOM_FIELD_UID, customFieldUid.toString())
        impl.go(CustomFieldDetailView.VIEW_NAME, args, context)
    }

    fun handleClickDeleteCustomField(customFieldUid: Long) {
        GlobalScope.launch {
            customFieldDao.deleteCustomField(customFieldUid)
        }
    }

    companion object {

        val ENTITY_TYPE_CLASS = 0
        val ENTITY_TYPE_PERSON = 1
    }

}
