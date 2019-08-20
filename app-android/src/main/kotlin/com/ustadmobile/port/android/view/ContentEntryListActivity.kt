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
import com.ustadmobile.core.controller.ContentEntryListPresenter
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_CREATE_FOLDER
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_FILE
import com.ustadmobile.core.view.ContentEntryListView.Companion.CONTENT_IMPORT_LINK
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.sharedse.network.NetworkManagerBle
import java.security.AccessController.getContext


class ContentEntryListActivity : UstadBaseWithContentOptionsActivity(),
        ContentEntryListFragment.ContentEntryListener, ContentEntryListView,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    private var showOptions = false

    private var presenter: ContentEntryListPresenter? = null

    lateinit var managerBle: NetworkManagerBle

    private var showControls = false


    private var contentCreationOptionBehaviour: BottomSheetBehavior<LinearLayout>? = null

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle) {
        super.onBleNetworkServiceBound(networkManagerBle)
        managerBle = networkManagerBle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_list)

        setUMToolbar(R.id.content_entry_list_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        showControls = UstadMobileSystemImpl.instance.getAppConfigString(
                AppConfig.KEY_SHOW_CONTENT_EDITOR_CONTROLS, null, this)!!.toBoolean()

        presenter = ContentEntryListPresenter(getContext(),
                UMAndroidUtil.bundleToMap(intent.extras), this)
        presenter!!.handleShowContentEditorOptios(showControls)
        presenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        contentCreationOptionBehaviour = BottomSheetBehavior
                .from(findViewById(R.id.bottom_content_option_sheet))

        findViewById<View>(R.id.action_close_options).setOnClickListener {
            val collapsed = contentCreationOptionBehaviour!!.state == BottomSheetBehavior.STATE_COLLAPSED
            contentCreationOptionBehaviour!!.setState(if (collapsed)
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
            android.R.id.home -> presenter!!.handleBackNavigation()
            R.id.create_new_content ->
                contentCreationOptionBehaviour!!.setState(BottomSheetBehavior.STATE_EXPANDED)
            R.id.edit_category_content ->
                presenter!!.handleContentCreation(CONTENT_CREATE_FOLDER, false)
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

    override fun setLanguageFilterSpinner(result: List<Language>) {
        runOnUiThread {
            val spinnerLayout = findViewById<LinearLayout>(R.id.content_entry_list_spinner_layout)
            spinnerLayout.visibility = View.VISIBLE
            val spinner = Spinner(this)
            val dataAdapter = ArrayAdapter(this,
                    R.layout.content_entry_list_spinner_layout, result)
            spinner.adapter = dataAdapter
            spinner.onItemSelectedListener = this
            spinner.background.setColorFilter(ContextCompat.getColor(
                    this, android.R.color.white),
                    PorterDuff.Mode.SRC_ATOP)

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            spinnerLayout.addView(spinner, 0, params)
        }
    }


    override fun onItemSelected(adapterView: AdapterView<*>, view: View, pos: Int, id: Long) {
        runOnUiThread {
            val item = adapterView.getItemAtPosition(pos)

            if (adapterView.getChildAt(0) != null) {
                (adapterView.getChildAt(0) as TextView).setTextColor(Color.WHITE)
            }

            val fragment = supportFragmentManager.findFragmentById(R.id.entry_content)
                    as ContentEntryListFragment?
            if (item is Language) {
                // language
                fragment!!.filterByLang(item.langUid)

            } else if (item is DistinctCategorySchema) {
                fragment!!.filterBySchemaCategory(item.contentCategoryUid)
            }
        }

    }

    override fun showCreateContentOption(showOption: Boolean) {
        this.showOptions = showOption
    }


    override fun onBackPressed() {
        if (contentCreationOptionBehaviour!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            contentCreationOptionBehaviour!!.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val account = UmAccountManager.getActiveAccount(this)
        menu.findItem(R.id.create_new_content).isVisible = showOptions && account != null && account.personUid != 0L
        menu.findItem(R.id.edit_category_content).isVisible = showOptions && account != null && account.personUid != 0L
        return super.onPrepareOptionsMenu(menu)
    }

    override fun navigateBack() {
        val fragment = supportFragmentManager.findFragmentById(R.id.entry_content)
                as ContentEntryListFragment?
        fragment!!.clickUpNavigation()
    }

    override fun showMessage(message: String) {
        showBaseMessage(message)
    }


    override fun onClick(view: View) {
        when {
            view.id == R.id.content_create_category ->
                presenter!!.handleContentCreation(CONTENT_CREATE_FOLDER, true)
            view.id == R.id.content_import_file ->
                presenter!!.handleContentCreation(CONTENT_IMPORT_FILE, true)
            view.id == R.id.content_create_content ->
                presenter!!.handleContentCreation(CONTENT_CREATE_CONTENT, true)
            view.id == R.id.content_import_link ->
                presenter!!.handleContentCreation(CONTENT_IMPORT_LINK, true)
        }
        contentCreationOptionBehaviour!!.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    override fun onDestroy() {
        presenter!!.onDestroy()
        super.onDestroy()
    }
}
