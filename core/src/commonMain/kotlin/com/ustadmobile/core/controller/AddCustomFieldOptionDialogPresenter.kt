package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.CustomFieldValueOptionDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView
import com.ustadmobile.core.view.AddCustomFieldOptionDialogView.Companion.ARG_CUSTOM_FIELD_VALUE_OPTION_UID
import com.ustadmobile.core.view.CustomFieldDetailView.Companion.ARG_CUSTOM_FIELD_UID
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for AddCustomFieldOptionDialog view
 */
class AddCustomFieldOptionDialogPresenter(context: Any, arguments:Map<String, String>?,
                                          view: AddCustomFieldOptionDialogView)
    : UstadBaseController<AddCustomFieldOptionDialogView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase

    private var customfieldUid: Long = 0
    internal var optionValue: String? = null
    private var optionUid: Long = 0
    private var currentOption: CustomFieldValueOption? = null
    private val optionDao: CustomFieldValueOptionDao

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        optionDao = repository.customFieldValueOptionDao

        if (arguments!!.containsKey(ARG_CUSTOM_FIELD_UID)) {
            customfieldUid = (arguments.get(ARG_CUSTOM_FIELD_UID)!!.toString()).toLong()
        }
        if (arguments!!.containsKey(ARG_CUSTOM_FIELD_VALUE_OPTION_UID))
            optionUid = (arguments.get(ARG_CUSTOM_FIELD_VALUE_OPTION_UID)!!.toString()).toLong()

    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)


        if (optionUid != 0L) {
            GlobalScope.launch {
                val result = optionDao.findByUidAsync(optionUid)
                initFromOption(result)
            }
        } else {
            val option = CustomFieldValueOption()
            GlobalScope.launch {
                val resl = optionDao.insertAsync(option)
                if (resl != 0) {
                    option.customFieldValueOptionUid = result
                    initFromOption(option)
                }
            }
        }

    }

    private fun initFromOption(option: CustomFieldValueOption?) {
        if (option != null) {
            currentOption = option
            view.setOptionValue(option.customFieldValueOptionName!!)
        }
    }

    fun setOptionValue(optionValue: String) {
        this.optionValue = optionValue
    }

    fun handleClickOk() {

        currentOption!!.customFieldValueOptionName = optionValue
        currentOption!!.customFieldValueOptionFieldUid = customfieldUid
        currentOption!!.isCustomFieldValueOptionActive = true

        optionDao.updateAsync(currentOption!!, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                view.finish()
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })

    }


}
