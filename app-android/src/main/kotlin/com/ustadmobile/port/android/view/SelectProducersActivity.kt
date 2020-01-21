package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SelectProducersPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SelectProducersView
import com.ustadmobile.lib.db.entities.PersonWithInventory


class SelectProducersActivity : UstadBaseActivity(), SelectProducersView {

    private var toolbar: Toolbar? = null
    private var mPresenter: SelectProducersPresenter? = null
    private var mLinearLayout: LinearLayout? = null

    private var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

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
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_done -> {
                mPresenter!!.handleClickSave()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_select_producers)

        //Toolbar:
        toolbar = findViewById(R.id.activity_select_producers_toolbar)
        toolbar!!.title = getText(R.string.select_producers)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        sortSpinner = findViewById(R.id.activity_select_producers_spinner)

        //RecyclerView
        mLinearLayout = findViewById(
                R.id.activity_select_producers_linearlayout)

        //Call the Presenter
        mPresenter = SelectProducersPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        //Sort spinner handler
        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }


    /**
     * Gets resource id of a resource image like an image. Usually used to get the resource id
     * from an image
     *
     * @param pVariableName     The variable name of the resource eg: ic_person_24dp
     * @param pResourcename     The name of the type of resource eg: drawable
     * @param pPackageName      The package name calling the resource (usually getPackageName() from
     * an activity)
     * @return                  The resource ID
     */
    fun getResourceId(pVariableName: String, pResourcename: String, pPackageName: String): Int {
        try {
            return resources.getIdentifier(pVariableName, pResourcename, pPackageName)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }


    override fun updateProducersOnView(producers: List<PersonWithInventory>) {

        mLinearLayout!!.removeAllViews()

        for(producer in producers){

            val personWithInventorySelection = PersonWithInventorySelectionView(this, producer, mPresenter!!, -1)
            mLinearLayout!!.addView(personWithInventorySelection)

        }
    }


    override fun updateSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(this,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    companion object {
        private val IMAGE_PERSON_THUMBNAIL_WIDTH = 48

        private val SEEKBAR_MAX = 150
    }
}
