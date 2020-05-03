package com.ustadmobile.staging.port.android.view

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PersonListSearchPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.PersonListSearchView
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PersonListSearchActivity : UstadBaseActivity(), PersonListSearchView {


    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mPresenter: PersonListSearchPresenter
    private lateinit var searchView: SearchView

    private var apl = 0.0f
    private var aph = 1.0f
    private var days = 0
    private var currentValue = ""

    private lateinit var attendanceRangeSeekbar: CrystalRangeSeekbar
    private lateinit var daysAbsentSeekbar: CrystalSeekbar
    private lateinit var rangeTextView: TextView
    private lateinit var daysAbsentTextView: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Set layout
        setContentView(R.layout.activity_person_list_search)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.activity_person_list_search_toolbar)
        toolbar.setTitle(R.string.students_literal)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(R.id.activity_person_list_search_rv)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager)

        attendanceRangeSeekbar = findViewById(R.id.activity_person_list_search_attendance_range_seekbar)
        daysAbsentSeekbar = findViewById(R.id.activity_person_list_search_days_absent_seekbar)
        rangeTextView = findViewById(R.id.activity_person_list_search_range_textview)
        daysAbsentTextView = findViewById(R.id.activity_person_list_search_days_absent_textview)


        //Presenter
        mPresenter = PersonListSearchPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        daysAbsentSeekbar!!.setOnSeekbarChangeListener({ value ->
            updateDaysAbsentText(value.toInt())
            days = value.toInt()

        })

        attendanceRangeSeekbar!!.setOnRangeSeekbarChangeListener({ minValue, maxValue ->
            updateAttendanceRangeText(minValue.toInt(), maxValue.toInt())
            if (minValue.toFloat() > 0)
                apl = minValue.toInt() / 100f
            if (maxValue.toFloat() > 0)
                aph = maxValue.toInt() / 100f

        })

        attendanceRangeSeekbar!!.setOnRangeSeekbarFinalValueListener(object : OnRangeSeekbarFinalValueListener {
            override fun finalValue(minValue: Number, maxValue: Number) {
                if (minValue.toFloat() > 0)
                    apl = minValue.toInt() / 100f
                if (maxValue.toFloat() > 0)
                    aph = maxValue.toInt() / 100f
                mPresenter.updateFilter(apl, aph, currentValue)
            }
        })

        updateDaysAbsentText(0)
        updateAttendanceRangeText(0, 100)
    }

    fun updateDaysAbsentText(days: Int) {
        val daysAbsentText = getText(R.string.over).toString() + " " + days + " " +
                getText(R.string.days).toString().toLowerCase()
        daysAbsentTextView.text = daysAbsentText
    }

    fun updateAttendanceRangeText(from: Int, to: Int) {
        val rangeText = from.toString() + "% " + getText(R.string.to).toString() + " " + to + "%"
        rangeTextView.text = rangeText
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search)
                .actionView as SearchView
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(componentName))

        searchView.setMaxWidth(Integer.MAX_VALUE)

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                currentValue = query
                // filter recycler view when query submitted
                mPresenter.updateFilter(apl, aph, currentValue)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                currentValue = query
                // filter recycler view when text is changed
                mPresenter.updateFilter(apl, aph, currentValue)
                return false
            }
        })

        searchView.setOnCloseListener({
            currentValue = ""
            mPresenter.updateFilter(apl, aph, currentValue)
            false
        })


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true)
            return
        }
        super.onBackPressed()
    }

    override fun setPeopleListProvider(factory: DataSource.Factory<Int, PersonWithEnrollment>) {

        val recyclerAdapter = PersonWithEnrollmentRecyclerAdapter(DIFF_CALLBACK2, applicationContext,
                this, mPresenter, true, false,
                false, false, true)
        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithEnrollment>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK2: DiffUtil.ItemCallback<PersonWithEnrollment> = object : DiffUtil.ItemCallback<PersonWithEnrollment>() {
            override fun areItemsTheSame(oldItem: PersonWithEnrollment,
                                         newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithEnrollment,
                                            newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }
        }
    }


}
