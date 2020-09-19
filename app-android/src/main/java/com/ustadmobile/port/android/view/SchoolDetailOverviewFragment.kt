package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolOverviewBinding
import com.toughra.ustadmobile.databinding.ItemClazzSimpleDetailBinding
import com.ustadmobile.core.controller.SchoolDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolDetailOverviewView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap

interface SchoolDetailOverviewEventListener {
    fun onClickSchoolCode(code: String?)
}

class SchoolDetailOverviewFragment: UstadDetailFragment<SchoolWithHolidayCalendar>(),
        SchoolDetailOverviewView, SchoolDetailOverviewEventListener,
        Observer<PagedList<Clazz>>{

    private var mBinding: FragmentSchoolOverviewBinding? = null

    private var mPresenter: SchoolDetailOverviewPresenter? = null

    private var clazzRecyclerAdapter: ClazzRecyclerAdapter? = null

    private var clazzRecyclerView : RecyclerView? = null

    protected var currentLiveData: LiveData<PagedList<Clazz>>? = null

    private val clazzObserver = Observer<List<Clazz>?>{
        t -> clazzRecyclerAdapter?.submitList(t)
    }

    override var schoolClazzes: DataSource.Factory<Int, Clazz>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(this)
            currentLiveData = value?.asRepositoryLiveData(ClazzDao)
            currentLiveData?.observe(this, this)
        }

    class ClazzRecyclerAdapter(var presenter: SchoolDetailOverviewPresenter?)
        : ListAdapter<Clazz,
            ClazzRecyclerAdapter.ClazzViewHolder>(SchoolEditFragment.DIFF_CALLBACK_CLAZZ) {

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
                this,  di, viewLifecycleOwner)

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

    override fun onChanged(t: PagedList<Clazz>?) {
        clazzRecyclerAdapter?.submitList(t)
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onClickSchoolCode(code: String?) {
        mPresenter?.handleGoToInviteViaLink()
    }

}