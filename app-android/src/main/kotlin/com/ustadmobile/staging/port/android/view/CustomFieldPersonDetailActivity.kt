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
import com.ustadmobile.core.controller.CustomFieldPersonDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import com.ustadmobile.port.android.view.UstadBaseActivity
import com.ustadmobile.staging.core.view.CustomFieldPersonDetailView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CustomFieldPersonDetailActivity : UstadBaseActivity(), CustomFieldPersonDetailView {

    private lateinit var toolbar: Toolbar
    private lateinit var mPresenter: CustomFieldPersonDetailPresenter
    private lateinit var mRecyclerView: RecyclerView

    private lateinit var fieldTypeSpinner: Spinner

    private lateinit var title: EditText
    private lateinit var titleDari: EditText
    private lateinit var titlePashto: EditText
    private lateinit var defaultET: EditText
    private lateinit var optionsCL: ConstraintLayout
    private lateinit var addOptionsCL: ConstraintLayout

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
            mPresenter.handleClickDone()

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
        setContentView(R.layout.activity_custom_field_person_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_custom_field_person_detail_toolbar)
        toolbar.title = getText(R.string.custom_field)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        title = findViewById(R.id.activity_custom_field_person_detail_name)
        titleDari = findViewById(R.id.activity_custom_field_person_detail_name_dari)
        titlePashto = findViewById(R.id.activity_custom_field_person_detail_name_pashto)
        defaultET = findViewById(R.id.activity_custom_field_person_detail_default_et)

        fieldTypeSpinner = findViewById(R.id.activity_custom_field_person_detail_field_type_spinner)

        optionsCL = findViewById(R.id.activity_custom_field_person_detail_options_cl)
        optionsCL.visibility = View.GONE

        addOptionsCL = findViewById(R.id.activity_custom_field_person_detail_add_cl)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_custom_field_person_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = CustomFieldPersonDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        addOptionsCL.setOnClickListener { mPresenter.handleClickAddOption() }

        title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter.handleFieldNameChanged(s.toString())
            }
        })

        titleDari.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter.handleFieldNameDariChanged(s.toString())
            }
        })

        titlePashto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter.handleFieldNamePashtoChanged(s.toString())
            }
        })

        fieldTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(Color.BLACK)
                mPresenter.handleFieldTypeChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        defaultET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                mPresenter.handleDefaultValueChanged(s.toString())
            }
        })


    }

    override fun setListProvider(listProvider: DataSource.Factory<Int, CustomFieldValueOption>) {
        val recyclerAdapter = CustomFieldPersonDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                applicationContext)

        // get the provider, set , observe, etc.
        val data = LivePagedListBuilder(listProvider, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<CustomFieldValueOption>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView.setAdapter(recyclerAdapter)
    }

    override fun setDropdownPresetsOnView(dropdownPresets: Array<String>) {
        val adapter = ArrayAdapter(applicationContext,
                android.R.layout.simple_spinner_dropdown_item, dropdownPresets)
        fieldTypeSpinner.adapter = adapter

    }

    override fun setCustomFieldOnView(customField: CustomField) {
        title.setText(customField.customFieldName)
        titleDari.setText(customField.customFieldNameAlt)
        titlePashto.setText(customField.customFieldNameAltTwo)

        defaultET.setText(customField.customFieldDefaultValue)
        when (customField.customFieldType) {
            CustomField.FIELD_TYPE_TEXT -> {
                runOnUiThread {
                    fieldTypeSpinner.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_TEXT)
                }
                showOptions(false)
            }
            CustomField.FIELD_TYPE_DROPDOWN -> {
                runOnUiThread {
                    fieldTypeSpinner.setSelection(CustomFieldDetailPresenter.FIELD_TYPE_DROPDOWN)
                }
                showOptions(true)
            }
            else -> {
            }
        }

    }

    override fun showOptions(show: Boolean) {
        runOnUiThread {
            optionsCL.visibility = if (show) View.VISIBLE else View.GONE
        }
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
