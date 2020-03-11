package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.databinding.ActivityClazzEdit2Binding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

class ClazzEdit2Activity : UstadBaseActivity(), ClazzEdit2View {

    private var rootView: ActivityClazzEdit2Binding? = null

    private lateinit var mPresenter: ClazzEdit2Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_clazz_edit2)

        val toolbar = findViewById<Toolbar>(R.id.activity_clazz_edit_toolbar)
        toolbar.setTitle(R.string.class_setup)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPresenter = ClazzEdit2Presenter(this, intent.extras.toStringMap(), this,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toStringMap())
    }

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        get() = field
        set(value) {
            field = value
        }

    override var clazz: Clazz? = null
        get() = field
        set(value) {
            field = value
            rootView?.clazz = clazz
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
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
                val selectedClazz = rootView?.clazz ?: return false
                mPresenter.handleClickDone(selectedClazz)
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
}
