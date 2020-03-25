package com.ustadmobile.port.android.view

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
import com.ustadmobile.lib.db.entities.@EditEntity@
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson

interface @Entity@ActivityEventHandler {

}

class @Entity@EditActivity : UstadBaseActivity(), @Entity@EditView,
        @Entity@ActivityEventHandler {

    private var rootView: Activity@Entity_ViewBinding_VariableName@EditBinding? = null

    private lateinit var mPresenter: @Entity@EditPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_@Entity_LowerCase@_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_@Entity_LowerCase@_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
            putExtraResultAsJson(@Entity@ActivityResultContract.RESULT_EXTRA_KEY, listOf(result))
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
