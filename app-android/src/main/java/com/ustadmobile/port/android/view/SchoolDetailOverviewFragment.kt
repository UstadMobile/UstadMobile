package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolOverviewBinding
import com.toughra.ustadmobile.databinding.ItemClazzSimpleDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.SchoolDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase

interface SchoolDetailOverviewEventListener {
    fun onClickSchoolCode(code: String?)
}

class SchoolDetailOverviewFragment: UstadDetailFragment<SchoolWithHolidayCalendar>(),
        SchoolDetailOverviewView, SchoolDetailOverviewEventListener,
        Observer<PagedList<ClazzWithListDisplayDetails>>{

    private var mBinding: FragmentSchoolOverviewBinding? = null

    private var mPresenter: SchoolDetailOverviewPresenter? = null

    private var clazzRecyclerAdapter: ClazzRecyclerAdapter? = null

    private var clazzRecyclerView : RecyclerView? = null

    protected var currentLiveData: LiveData<PagedList<ClazzWithListDisplayDetails>>? = null

    private val accountManager: UstadAccountManager by instance()

    private val repo: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    private val clazzObserver = Observer<List<ClazzWithListDisplayDetails>?>{
        t -> clazzRecyclerAdapter?.submitList(t)
    }

    override var schoolClazzes: DataSource.Factory<Int, ClazzWithListDisplayDetails>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(this)
            currentLiveData = value?.asRepositoryLiveData(repo)
            currentLiveData?.observe(this, this)
        }

    class ClazzRecyclerAdapter(var presenter: SchoolDetailOverviewPresenter?)
        : ListAdapter<ClazzWithListDisplayDetails,
            ClazzRecyclerAdapter.ClazzViewHolder>(DIFF_CALLBACK_CLAZZ) {

        class ClazzViewHolder(val binding: ItemClazzSimpleDetailBinding)
            : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzViewHolder {
            return ClazzViewHolder(ItemClazzSimpleDetailBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ClazzViewHolder, position: Int) {
            holder.binding.clazz = getItem(position)
            holder.binding.mPresenter = presenter
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolOverviewBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.fragmentEventHandler = this
        }

        clazzRecyclerView = rootView.findViewById(R.id.fragment_school_detail_overview_detail_clazz_rv)

        mPresenter = SchoolDetailOverviewPresenter(requireContext(), arguments.toStringMap(),
                this,  di, viewLifecycleOwner).withViewLifecycle()

        clazzRecyclerAdapter = ClazzRecyclerAdapter(mPresenter)
        clazzRecyclerView?.adapter = clazzRecyclerAdapter
        clazzRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        clazzRecyclerView = null
        clazzRecyclerAdapter = null
    }

    override var entity: SchoolWithHolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            mBinding?.schoolWithHolidayCalendar = value
        }

    override var schoolCodeVisible: Boolean
        get() = mBinding?.schoolCodeVisible ?: false
        set(value) {
            mBinding?.schoolCodeVisible = value
        }

    override fun onChanged(t: PagedList<ClazzWithListDisplayDetails>?) {
        clazzRecyclerAdapter?.submitList(t)
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onClickSchoolCode(code: String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", code)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
    }

    companion object{
        val DIFF_CALLBACK_CLAZZ = object: DiffUtil.ItemCallback<ClazzWithListDisplayDetails>() {
            override fun areItemsTheSame(oldItem: ClazzWithListDisplayDetails, newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithListDisplayDetails,
                                            newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem == newItem
            }
        }
    }

}