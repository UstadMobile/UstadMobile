package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.paging.DataSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SchoolListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.SchoolListView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.view.UstadBaseFragment
import com.ustadmobile.staging.core.view.SearchableListener
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * SchoolListFragment responsible for showing people list on the people bottom navigation
 */
class SchoolListFragment : UstadBaseFragment(), SchoolListView, SearchableListener {


    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View 
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mPresenter: SchoolListPresenter
    private lateinit var fab: FloatingTextButton

    internal lateinit var sortSpinner: Spinner
    internal lateinit var sortSpinnerPresets: Array<String>

    private lateinit var pullToRefresh: SwipeRefreshLayout

    /**
     * On Create of the View fragment. Sets up the presenter and the floating action button's
     * on click listener.
     *
     * @param inflater              The inflater
     * @param container             The view group container
     * @param savedInstanceState    The saved instance state
     * @return                      The view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_school_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer.findViewById(R.id.fragment_school_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager)

        fab = rootContainer.findViewById(R.id.fragment_school_list_fab)
        sortSpinner = rootContainer.findViewById(R.id.fragment_school_list_sort_spinner2)

        //set up Presenter
        mPresenter = SchoolListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        pullToRefresh = rootContainer.findViewById(R.id.fragment_school_list_swiperefreshlayout)

        fab.setOnClickListener { v -> mPresenter.handleClickAddSchool() }
        

        sortSpinner .onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter.handleSortChanged(position.toLong())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        pullToRefresh.setOnRefreshListener{
            try {
                Thread.sleep(300)

            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            pullToRefresh.setRefreshing(false)
        }

        return rootContainer
    }

    fun searchPeople(searchValue: String) {
        mPresenter.handleSearchQuery(searchValue)
    }

    override fun setListProvider(listProvider: DataSource.Factory<Int, School>) {
        //TODO
    }


    override fun setSortOptions(presets: Array<String>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter
    }

    override fun onSearchButtonClick() {
        //Does nothing here
    }

    override fun onSearchQueryUpdated(query: String) {
        searchPeople(query)
    }
    

    companion object {
        val icon = R.drawable.ic_school_black_24dp
        val title = R.string.schools

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment PeopleListFragment.
         */
        fun newInstance(): SchoolListFragment {
            val fragment = SchoolListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(fromActivity: Boolean): SchoolListFragment {
            val fragment = SchoolListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
        
    }
}
