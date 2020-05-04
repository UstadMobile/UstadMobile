package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.ActionBar

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SELRecognitionPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SELRecognitionView
import com.ustadmobile.lib.db.entities.PersonWithPersonPicture
import com.ustadmobile.port.android.view.UstadBaseActivity

import java.util.Objects

import ru.dimorinny.floatingtextbutton.FloatingTextButton


/**
 * The SELRecognition activity. This Activity extends UstadBaseActivity and implements
 * SELRecognitionView. This activity is responsible for showing and handling recognition which is
 * merely a toggle on people blob lists and it will only allow to proceed if recognition check box
 * is explicitly checked.
 */
class SELRecognitionActivity : UstadBaseActivity(), SELRecognitionView {

    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: SELRecognitionPresenter? = null

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is Handling
     * back button pressed.
     *
     * @param item  The menu item that was selected / clicked
     * @return      true if accounted for.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sets the people blob list recycler adapter to the recycler view and observes it.
     *
     * @param factory The provider data
     */
    override fun setListProvider(factory: DataSource.Factory<Int, PersonWithPersonPicture>) {

        // Specify the mAdapter
        val recyclerAdapter = PeopleBlobListRecyclerAdapter(DIFF_CALLBACK, applicationContext,
                mPresenter!!, true)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        data.observe(this, Observer<PagedList<PersonWithPersonPicture>> { recyclerAdapter.submitList(it) })

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun showMessage(message: String) {
        Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * In Order:
     * 1. Sets layout
     * 2. Sets toolbar
     * 3. Sets Recycler View
     * 4. Sets layout of Recycler View with Grid (for people blobs)
     * 5. Instantiates the presenter and calls it's onCreate()
     * 6. Sets the Floating action button (that starts the SEL) to presenter's method along
     * with the value of recognized checkbox.
     *
     * @param savedInstanceState    The application bundle
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_sel_recognition)

        //Toolbar:
        val toolbar = findViewById<Toolbar>(R.id.activity_sel_recognition_toolbar)
        toolbar.title = getText(R.string.social_nomination)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //Recycler View:
        mRecyclerView = findViewById(
                R.id.activity_sel_recognition_recyclerview)
        //View people blobs as a grid
        val mRecyclerLayoutManager = GridLayoutManager(applicationContext, 3)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        //Call the Presenter
        mPresenter = SELRecognitionPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        val recognizedCheckBox = findViewById<CheckBox>(R.id.activity_sel_recognition_checkbox)

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_sel_recognition_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton(recognizedCheckBox.isChecked) }

    }

    companion object {

        /**
         * The DIFF callback
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithPersonPicture> = object
            : DiffUtil.ItemCallback<PersonWithPersonPicture>() {
            override fun areItemsTheSame(oldItem: PersonWithPersonPicture,
                                         newItem: PersonWithPersonPicture): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: PersonWithPersonPicture,
                                            newItem: PersonWithPersonPicture): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }

}
