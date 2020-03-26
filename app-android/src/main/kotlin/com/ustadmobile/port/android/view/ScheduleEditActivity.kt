package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityScheduleEditBinding
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScheduleEditView
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract

fun ComponentActivity.prepareScheduleEditCall(callback: (List<Schedule>?) -> Unit) = prepareCall(CrudEditActivityResultContract(this, Schedule::class.java,
        ScheduleEditActivity::class.java, Schedule::scheduleUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<Schedule>>.launchScheduleEdit(schedule: Schedule?, extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}

class ScheduleEditActivity : UstadBaseActivity(), ScheduleEditView{

    private var rootView: ActivityScheduleEditBinding? = null

    private lateinit var mPresenter: ScheduleEditPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_schedule_edit)

        val toolbar = findViewById<Toolbar>(R.id.activity_schedule_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPresenter = ScheduleEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this),
                UmAccountManager.activeAccountLiveData)
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: Schedule? = null
        get() = field
        set(value) {
            field = value
            rootView?.schedule = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override var frequencyOptions: List<ScheduleEditPresenter.FrequencyMessageIdOption>? = null
        get() = field
        set(value) {
            rootView?.frequencyOptions = value
            field  = value
        }

    override var dayOptions: List<ScheduleEditPresenter.DayMessageIdOption>? = null
        get() = field
        set(value) {
            rootView?.dayOptions = value
            field = value
        }


    override fun finishWithResult(result: Schedule) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, listOf(result))
        })
        finish()
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.loading = value
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_done -> {
                val entityVal = rootView?.schedule ?: return false
                mPresenter.handleClickSave(entityVal)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    companion object {

    }

}
