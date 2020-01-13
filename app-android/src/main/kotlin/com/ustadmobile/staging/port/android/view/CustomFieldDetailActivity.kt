package com.ustadmobile.staging.port.android.view

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CustomFieldDetailPresenter
import com.ustadmobile.core.controller.CustomFieldListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.CustomFieldDetailView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CustomFieldDetailActivity : UstadBaseActivity(), CustomFieldDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: CustomFieldDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null

    private var entityTypeSpinner: Spinner? = null
    private var fieldTypeSpinner: Spinner? = null

    private var title: EditText? = null
    private var titleAlt: EditText? = null
    private var defaultET: EditText? = null
    internal var optionsCL: ConstraintLayout ?= null
    internal var addOptionsCL: ConstraintLayout? = null


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_done) {
            mPresenter!!.handleClickDone()

            return super.onOptionsItemSelected(item)
        }

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_custom_field_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_custom_field_detail_toolbar)
        toolbar!!.setTitle(getText(R.string.custom_field))
        setSupportActionBar(toolbar!!)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.activity_custom_field_detail_name)
        titleAlt = findViewById(R.id.activity_custom_field_detail_name_arabic)
        defaultET = findViewById(R.id.activity_custom_field_detail_default_et)

        entityTypeSpinner = findViewById(R.id.activity_custom_field_detail_entity_spinner)
        fieldTypeSpinner = findViewById(R.id.activity_custom_field_detail_field_type_spinner)

        optionsCL = findViewById(R.id.activity_custom_field_detail_options_cl)
        optionsCL!!.setVisibility(View.GONE)

        addOptionsCL = findViewById(R.id.activity_custom_field_detail_add_cl)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_custom_field_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Call the Presenter
        mPresenter = CustomFieldDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        addOptionsCL!!.setOnClickListener({ v -> mPresenter!!.handleClickAddOption() })

        title!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.handleFieldNameChanged(s.toString())
            }
        })

        titleAlt!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.handleFieldNameAltChanged(s.toString())
            }
        })

        entityTypeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                mPresenter!!.handleEntityEntityChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fieldTypeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                mPresenter!!.handleFieldTypeChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        defaultET!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.handleDefaultValueChanged(s.toString())
            }
        })


    }

    override fun setListProvider(factory: DataSource.Factory<Int, CustomFieldValueOption>) {
        val recyclerAdapter = CustomFieldDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.
        // A warning is expected
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<CustomFieldValueOption>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setDropdownPresetsOnView(dropdownPresets: Array<String>) {
        val adapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_dropdown_item, dropdownPresets)
        fieldTypeSpinner!!.adapter = adapter

    }

    override fun setEntityTypePresetsOnView(entityTypePresets: Array<String>) {
        val adapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_dropdown_item, entityTypePresets)
        entityTypeSpinner!!.adapter = adapter
    }

    override fun setCustomFieldOnView(customField: CustomField) {
        title!!.setText(customField.customFieldName)
        titleAlt!!.setText(customField.customFieldNameAlt)
        defaultET!!.setText(customField.customFieldDefaultValue)
        when (customField.customFieldType) {
            CustomField.FIELD_TYPE_TEXT -> {
                runOnUiThread(Runnable {
                    fieldTypeSpinner!!.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_TEXT)
                })
                showOptions(false)
            }
            CustomField.FIELD_TYPE_DROPDOWN -> {
                runOnUiThread(Runnable {
                    fieldTypeSpinner!!.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_DROPDOWN)
                })
                showOptions(true)
            }
            else -> {
            }
        }
        when (customField.customFieldEntityType) {
            Clazz.TABLE_ID ->
            {
                runOnUiThread(Runnable {
                    entityTypeSpinner!!.setSelection(CustomFieldListPresenter.ENTITY_TYPE_CLASS)
                })
            }
            Person.TABLE_ID -> {
                runOnUiThread(Runnable {
                    entityTypeSpinner!!.setSelection(CustomFieldListPresenter.ENTITY_TYPE_PERSON)
                })
            }
            else -> {
            }
        }
    }

    override fun showOptions(show: Boolean) {
        runOnUiThread(Runnable {
            optionsCL!!.setVisibility(if (show) View.VISIBLE else View.GONE)
        })
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<CustomFieldValueOption> = object
            : DiffUtil.ItemCallback<CustomFieldValueOption>() {
            override fun areItemsTheSame(oldItem: CustomFieldValueOption,
                                         newItem: CustomFieldValueOption): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CustomFieldValueOption,
                                            newItem: CustomFieldValueOption): Boolean {
                return oldItem.customFieldValueOptionUid == newItem.customFieldValueOptionUid
            }
        }
    }
}
