package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClazzListView
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.port.android.view.UstadBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
class ClazzListFragment : UstadBaseFragment, ClazzListView {
    override val viewContext: Any
        get() = context!!

    //RecyclerView
    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: ClazzListPresenter? = null
    internal var sortSpinner: Spinner ? = null
    internal lateinit var sortSpinnerPresets: Array<String?>
    internal var fab: FloatingTextButton ? = null

    private var mOptionsMenu: Menu? = null

    private var showAllClazzSettingsButton = false

    private var pullToRefresh: SwipeRefreshLayout? = null

    fun searchClasses(searchValue: String) {
        mPresenter!!.updateProviderWithSearch(searchValue)
    }

    /**
     * On Create of the fragment.
     *
     * @param savedInstanceState    The bundle state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootContainer = inflater.inflate(R.layout.fragment_clazz_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer.findViewById(R.id.fragment_class_list_recyclerview)

        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        sortSpinner = rootContainer.findViewById(R.id.fragment_clazz_list_sort_spinner)

        fab = rootContainer.findViewById(R.id.fragment_clazz_list_fab)
        fab!!.setOnClickListener { v -> mPresenter!!.handleClickPrimaryActionButton() }

        pullToRefresh = rootContainer.findViewById(R.id.fragment_clazz_list_swipe_refresh_layout)

        //set up Presenter
        mPresenter = ClazzListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        sortSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                mPresenter!!.handleChangeSortOrder(id)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        pullToRefresh!!.setOnRefreshListener {
            try {
                Thread.sleep(300)
                (Objects.requireNonNull(activity) as BasePointActivity).forceSync()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            pullToRefresh!!.isRefreshing = false
        }

        return rootContainer
    }

    /**
     * Sets the provider to the view.
     *
     * @param clazzListProvider The UMProvider provider of ClazzWithNumStudents Type.
     */
    override fun setClazzListProvider(factory : DataSource.Factory<Int, ClazzWithNumStudents>) {
        val recyclerAdapter = ClazzListRecyclerAdapter(DIFF_CALLBACK,
                context!!, this, mPresenter!!)

        // a warning is expected.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzWithNumStudents>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView!!.adapter = recyclerAdapter
    }

    /**
     * Updates the sort spinner with string list given
     *
     * @param presets A String array String[] of the presets available.
     */
    override fun updateSortSpinner(presets: Array<String?>) {
        this.sortSpinnerPresets = presets
        val adapter = ArrayAdapter(context!!,
                R.layout.item_simple_spinner_gray, sortSpinnerPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner!!.adapter = adapter
    }

    override fun showAddClassButton(show: Boolean) {
        runOnUiThread (Runnable{
            if (show) {
                fab!!.visibility = View.VISIBLE
            } else {
                fab!!.visibility = View.INVISIBLE
            }
        })

    }

    override fun showAllClazzSettingsButton(show: Boolean) {
        showAllClazzSettingsButton = show
    }

    override fun forceCheckPermissions() {
        if (mPresenter != null)
            mPresenter!!.checkPermissions()
    }

    fun showSettings() {

        val allClazzSettingsMenuItem = mOptionsMenu!!.findItem(R.id.menu_action_settings)
        if (allClazzSettingsMenuItem != null) {
            allClazzSettingsMenuItem.isVisible = showAllClazzSettingsButton
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        mOptionsMenu = menu
        showSettings()
    }

    override fun showMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val message = impl.getString(messageId, context!!)

        runOnUiThread (Runnable{
            Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_SHORT
            ).show()
        })

    }

    constructor()  {
        val args = Bundle()
        arguments = args
        icon = R.drawable.ic_group_black_24dp
        title = R.string.bottomnav_classes_title
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(): ClazzListFragment {
            val fragment = ClazzListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithNumStudents> = object
            : DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            override fun areItemsTheSame(oldItem: ClazzWithNumStudents,
                                         newItem: ClazzWithNumStudents): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ClazzWithNumStudents,
                                            newItem: ClazzWithNumStudents): Boolean {
                return oldItem == newItem
            }
        }
    }

}
