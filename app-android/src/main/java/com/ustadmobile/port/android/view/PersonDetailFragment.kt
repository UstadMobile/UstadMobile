package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzDetailBinding
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailFragmentEventHandler {
    fun handleClickDialNumber(number: String)

    fun handleClickEmail(emailAddr: String)

    fun handleClickSms(number: String)
}

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView, PersonDetailFragmentEventHandler {

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    var dbRepo: UmAppDatabase? = null

    class ClazzMemberWithClazzRecyclerAdapter(val activityEventHandler: PersonDetailFragmentEventHandler,
            var presenter: PersonDetailPresenter?): ListAdapter<ClazzMemberWithClazz, ClazzMemberWithClazzRecyclerAdapter.ClazzMemberWithClazzViewHolder>(DIFFUTIL_CLAZZMEMBERWITHCLAZZ) {

            class ClazzMemberWithClazzViewHolder(val binding: ItemClazzMemberWithClazzDetailBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzMemberWithClazzViewHolder {
                val viewHolder = ClazzMemberWithClazzViewHolder(ItemClazzMemberWithClazzDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false).apply {
                    mPresenter = presenter
                })

                return viewHolder
            }

            override fun onBindViewHolder(holder: ClazzMemberWithClazzViewHolder, position: Int) {
                holder.binding.clazzMemberWithClazz = getItem(position)
            }
        }

    override var clazzes: DataSource.Factory<Int, ClazzMemberWithClazz>? = null
        get() = field
        set(value) {
            clazzesLiveData?.removeObserver(clazzMemberWithClazzObserver)
            field = value
            val clazzMemberDao = dbRepo?.clazzMemberDao ?: return
            clazzesLiveData = value?.asRepositoryLiveData(clazzMemberDao)
            clazzesLiveData?.observe(viewLifecycleOwner, clazzMemberWithClazzObserver)
        }

    private var clazzesLiveData: LiveData<PagedList<ClazzMemberWithClazz>>? = null

    private var clazzMemberWithClazzRecyclerAdapter: ClazzMemberWithClazzRecyclerAdapter? = null

    private val clazzMemberWithClazzObserver = Observer<PagedList<ClazzMemberWithClazz>?> {
        t -> clazzMemberWithClazzRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        clazzMemberWithClazzRecyclerAdapter = ClazzMemberWithClazzRecyclerAdapter(this,
            null)
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentEventHandler = this
            it.classesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.classesRecyclerview.adapter = clazzMemberWithClazzRecyclerAdapter
        }

        dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        clazzMemberWithClazzRecyclerAdapter?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.classesRecyclerview?.adapter = null
        clazzMemberWithClazzRecyclerAdapter = null
        dbRepo = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    override fun handleClickDialNumber(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            setData(Uri.parse("tel:$number"))
        }

        if(callIntent.resolveActivity(requireContext().packageManager) != null)
            requireContext().startActivity(callIntent)
    }

    override fun handleClickEmail(emailAddr: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddr))
            data = Uri.parse("mailto:$emailAddr")
        }
        if(emailIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(emailIntent)
        }
    }

    override fun handleClickSms(number: String) {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
        }
        if(smsIntent.resolveActivity(requireContext().packageManager) != null) {
            requireContext().startActivity(smsIntent)
        }
    }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title = value?.firstNames + " " + value?.lastName
        }


    companion object {

        val DIFFUTIL_CLAZZMEMBERWITHCLAZZ = object: DiffUtil.ItemCallback<ClazzMemberWithClazz>() {
            override fun areItemsTheSame(oldItem: ClazzMemberWithClazz, newItem: ClazzMemberWithClazz): Boolean {
                return oldItem.clazzMemberUid == newItem.clazzMemberUid
            }

            override fun areContentsTheSame(oldItem: ClazzMemberWithClazz, newItem: ClazzMemberWithClazz): Boolean {
                return oldItem == newItem
            }
        }

    }

}