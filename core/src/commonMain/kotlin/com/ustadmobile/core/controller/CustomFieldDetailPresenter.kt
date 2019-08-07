package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.CustomFieldDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.impl.UstadMobileSystemImpl



import com.ustadmobile.core.view.AddCustomFieldOptionDialogView
import com.ustadmobile.core.view.CustomFieldDetailView

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.lib.db.entities.Person

import com.ustadmobile.core.controller.CustomFieldListPresenter.Companion.ENTITY_TYPE_CLASS
import com.ustadmobile.core.controller.CustomFieldListPresenter.Companion.ENTITY_TYPE_PERSON
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView.Companion.ARG_CUSTOM_FIELD_VALUE_OPTION_UID
import com.ustadmobile.core.view.CustomFieldDetailView.Companion.ARG_CUSTOM_FIELD_UID

/**
 * Presenter for CustomFieldDetail view
 */
class CustomFieldDetailPresenter(context: Any, arguments: Map<String, String>?,
                                 view: CustomFieldDetailView,
                                 val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<CustomFieldDetailView>(context, arguments!!, view) {

    private var optionProvider: UmProvider<CustomFieldValueOption>? = null
    internal var repository: UmAppDatabase

    private val customFieldDao: CustomFieldDao
    private val optionDao: CustomFieldValueOptionDao

    private var currentField: CustomField? = null
    private var updatedField: CustomField? = null
    private var customFieldUid: Long = 0

    private var fieldTypePresets: Array<String>? = null
    private var entityTypePresets: Array<String>? = null


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        //Get provider Dao
        optionDao = repository.customFieldValueOptionDao
        customFieldDao = repository.customFieldDao

        if (arguments!!.containsKey(ARG_CUSTOM_FIELD_UID)) {
            customFieldUid = arguments!!.get(ARG_CUSTOM_FIELD_UID)
        }


    }

    fun initFromCustomField(uid: Long) {
        val currentFieldLive = customFieldDao.findByUidLive(uid)
        currentFieldLive.observe(this@CustomFieldDetailPresenter,
                UmObserver<CustomField> { this@CustomFieldDetailPresenter.handleCustomFieldChanged(it) })
        customFieldDao.findByUidAsync(uid, object : UmCallback<CustomField> {
            override fun onSuccess(result: CustomField?) {
                updatedField = result
                view.setCustomFieldOnView(updatedField!!)

                getSetOptionProvider()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        fieldTypePresets = arrayOf(impl.getString(MessageID.text, context), impl.getString(MessageID.dropdown, context))
        view.setDropdownPresetsOnView(fieldTypePresets!!)

        entityTypePresets = arrayOf(impl.getString(MessageID.clazz, context), impl.getString(MessageID.people, context))
        view.setEntityTypePresetsOnView(entityTypePresets!!)


        if (customFieldUid == 0L) {
            currentField = CustomField()
            currentField!!.isCustomFieldActive = false
            currentField!!.customFieldEntityType = Clazz.TABLE_ID
            currentField!!.customFieldType = CustomField.FIELD_TYPE_TEXT
            customFieldDao.insertAsync(currentField, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    initFromCustomField(result!!)
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            initFromCustomField(customFieldUid)
        }

    }

    fun handleFieldNameChanged(title: String) {
        updatedField!!.customFieldName = title
    }

    fun handleFieldNameAltChanged(title: String) {
        updatedField!!.customFieldNameAlt = title
    }

    fun handleFieldTypeChanged(type: Int) {
        var fieldType = 0
        when (type) {
            FIELD_TYPE_TEXT -> {
                fieldType = CustomField.FIELD_TYPE_TEXT
                view.showOptions(false)
            }
            FIELD_TYPE_DROPDOWN -> {
                fieldType = CustomField.FIELD_TYPE_DROPDOWN
                view.showOptions(true)
            }
            else -> view.showOptions(false)
        }

        if (updatedField != null) {
            updatedField!!.customFieldType = fieldType
        }
    }

    fun handleEntityEntityChanged(type: Int) {
        when (type) {
            ENTITY_TYPE_CLASS -> updatedField!!.customFieldEntityType = Clazz.TABLE_ID
            ENTITY_TYPE_PERSON -> updatedField!!.customFieldEntityType = Person.TABLE_ID
            else -> {
            }
        }
    }

    fun handleDefaultValueChanged(defaultString: String) {
        updatedField!!.customFieldDefaultValue = defaultString
    }

    fun handleClickAddOption() {

        val args = HashMap<String, String>()
        args.put(ARG_CUSTOM_FIELD_UID, updatedField!!.customFieldUid)
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickOptionEdit(customFieldOptionUid: Long) {
        val args = HashMap<String, String>()
        args.put(ARG_CUSTOM_FIELD_UID, updatedField!!.customFieldUid)
        args.put(ARG_CUSTOM_FIELD_VALUE_OPTION_UID, customFieldOptionUid)
        impl.go(AddCustomFieldOptionDialogView.VIEW_NAME, args, context)
    }

    fun handleClickOptionDelete(customFieldOptionUid: Long) {
        optionDao.deleteOption(customFieldOptionUid, null!!)
    }

    private fun handleCustomFieldChanged(changedCustomField: CustomField?) {
        //set the og person value
        if (currentField == null)
            currentField = changedCustomField

        if (updatedField == null || updatedField != changedCustomField) {

            if (changedCustomField != null) {
                //Update the currently editing custom field object
                updatedField = changedCustomField

                view.setCustomFieldOnView(changedCustomField)
            }
        }
    }

    private fun getSetOptionProvider() {
        optionProvider = optionDao.findAllOptionsForField(updatedField!!.customFieldUid)
        view.setListProvider(optionProvider!!)
    }

    fun handleClickDone() {
        updatedField!!.isCustomFieldActive = true
        customFieldDao.updateAsync(updatedField!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    companion object {

        val FIELD_TYPE_TEXT = 0
        val FIELD_TYPE_DROPDOWN = 1
    }
}
