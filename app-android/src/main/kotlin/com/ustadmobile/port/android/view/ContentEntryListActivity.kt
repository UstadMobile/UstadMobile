package com.ustadmobile.port.android.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.DistinctCategorySchema
import com.ustadmobile.lib.db.entities.Language

class ContentEntryListActivity : UstadBaseActivity(), ContentEntryListFragment.ContentEntryListener, AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_list)

        setUMToolbar(R.id.content_entry_list_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val currentFrag = ContentEntryListFragment.newInstance(intent.extras)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.entry_content, currentFrag)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                clickUpNavigation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clickUpNavigation() {
        runOnUiThread {
            val fragment = supportFragmentManager.findFragmentById(R.id.entry_content) as ContentEntryListFragment?
            fragment!!.clickUpNavigation()
        }

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

            val fragment = supportFragmentManager.findFragmentById(R.id.entry_content) as ContentEntryListFragment?
            if (item is Language) {
                // language
                fragment!!.filterByLang(item.langUid)

            } else if (item is DistinctCategorySchema) {
                fragment!!.filterBySchemaCategory(item.contentCategoryUid, item.contentCategorySchemaUid)
            }
        }

    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }
}
