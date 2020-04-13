package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityHolidayEditBinding
import com.ustadmobile.core.controller.HolidayEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayEditView
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.ext.setEditActivityTitle
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract


fun ComponentActivity.prepareHolidayEditCall(callback: (List<Holiday>?) -> Unit) = prepareCall(CrudEditActivityResultContract(this, Holiday::class.java,
        HolidayEditActivity::class.java, Holiday::holUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<Holiday>>.launchHolidayEdit(schedule: Holiday?, extraArgs: Map<String, String> = mapOf()) {
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface HolidayEditActivityEventHandler {

}

class HolidayEditActivity : UstadBaseActivity(), HolidayEditView,
    HolidayEditActivityEventHandler {

    private var rootView: ActivityHolidayEditBinding? = null

    private lateinit var mPresenter: HolidayEditPresenter

    /*
     * TODO: Add any required one to many join relationships - use the following templates (then hit tab)
     *  onetomanyadapter - adds a recycler adapter, observer, and handler methods for a one-many field
     *  diffutil - adds a diffutil.itemcallback for an entity (put in the companion object)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_holiday_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_holiday_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.holiday)

        mPresenter = HolidayEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: Holiday? = null
        get() = field
        set(value) {
            field = value
            rootView?.holiday = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: List<Holiday>) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, result)
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
                val entityVal = rootView?.holiday ?: return false
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
