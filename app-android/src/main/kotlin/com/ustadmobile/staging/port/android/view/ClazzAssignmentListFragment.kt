package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentListBinding
import com.ustadmobile.core.controller.ClazzAssignmentListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.port.android.view.ClazzAssignmentListRecyclerAdapter
import com.ustadmobile.port.android.view.UstadBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
class ClazzAssignmentListFragment : UstadBaseFragment(), ClazzAssignmentListView {
    override val viewContext: Any
        get() = requireContext()

    private var rootContainer: FragmentClazzAssignmentListBinding? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: ClazzAssignmentListPresenter? = null
    internal var fab: FloatingTextButton? = null

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

        rootContainer = FragmentClazzAssignmentListBinding.inflate(
                LayoutInflater.from(context), container, false)

        mRecyclerView = rootContainer?.fragmentClazzAssignmentListRecyclerview
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = mRecyclerLayoutManager
        //set up Presenter
        mPresenter = ClazzAssignmentListPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        rootContainer?.presenter = mPresenter

        return rootContainer?.root
    }

    /**
     * Sets the provider to the view.
     *
     * @param setListProvider The UMProvider provider of ClazzWithNumStudents Type.
     */
    override fun setListProvider(factory : DataSource.Factory<Int, ClazzAssignmentWithMetrics>) {
        val recyclerAdapter = ClazzAssignmentListRecyclerAdapter(DIFF_CALLBACK, mPresenter)

        // a warning is expected.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzAssignmentWithMetrics>> { recyclerAdapter.submitList(it) })
        }

        mRecyclerView?.adapter = recyclerAdapter
    }

    override fun setEditVisibility(visible: Boolean) {
        if(visible) {
            rootContainer?.fragmentClazzAssignmentListFab?.visibility = View.VISIBLE
        }else{
            rootContainer?.fragmentClazzAssignmentListFab?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootContainer = null
        mPresenter = null
        mRecyclerView?.adapter = null
        mRecyclerView = null
    }

    companion object {
        val title = R.string.assignments

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(): ClazzAssignmentListFragment {
            val fragment = ClazzAssignmentListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(args: Bundle): ClazzAssignmentListFragment {
            val fragment = ClazzAssignmentListFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzAssignmentWithMetrics> = object
            : DiffUtil.ItemCallback<ClazzAssignmentWithMetrics>() {
            override fun areItemsTheSame(oldItem: ClazzAssignmentWithMetrics,
                                         newItem: ClazzAssignmentWithMetrics): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ClazzAssignmentWithMetrics,
                                            newItem: ClazzAssignmentWithMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }

}
