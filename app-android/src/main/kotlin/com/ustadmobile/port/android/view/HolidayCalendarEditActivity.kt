package com.ustadmobile.port.android.view

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityHolidaycalendarEditBinding
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.lib.db.entities.HolidayCalendar

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY


fun ComponentActivity.prepareHolidayCalendarEditCall(callback: (List<HolidayCalendar>?) -> Unit) = prepareCall(CrudEditActivityResultContract(this, HolidayCalendar::class.java,
        HolidayCalendarEditActivity::class.java, HolidayCalendar::umCalendarUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<HolidayCalendar>>.launchHolidayCalendarEdit(schedule: HolidayCalendar?, extraArgs: Map<String, String> = mapOf()) {
    //TODO: Set PersistenceMode to JSON or DB here
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface HolidayCalendarActivityEventHandler {

}

class HolidayCalendarEditActivity : UstadBaseActivity(), HolidayCalendarEditView,
        HolidayCalendarActivityEventHandler {

    private var rootView: ActivityHolidaycalendarEditBinding? = null

    private lateinit var mPresenter: HolidayCalendarEditPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_holidaycalendar_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_holidaycalendar_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPresenter = HolidayCalendarEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: HolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            rootView?.holidaycalendar = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: HolidayCalendar) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, listOf(result))
        })
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
                val entityVal = rootView?.holidaycalendar ?: return false
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
