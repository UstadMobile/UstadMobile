package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.PeopleListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PeopleListView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.port.android.view.UstadBaseFragment
import com.ustadmobile.staging.core.view.SearchableListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * PeopleListFragment responsible for showing people list on the people bottom navigation
 */
class PeopleListFragment : UstadBaseFragment, PeopleListView, SearchableListener {


    override val viewContext: Any
        get() = requireContext()

    internal var rootContainer: View ? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: PeopleListPresenter? = null
    private var fab: FloatingTextButton? = null

    internal var sortSpinner: Spinner? = null
    internal lateinit var sortSpinnerPresets: Array<String?>

    private var activityMode : Boolean = false

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

        rootContainer = inflater.inflate(R.layout.fragment_people_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer!!.findViewById(R.id.fragment_people_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        fab = rootContainer!!.findViewById(R.id.fragment_people_list_fab)
        sortSpinner = rootContainer!!.findViewById(R.id.fragment_people_list_sort_spinner2)

        //set up Presenter
        mPresenter = PeopleListPresenter(requireContext(),
                UMAndroidUtil.bundleToMap(arguments), this, this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        fab!!.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

        if(activityMode){
            val layout = rootContainer!!.findViewById<ConstraintLayout>(
                    R.id.fragment_people_list_root)
            layout.setPadding(0,0,0,24)
        }

        sortSpinner!! .onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        return rootContainer
    }

    fun searchPeople(searchValue: String) {
        mPresenter?.updateProviderWithSearch(searchValue)
    }

    override fun setPeopleListProvider(factory : DataSource.Factory<Int, PersonWithEnrollment>) {

        val recyclerAdapter = PersonWithEnrollmentRecyclerAdapter(requireContext(), DIFF_CALLBACK,
                this, mPresenter!!, false, false)

        //personDao.findAllPeopleWithEnrollmentBySearch
        val data = factory.asRepositoryLiveData(
                UmAccountManager.getRepositoryForActiveAccount(requireContext()).personDao)


        data.observe(this@PeopleListFragment,
                Observer<PagedList<PersonWithEnrollment>> { recyclerAdapter.submitList(it) })

        mRecyclerView?.setAdapter(recyclerAdapter)
    }

    override fun showFAB(show: Boolean) {
        fab?.visibility = if (show) View.VISIBLE else View.INVISIBLE
        fab?.isEnabled = show
    }

    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    override fun onSearchButtonClick() {
        //Does nothing here
    }

    override fun onSearchQueryUpdated(query: String) {
        searchPeople(query)
    }

    constructor()  {
        val args = Bundle()
        arguments = args
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.fab = null
        this.mPresenter = null
        this.mRecyclerView?.adapter = null
        this.mRecyclerView = null
        this.rootContainer = null
        this.sortSpinner = null
    }

    companion object {
        val icon = R.drawable.ic_person_black_24dp
        val title = R.string.bottomnav_people_title

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment PeopleListFragment.
         */
        fun newInstance(): PeopleListFragment {
            val fragment = PeopleListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(fromActivity: Boolean): PeopleListFragment {
            val fragment = PeopleListFragment()
            fragment.activityMode = fromActivity
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithEnrollment> = object
            : DiffUtil.ItemCallback<PersonWithEnrollment>() {
            override fun areItemsTheSame(oldItem: PersonWithEnrollment,
                                         newItem: PersonWithEnrollment): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithEnrollment,
                                            newItem: PersonWithEnrollment): Boolean {
                return oldItem == newItem
            }
        }
    }
}
