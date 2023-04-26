package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.toughra.ustadmobile.databinding.ItemStudentNoPasswordStudentBinding
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.presenter.StudentNoPasswordSignOnStudentListPresenter
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.attachments.retrieveAttachment
import kotlinx.coroutines.Job

class StudentNoPasswordSignOnStudentListFragment: UstadBaseFragment(

), StudentNoPasswordSignOnStudentListView{

    class StudentNoPasswordStudentViewHolder(
        val binding: ItemStudentNoPasswordStudentBinding,
    ): ViewHolder(binding.root) {
        var imageJob: Job? = null
    }

    inner class StudentNoPasswordStudentListAdapter(

    ) : PagedListAdapter<Person, StudentNoPasswordStudentViewHolder>(DIFF_UTIL){

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StudentNoPasswordStudentViewHolder {
            val binding = ItemStudentNoPasswordStudentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return StudentNoPasswordStudentViewHolder(binding)
        }

        override fun onBindViewHolder(holder: StudentNoPasswordStudentViewHolder, position: Int) {
            val person = getItem(position)
            val endpoint = studentList?.endpoint ?: return

            holder.binding.person = person

            if(person != null) {
                holder.binding.root.setOnClickListener {
                    mPresenter?.onClickPerson(person)
                }

                holder.imageJob = viewLifecycleOwner.lifecycleScope.launch {
                    val db: UmAppDatabase = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
                    val personPicture = db.personPictureDao.findByPersonUidAsync(person.personUid)
                    val doorUri = personPicture?.personPictureUri?.let { db.retrieveAttachment(it) }

                    if(doorUri != null) {
                        Picasso.get().load(doorUri.uri).into(holder.binding.itemPersonNewitemicon)
                    }else {
                        holder.binding.itemPersonNewitemicon.setImageDrawable(
                            ContextCompat.getDrawable(holder.itemView.context,
                                R.drawable.ic_account_circle_black_24dp))
                    }
                }
            }else {
                holder.binding.root.setOnClickListener(null)
            }
        }
    }


    private var mBinding: FragmentListBinding? = null

    private val mObserver: Observer<PagedList<Person>> = Observer {
        mListAdapter?.submitList(it)
    }

    private var mListAdapter: StudentNoPasswordStudentListAdapter? = null

    private var mPagedLiveData: LiveData<PagedList<Person>>? = null

    override var studentList: StudentNoPasswordSignOnStudentListPresenter.NoPasswordStudentList? = null
        set(value) {
            mPagedLiveData?.removeObserver(mObserver)

            field = value

            if(value != null) {
                mPagedLiveData = LivePagedListBuilder(value.students, 20).build()
                mPagedLiveData?.observe(viewLifecycleOwner, mObserver)
            }
        }

    private var mPresenter: StudentNoPasswordSignOnStudentListPresenter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mListAdapter = StudentNoPasswordStudentListAdapter()
        return FragmentListBinding.inflate(inflater, container, false).also {
            mBinding = it
            it.fragmentListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.fragmentListRecyclerview.adapter = mListAdapter
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = StudentNoPasswordSignOnStudentListPresenter(requireContext(),
            arguments.toStringMap(), this, di)
        mPresenter?.onCreate(emptyMap())
    }

    companion object {

        val DIFF_UTIL = object: DiffUtil.ItemCallback<Person>() {
            override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
                return oldItem.personFullName() == newItem.personFullName()
            }
        }

    }

}