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
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailProgressBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailProgressPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzAssignmentDetailProgressView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAssignmentMetrics
import com.ustadmobile.port.android.view.PersonWithAssignmentMetricsRecyclerAdapter
import com.ustadmobile.port.android.view.UstadBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * ClazzListFragment Android fragment extends UstadBaseFragment
 */
class ClazzAssignmentDetailProgressFragment : UstadBaseFragment(), ClazzAssignmentDetailProgressView {


    override val viewContext: Any
        get() = requireContext()

    private var rootContainer: FragmentClazzAssignmentDetailProgressBinding? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPresenter: ClazzAssignmentDetailProgressPresenter? = null

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

        rootContainer = FragmentClazzAssignmentDetailProgressBinding.inflate(
                LayoutInflater.from(context), container, false)

        mRecyclerView = rootContainer?.fragmentClazzAssignmentDetailProgressRecyclerview
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.layoutManager = mRecyclerLayoutManager
        //set up Presenter
        mPresenter = ClazzAssignmentDetailProgressPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter?.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        rootContainer?.presenter = mPresenter

        return rootContainer?.root
    }

    override fun setListProvider(factory: DataSource.Factory<Int, PersonWithAssignmentMetrics>) {
        val recyclerAdapter = PersonWithAssignmentMetricsRecyclerAdapter(DIFF_CALLBACK_PERSON_WITH_METRICS, mPresenter)

        // a warning is expected.
        val data = LivePagedListBuilder(factory, 20).build()
        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<PersonWithAssignmentMetrics>> { recyclerAdapter.submitList(it) })
        }
        mRecyclerView?.adapter = recyclerAdapter
    }

    override fun setClazzAssignment(clazzAssignment: ClazzAssignmentWithMetrics) {
        //TODO: Remove if not useful
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootContainer = null
        mPresenter = null
        mRecyclerView?.adapter = null
        mRecyclerView = null
    }

    companion object {
        val title = R.string.student_progress

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(): ClazzAssignmentDetailProgressFragment {
            val fragment = ClazzAssignmentDetailProgressFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzListFragment.
         */
        fun newInstance(args: Bundle?): ClazzAssignmentDetailProgressFragment {
            val fragment = ClazzAssignmentDetailProgressFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * The DIFF Callback.
         */
        val DIFF_CALLBACK_PERSON_WITH_METRICS: DiffUtil.ItemCallback<PersonWithAssignmentMetrics> = object
            : DiffUtil.ItemCallback<PersonWithAssignmentMetrics>() {
            override fun areItemsTheSame(oldItem: PersonWithAssignmentMetrics,
                                         newItem: PersonWithAssignmentMetrics): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: PersonWithAssignmentMetrics,
                                            newItem: PersonWithAssignmentMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }

}
