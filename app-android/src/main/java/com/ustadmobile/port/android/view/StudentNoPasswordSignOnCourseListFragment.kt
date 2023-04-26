package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.toughra.ustadmobile.databinding.ItemStudentNoPasswordCourseBinding
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.port.android.presenter.StudentNoPasswordSignOnCourseListPresenter

class StudentNoPasswordSignOnCourseListFragment: UstadBaseFragment(

), StudentNoPasswordSignOnCourseListView {


    //Switch to binding
    class NoPasswordCourseViewHolder(
        val binding: ItemStudentNoPasswordCourseBinding,
    ): ViewHolder(binding.root)

    inner class NoPasswordCourseListAdapter(
        private val endpoint: Endpoint
    ): PagedListAdapter<Clazz, NoPasswordCourseViewHolder>(CLAZZ_DIFF_UTIL) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NoPasswordCourseViewHolder {
            val binding =ItemStudentNoPasswordCourseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return NoPasswordCourseViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NoPasswordCourseViewHolder, position: Int) {
            val clazz = getItem(position)
            if(clazz != null) {
                holder.binding.studentNoPasswordCourseLinearLayout.setOnClickListener {
                    mPresenter?.onClickCourse(endpoint, clazz)
                }
            }else {
                holder.binding.studentNoPasswordCourseLinearLayout.setOnClickListener(null)
            }

            holder.binding.clazz = clazz
        }
    }

    private var mPresenter: StudentNoPasswordSignOnCourseListPresenter? = null

    private var mBinding: FragmentListBinding? = null

    private var mObservers: List<Observer<PagedList<Clazz>>>? = null

    private var mLiveDataList: List<LiveData<PagedList<Clazz>>>? = null


    private var mAdapters: List<NoPasswordCourseListAdapter>? = null

    override var coursesLists: List<StudentNoPasswordSignOnCourseListPresenter.EndpointNoPasswordCourseList>? = null
        set(value) {
            mLiveDataList?.forEachIndexed { index, liveData ->
                mObservers?.getOrNull(index)?.also { observer ->
                    liveData.removeObserver(observer)
                }
            }

            field = value
            if(value == null){
                mBinding?.fragmentListRecyclerview?.adapter = ConcatAdapter(emptyList())
                return
            }

            mLiveDataList = value.map {
                LivePagedListBuilder(it.courseList, 20).build()
            }

            mAdapters = mLiveDataList?.let {
                List(it.size) { index ->
                    NoPasswordCourseListAdapter(value[index].endpoint)
                }
            }

            mObservers = mLiveDataList?.let { liveDataList ->
                List(liveDataList.size){ index ->
                    Observer<PagedList<Clazz>> { pagedList ->
                         mAdapters?.getOrNull(index)?.submitList(pagedList)
                    }.also {
                        mLiveDataList?.getOrNull(index)?.observe(viewLifecycleOwner, it)
                    }
                }
            }

            mBinding?.fragmentListRecyclerview?.adapter = ConcatAdapter(
                mAdapters ?: emptyList()
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentListBinding.inflate(inflater, container, false).also {
            mBinding = it
            it.fragmentListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = StudentNoPasswordSignOnCourseListPresenter(requireContext(),
            arguments.toStringMap(), this, di).withViewLifecycle()
        mPresenter?.onCreate(backStackSavedState)
    }

    companion object {

        val CLAZZ_DIFF_UTIL = object: DiffUtil.ItemCallback<Clazz>() {
            override fun areItemsTheSame(oldItem: Clazz, newItem: Clazz): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: Clazz, newItem: Clazz): Boolean {
                return oldItem.clazzName == newItem.clazzName
            }
        }

    }
}