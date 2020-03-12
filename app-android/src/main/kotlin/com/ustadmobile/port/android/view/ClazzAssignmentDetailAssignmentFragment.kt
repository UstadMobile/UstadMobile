package com.ustadmobile.port.android.view


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
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailAssignmentBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailAssignmentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzAssignmentDetailAssignmentView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
class ClazzAssignmentDetailAssignmentFragment : UstadBaseFragment(), ClazzAssignmentDetailAssignmentView {

    override fun setEditVisibility(visible: Boolean) {
        if(visible){
            rootContainer?.fragmentClazzAssignmentDetailAssignmentEdit?.visibility = View.VISIBLE
        }else{
            rootContainer?.fragmentClazzAssignmentDetailAssignmentEdit?.visibility = View.INVISIBLE
        }
    }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignment) {
        rootContainer?.clazzassignment = clazzAssignment
        rootContainer?.fragmentClazzAssignmentDetailAssignmentDate?.text =
                UMCalendarUtil.getPrettyDateSimpleFromLong(clazzAssignment.clazzAssignmentDueDate,
                        null)

    }

    override val viewContext: Any
        get() = requireContext()

    private var rootContainer: FragmentClazzAssignmentDetailAssignmentBinding? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: ClazzAssignmentDetailAssignmentPresenter? = null

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

        rootContainer = FragmentClazzAssignmentDetailAssignmentBinding.inflate(
                LayoutInflater.from(context), container, false)

        mRecyclerView = rootContainer?.fragmentClazzAssignmentDetailAssignmentRecyclerview
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = mRecyclerLayoutManager
        //set up Presenter
        mPresenter = ClazzAssignmentDetailAssignmentPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        rootContainer?.presenter = mPresenter

        return rootContainer?.root
    }

    override fun setListProvider(factory: DataSource.Factory<Int, ContentEntryWithMetrics>) {
        val recyclerAdapter = ContentEntryWithMetricsRecyclerAdapter(DIFF_CALLBACK_CONTENT_WITH_METRICS)

        // a warning is expected.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ContentEntryWithMetrics>> { recyclerAdapter.submitList(it) })
        }
        mRecyclerView?.adapter = recyclerAdapter
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
        fun newInstance(): ClazzAssignmentDetailAssignmentFragment {
            val fragment = ClazzAssignmentDetailAssignmentFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(args: Bundle?): ClazzAssignmentDetailAssignmentFragment {
            val fragment = ClazzAssignmentDetailAssignmentFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK_CONTENT_WITH_METRICS: DiffUtil.ItemCallback<ContentEntryWithMetrics> = object
            : DiffUtil.ItemCallback<ContentEntryWithMetrics>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithMetrics,
                                         newItem: ContentEntryWithMetrics): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithMetrics,
                                            newItem: ContentEntryWithMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }

}
