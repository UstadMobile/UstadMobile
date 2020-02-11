package com.ustadmobile.port.android.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_LINK
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.LangUidAndName


class ContentEntryListActivity : UstadBaseWithContentOptionsActivity(),
        ContentEntryListFragment.ContentEntryListHostActivity,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private lateinit var contentCreationOptionBehaviour: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_entry_list)

        setUMToolbar(R.id.content_entry_list_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        contentCreationOptionBehaviour = BottomSheetBehavior
                .from(findViewById(R.id.bottom_content_option_sheet))

        findViewById<View>(R.id.action_close_options).setOnClickListener {
            val collapsed = contentCreationOptionBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED
            contentCreationOptionBehaviour.setState(if (collapsed)
                BottomSheetBehavior.STATE_EXPANDED
            else
                BottomSheetBehavior.STATE_COLLAPSED)
        }

        coordinatorLayout = findViewById(R.id.coordinationLayout)

        findViewById<View>(R.id.content_create_category).setOnClickListener(this)

        findViewById<View>(R.id.content_import_file).setOnClickListener(this)

        findViewById<View>(R.id.content_create_content).setOnClickListener(this)

        findViewById<View>(R.id.content_import_link).setOnClickListener(this)

        val currentFrag = ContentEntryListFragment.newInstance(intent.extras!!)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_content_entrylist_top, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateBack()
                return true
            }
            R.id.create_new_content -> {
                contentCreationOptionBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun setTitle(title: String) {
        runOnUiThread {
            val toolBarTitle = findViewById<Toolbar>(R.id.content_entry_list_toolbar)
            toolBarTitle.title = title
        }
    }

    override fun setFilterSpinner(idToValuesMap: Map<Long, List<DistinctCategorySchema>>) {
        runOnUiThread {
            val spinnerLayout = findViewById<LinearLayout>(R.id.content_entry_list_spinner_layout)
            spinnerLayout.visibility = View.VISIBLE
            for (id in idToValuesMap.keys) {

                val spinner = Spinner(this)
                val dataAdapter = ArrayAdapter(this,
                        R.layout.content_entry_list_spinner_layout, idToValuesMap[id]!!)
                spinner.adapter = dataAdapter
                spinner.onItemSelectedListener = this
                spinner.background.setColorFilter(ContextCompat.getColor(
                        this, android.R.color.white),
                        PorterDuff.Mode.SRC_ATOP)

                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                spinnerLayout.addView(spinner, params)
            }
        }

    }

    override fun setLanguageFilterSpinner(result: List<LangUidAndName>) {
        runOnUiThread {
            val spinnerLayout = findViewById<LinearLayout>(R.id.content_entry_list_spinner_layout)

            //remove any previous spinners
            val existingSpinner = spinnerLayout.findViewWithTag<View?>(TAG_LANGUAGE_SPINNER)
            if(existingSpinner != null) {
                spinnerLayout.removeView(existingSpinner)
            }


            spinnerLayout.visibility = View.VISIBLE
            val spinner = Spinner(this)
            val dataAdapter = ArrayAdapter(this,
                    R.layout.content_entry_list_spinner_layout, result)
            spinner.adapter = dataAdapter
            spinner.onItemSelectedListener = this
            spinner.background.setColorFilter(ContextCompat.getColor(
                    this, android.R.color.white),
                    PorterDuff.Mode.SRC_ATOP)
            spinner.tag = TAG_LANGUAGE_SPINNER

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            spinnerLayout.addView(spinner, 0, params)
        }
    }


    override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
        val item = adapterView.getItemAtPosition(pos)

        val firstItem = adapterView.getChildAt(0)
        if (firstItem is TextView) {
            firstItem.setTextColor(Color.WHITE)
        }

        val fragment = supportFragmentManager.findFragmentById(R.id.entry_content)
                as ContentEntryListFragment?
        if (item is LangUidAndName) {
            // language
            fragment?.filterByLang(item.langUid)

        } else if (item is DistinctCategorySchema) {
            fragment?.filterBySchemaCategory(item.contentCategoryUid)
        }
    }

    override fun onBackPressed() {
        if (contentCreationOptionBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            contentCreationOptionBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    fun navigateBack() {
        val fragment = supportFragmentManager.findFragmentById(R.id.entry_content)
                as ContentEntryListFragment?
        fragment?.clickUpNavigation()
    }


    override fun onClick(view: View) {
        val fragment = supportFragmentManager.findFragmentById(R.id.entry_content)
                as ContentEntryListFragment?
        when (view.id) {
            R.id.content_create_category ->
                fragment?.handleBottomSheetClicked(CONTENT_CREATE_FOLDER)
            R.id.content_import_file ->
                fragment?.handleBottomSheetClicked(CONTENT_IMPORT_FILE)
            R.id.content_create_content ->
                fragment?.handleBottomSheetClicked(CONTENT_CREATE_CONTENT)
            R.id.content_import_link ->
                fragment?.handleBottomSheetClicked(CONTENT_IMPORT_LINK)
        }
        contentCreationOptionBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    companion object {
        const val TAG_LANGUAGE_SPINNER = 42
    }

}
