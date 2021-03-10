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
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentWithClazzDetailBinding
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
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class PersonDetailFragment: UstadDetailFragment<PersonWithDisplayDetails>(), PersonDetailView,
        EntityRoleItemHandler{

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

    private var canManageAccount: Boolean = false

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    var dbRepo: UmAppDatabase? = null

    class ClazzEnrolmentWithClazzRecyclerAdapter(var presenter: PersonDetailPresenter?)
        : ListAdapter<ClazzEnrolmentWithClazzAndAttendance,
                ClazzEnrolmentWithClazzRecyclerAdapter.ClazzEnrolmentWithClazzViewHolder>(
                    DIFFUTIL_CLAZZMEMBERWITHCLAZZ) {

            class ClazzEnrolmentWithClazzViewHolder(val binding: ItemClazzEnrolmentWithClazzDetailBinding)
                    : RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                    : ClazzEnrolmentWithClazzViewHolder {

                return ClazzEnrolmentWithClazzViewHolder(ItemClazzEnrolmentWithClazzDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false).apply {
                    mPresenter = presenter
                })
            }

            override fun onBindViewHolder(holder: ClazzEnrolmentWithClazzViewHolder, position: Int) {
                holder.binding.clazzEnrolmentWithClazz = getItem(position)
            }
        }

    override var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>? = null
        set(value) {
            rolesAndPermissionsLiveData?.removeObserver(rolesAndPermissionsObserver)
            field = value
            val entityRoleDao = dbRepo?.entityRoleDao?: return
            rolesAndPermissionsLiveData = value?.asRepositoryLiveData(entityRoleDao)
            rolesAndPermissionsLiveData?.observe(viewLifecycleOwner, rolesAndPermissionsObserver)
        }

    override var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>? = null
        get() = field
        set(value) {
            clazzesLiveData?.removeObserver(clazzMemberWithClazzObserver)
            field = value
            val clazzMemberDao = dbRepo?.clazzEnrolmentDao ?: return
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

    private var clazzesLiveData: LiveData<PagedList<ClazzEnrolmentWithClazzAndAttendance>>? = null

    private var clazzEnrolmentWithClazzRecyclerAdapter: ClazzEnrolmentWithClazzRecyclerAdapter? = null

    private val clazzMemberWithClazzObserver = Observer<PagedList<ClazzEnrolmentWithClazzAndAttendance>?> {
        t -> clazzEnrolmentWithClazzRecyclerAdapter?.submitList(t)
    }

    private var rolesAndPermissionsLiveData: LiveData<PagedList<EntityRoleWithNameAndRole>>? = null
    private var rolesAndPermissionsRecyclerAdapter: EntityRoleRecyclerAdapter? = null
    private val rolesAndPermissionsObserver = Observer<PagedList<EntityRoleWithNameAndRole>?> {
        t -> rolesAndPermissionsRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        val impl: UstadMobileSystemImpl by instance()
        canManageAccount = impl.getAppConfigBoolean(AppConfig.KEY_ALLOW_ACCOUNT_MANAGEMENT,
                requireContext())
        clazzEnrolmentWithClazzRecyclerAdapter = ClazzEnrolmentWithClazzRecyclerAdapter(
            null)
        rolesAndPermissionsRecyclerAdapter = EntityRoleRecyclerAdapter(false, this)
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.createAccountVisibility = View.GONE
            it.changePasswordVisibility = View.GONE
            it.classesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.classesRecyclerview.adapter = clazzEnrolmentWithClazzRecyclerAdapter
            it.rolesAndPermissionsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.rolesAndPermissionsRecyclerview.adapter = rolesAndPermissionsRecyclerAdapter

        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)
        clazzEnrolmentWithClazzRecyclerAdapter?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        mBinding?.presenter = mPresenter
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.classesRecyclerview?.adapter = null
        clazzEnrolmentWithClazzRecyclerAdapter = null
        mBinding?.rolesAndPermissionsRecyclerview?.adapter = null
        rolesAndPermissionsRecyclerAdapter = null
        dbRepo = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        if(mBinding?.person != null) {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                    mBinding?.person?.firstNames + " " + mBinding?.person?.lastName
        }
    }

    override var entity: PersonWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            if(viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
                (activity as? AppCompatActivity)?.supportActionBar?.title =
                        value?.firstNames + " " + value?.lastName
        }


    companion object {

        val DIFFUTIL_CLAZZMEMBERWITHCLAZZ =
                object: DiffUtil.ItemCallback<ClazzEnrolmentWithClazzAndAttendance>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithClazzAndAttendance,
                                         newItem: ClazzEnrolmentWithClazzAndAttendance): Boolean {
                return oldItem.clazzEnrolmentUid == newItem.clazzEnrolmentUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithClazzAndAttendance,
                                            newItem: ClazzEnrolmentWithClazzAndAttendance): Boolean {
                return oldItem == newItem
            }
        }

        @JvmStatic
        val FOREIGNKEYADAPTER_PERSON = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.personPictureDao.findByPersonUidAsync(foreignKey)?.personPictureUri
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

    override fun handleClickEntityRole(entityRole: EntityRoleWithNameAndRole) {
        //TODO: This
    }

    override fun handleRemoveEntityRole(entityRole: EntityRoleWithNameAndRole) {
        //Not applicable
    }

}