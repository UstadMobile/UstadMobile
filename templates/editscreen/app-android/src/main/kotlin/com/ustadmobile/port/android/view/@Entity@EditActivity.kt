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
import com.toughra.ustadmobile.databinding.Activity@Entity_ViewBinding_VariableName@EditBinding
import com.ustadmobile.core.controller.@Entity@EditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.@Entity@EditView
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import com.ustadmobile.port.android.view.ext.setEditActivityTitle



fun ComponentActivity.prepare@Entity@EditCall(callback: (List<@Entity@>?) -> Unit) = prepareCall(CrudEditActivityResultContract(this, @Entity@::class.java,
        @Entity@EditActivity::class.java, @Entity@::@Entity_LowerCase@Uid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<@Entity@>>.launch@Entity@Edit(schedule: @Entity@?, extraArgs: Map<String, String> = mapOf()) {
    //TODO: Set PersistenceMode to JSON or DB here
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface @Entity@EditActivityEventHandler {

}

class @Entity@EditActivity : UstadBaseActivity(), @Entity@EditView,
    @Entity@EditActivityEventHandler {

    private var rootView: Activity@Entity_ViewBinding_VariableName@EditBinding? = null

    private lateinit var mPresenter: @Entity@EditPresenter

    /*
     * TODO: Add any required one to many join relationships - use the following templates (then hit tab)
     *  onetomanyadapter - adds a recycler adapter, observer, and handler methods for a one-many field
     *  diffutil - adds a diffutil.itemcallback for an entity (put in the companion object)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_@Entity_LowerCase@_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_@Entity_LowerCase@_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.@Entity_LowerCase@)

        mPresenter = @Entity@EditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var entity: @EditEntity@? = null
        get() = field
        set(value) {
            field = value
            rootView?.@Entity_LowerCase@ = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: @EditEntity@) {
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
                val entityVal = rootView?.@Entity_LowerCase@ ?: return false
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
