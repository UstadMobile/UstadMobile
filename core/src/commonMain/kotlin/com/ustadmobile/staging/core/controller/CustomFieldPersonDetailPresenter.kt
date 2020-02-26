package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView.Companion.ARG_CUSTOM_FIELD_VALUE_OPTION_UID
import com.ustadmobile.core.view.CustomFieldDetailView
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.staging.core.view.CustomFieldPersonDetailView
import com.ustadmobile.staging.core.view.CustomFieldPersonDetailView.Companion.ARG_CUSTOM_FIELD_UID_FOR_PERSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for CustomFieldDetail view
 */
class CustomFieldPersonDetailPresenter(context: Any, arguments: Map<String, String>?,
                                       view: CustomFieldPersonDetailView,
                                       val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<CustomFieldPersonDetailView>(context, arguments!!, view) {

    private var optionProvider: DataSource.Factory<Int, CustomFieldValueOption>? = null
    internal var repository: UmAppDatabase

    private val customFieldDao: CustomFieldDao
    private val optionDao: CustomFieldValueOptionDao

    private var currentField: CustomField? = null
    private var updatedField: CustomField? = null
    private var customFieldUid: Long = 0

    private var fieldTypePresets: Array<String>? = null

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        optionDao = repository.customFieldValueOptionDao
        customFieldDao = repository.customFieldDao

        if (arguments!!.containsKey(ARG_CUSTOM_FIELD_UID_FOR_PERSON)) {
            customFieldUid = arguments.get(ARG_CUSTOM_FIELD_UID_FOR_PERSON)!!.toLong()
        }

    }

    fun initFromCustomField(uid: Long) {
        val currentFieldLive = customFieldDao.findByUidLive(uid)
        view.runOnUiThread(Runnable {
            currentFieldLive.observeWithPresenter(this, this::handleCustomFieldChanged)
        })
        GlobalScope.launch {
            val result = customFieldDao.findByUidAsync(uid)
            updatedField = result
            view.runOnUiThread(Runnable {
                view.setCustomFieldOnView(updatedField!!)
            })

            getSetOptionProvider()
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        fieldTypePresets = arrayOf(impl.getString(MessageID.text, context),
                impl.getString(MessageID.dropdown, context))
        view.setDropdownPresetsOnView(fieldTypePresets!!)

        if (customFieldUid == 0L) {
            currentField = CustomField()
            currentField!!.customFieldActive = false
            currentField!!.customFieldEntityType = Person.TABLE_ID
            currentField!!.customFieldType = CustomField.FIELD_TYPE_TEXT
            GlobalScope.launch {
                val result = customFieldDao.insertAsync(currentField!!)
                initFromCustomField(result)
            }
        } else {
            initFromCustomField(customFieldUid)
        }
    }

    fun handleFieldNameChanged(title: String) {
        updatedField!!.customFieldName = title
    }

    fun handleFieldNameDariChanged(title: String){
        updatedField!!.customFieldNameAlt = title
    }

    fun handleFieldNamePashtoChanged(title: String){
        updatedField!!.customFieldNameAltTwo = title
    }

    fun handleFieldTypeChanged(type: Int) {
        var fieldType = 0
        when (type) {
            FIELD_TYPE_TEXT -> {
                fieldType = CustomField.FIELD_TYPE_TEXT
                view.runOnUiThread(Runnable {
                    view.showOptions(false)
                })
            }
            FIELD_TYPE_DROPDOWN -> {
                fieldType = CustomField.FIELD_TYPE_DROPDOWN
                view.runOnUiThread(Runnable {
                    view.showOptions(true)
                })
            }
            else -> view.runOnUiThread(Runnable {
                view.showOptions(false)
            })
        }

        if (updatedField != null) {
            updatedField!!.customFieldType = fieldType
        }
    }

    fun handleEntityEntityChanged() {
        if(updatedField != null) {
            updatedField!!.customFieldEntityType = Person.TABLE_ID
        }
    }

    fun handleDefaultValueChanged(defaultString: String) {
        updatedField!!.customFieldDefaultValue = defaultString
    }

    fun handleClickAddOption() {

        val args = HashMap<String, String>()
        args.put(ARG_CUSTOM_FIELD_UID_FOR_PERSON, updatedField!!.customFieldUid.toString())
        args.put(CustomFieldDetailView.ARG_CUSTOM_FIELD_UID, updatedField!!.customFieldUid.toString())
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickOptionEdit(customFieldOptionUid: Long) {
        val args = HashMap<String, String>()
        args.put(CustomFieldDetailView.ARG_CUSTOM_FIELD_UID, updatedField!!.customFieldUid.toString())
        args.put(ARG_CUSTOM_FIELD_UID_FOR_PERSON, updatedField!!.customFieldUid.toString())
        args.put(ARG_CUSTOM_FIELD_VALUE_OPTION_UID, customFieldOptionUid.toString())
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickOptionDelete(customFieldOptionUid: Long) {
        GlobalScope.launch {
            optionDao.deleteOption(customFieldOptionUid)
        }
    }

    private fun handleCustomFieldChanged(changedCustomField: CustomField?) {
        //set the og person value
        if (currentField == null)
            currentField = changedCustomField

        if (updatedField == null || updatedField != changedCustomField) {

            if (changedCustomField != null) {
                //Update the currently editing custom field object
                updatedField = changedCustomField

                view.runOnUiThread(Runnable {
                    view.setCustomFieldOnView(changedCustomField)
                })
            }
        }
    }

    private fun getSetOptionProvider() {
        GlobalScope.launch {
            optionProvider = optionDao.findAllOptionsForField(updatedField!!.customFieldUid)
            view.runOnUiThread(Runnable {
                view.setListProvider(optionProvider!!)
            })
        }
    }

    fun handleClickDone() {
        updatedField!!.customFieldActive = true
        GlobalScope.launch {
            customFieldDao.updateAsync(updatedField!!)
            view.finish()
        }
    }

    companion object {

        val FIELD_TYPE_TEXT = 0
        val FIELD_TYPE_DROPDOWN = 1
    }
}
