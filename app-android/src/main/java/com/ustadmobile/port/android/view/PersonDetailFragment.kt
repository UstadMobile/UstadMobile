package com.ustadmobile.port.android.view

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
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentPersonDetailBinding
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.CustomField
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

interface PersonDetailFragmentEventHandler {

}

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView, PersonDetailFragmentEventHandler {

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    private var canManageAccount: Boolean = false

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    var dbRepo: UmAppDatabase? = null

    class ClazzMemberWithClazzRecyclerAdapter(val activityEventHandler: PersonDetailFragmentEventHandler,
            var presenter: PersonDetailPresenter?): ListAdapter<ClazzMemberWithClazz, ClazzMemberWithClazzRecyclerAdapter.ClazzMemberWithClazzViewHolder>(DIFFUTIL_CLAZZMEMBERWITHCLAZZ) {

            class ClazzMemberWithClazzViewHolder(val binding: ItemClazzMemberWithClazzDetailBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzMemberWithClazzViewHolder {

                return ClazzMemberWithClazzViewHolder(ItemClazzMemberWithClazzDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false).apply {
                    mPresenter = presenter
                })
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

    override var changePasswordVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.changePasswordVisibility = if(value && canManageAccount)
                View.VISIBLE else View.GONE
        }

    override var showCreateAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.createAccountVisibility = if(value && canManageAccount)
                View.VISIBLE else View.GONE
        }

    private var clazzesLiveData: LiveData<PagedList<ClazzMemberWithClazz>>? = null

    private var clazzMemberWithClazzRecyclerAdapter: ClazzMemberWithClazzRecyclerAdapter? = null

    private val clazzMemberWithClazzObserver = Observer<PagedList<ClazzMemberWithClazz>?> {
        t -> clazzMemberWithClazzRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        val impl: UstadMobileSystemImpl by instance()
        canManageAccount = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_ACCOUNT_MANAGEMENT, requireContext())
        clazzMemberWithClazzRecyclerAdapter = ClazzMemberWithClazzRecyclerAdapter(this,
            null)
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentEventHandler = this
            it.createAccountVisibility = View.GONE
            it.changePasswordVisibility = View.GONE
            it.classesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.classesRecyclerview.adapter = clazzMemberWithClazzRecyclerAdapter
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        clazzMemberWithClazzRecyclerAdapter?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        mBinding?.presenter = mPresenter
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

        @JvmField
        val FIELD_ICON_ID_MAP : Map<Int, Int> =
            mapOf(CustomField.ICON_PHONE to R.drawable.ic_phone_black_24dp,
                    CustomField.ICON_PERSON to R.drawable.ic_person_black_24dp,
                    CustomField.ICON_CALENDAR to R.drawable.ic_event_black_24dp,
                    CustomField.ICON_EMAIL to R.drawable.ic_email_black_24dp,
                    CustomField.ICON_ADDRESS to R.drawable.ic_location_pin_24dp)

    }

}