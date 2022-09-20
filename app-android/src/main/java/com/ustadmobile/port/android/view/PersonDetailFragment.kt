package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class PersonDetailFragment: UstadDetailFragment<PersonWithPersonParentJoin>(), PersonDetailView{

    private var mBinding: FragmentPersonDetailBinding? = null

    private var mPresenter: PersonDetailPresenter? = null

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
            mBinding?.changePasswordVisibility = if(value) View.VISIBLE else View.GONE
        }

    override var chatVisibility: Boolean = false
        set(value) {
            field = value
            mBinding?.chatVisibility = if(value) View.VISIBLE else View.GONE
        }

    override var showCreateAccountVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.createAccountVisibility = if(value) View.VISIBLE else View.GONE
        }

    private var clazzesLiveData: LiveData<PagedList<ClazzEnrolmentWithClazzAndAttendance>>? = null

    private var clazzEnrolmentWithClazzRecyclerAdapter: ClazzEnrolmentWithClazzRecyclerAdapter? = null

    private val clazzMemberWithClazzObserver = Observer<PagedList<ClazzEnrolmentWithClazzAndAttendance>?> {
        t -> clazzEnrolmentWithClazzRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View

        clazzEnrolmentWithClazzRecyclerAdapter = ClazzEnrolmentWithClazzRecyclerAdapter(
            null)
        mBinding = FragmentPersonDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.createAccountVisibility = View.GONE
            it.changePasswordVisibility = View.GONE
            it.chatVisibility = View.GONE
            it.classesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            it.classesRecyclerview.adapter = clazzEnrolmentWithClazzRecyclerAdapter
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = PersonDetailPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()
        clazzEnrolmentWithClazzRecyclerAdapter?.presenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        mBinding?.presenter = mPresenter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.classesRecyclerview?.adapter = null
        clazzEnrolmentWithClazzRecyclerAdapter = null
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

    override var entity: PersonWithPersonParentJoin? = null
        get() = field
        set(value) {
            field = value
            mBinding?.person = value
            ustadFragmentTitle = value?.fullName()
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.timeZoneId = "UTC"
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

}