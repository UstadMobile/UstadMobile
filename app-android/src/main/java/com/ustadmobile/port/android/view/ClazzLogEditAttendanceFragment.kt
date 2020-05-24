package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogEditAttendanceBinding
import com.ustadmobile.core.controller.ClazzLogEditAttendancePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

import com.ustadmobile.port.android.view.ext.setEditFragmentTitle

interface ClazzLogEditAttendanceFragmentEventHandler {

}

class ClazzLogEditAttendanceFragment: UstadEditFragment<ClazzLog>(), ClazzLogEditAttendanceView, ClazzLogEditAttendanceFragmentEventHandler {

    private var mBinding: FragmentClazzLogEditAttendanceBinding? = null

    private var mPresenter: ClazzLogEditAttendancePresenter? = null


//    class ClazzLogAttendanceRecordRecyclerAdapter(val activityEventHandler: ClazzLogEditAttendanceFragmentEventHandler,
//            var presenter: ClazzLogEditAttendancePresenter?): ListAdapter<ClazzLogAttendanceRecordWithPerson, ClazzLogAttendanceRecordRecyclerAdapter.ClazzLogAttendanceRecordViewHolder>(DIFF_CALLBACK_CLAZZLOGATTENDANCERECORD) {
//
//            class ClazzLogAttendanceRecordViewHolder(val binding: ItemClazzLogAttendanceRecordBinding): RecyclerView.ViewHolder(binding.root)
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogAttendanceRecordViewHolder {
//                val viewHolder = ClazzLogAttendanceRecordViewHolder(ItemClazzLogAttendanceRecordBinding.inflate(
//                        LayoutInflater.from(parent.context), parent, false))
//                viewHolder.binding.mPresenter = presenter
//                viewHolder.binding.mActivity = activityEventHandler
//                return viewHolder
//            }
//
//            override fun onBindViewHolder(holder: ClazzLogAttendanceRecordViewHolder, position: Int) {
//                holder.binding.clazzLogAttendanceRecord = getItem(position)
//            }
//        }

    override var clazzLogAttendanceRecordList: DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>? = null
        get() = field
        set(value) {
            //field?.removeObserver(clazzLogAttendanceRecordObserver)
            field = value
            //value?.observe(this, clazzLogAttendanceRecordObserver)
        }

//    private var clazzLogAttendanceRecordRecyclerAdapter: ClazzLogAttendanceRecordRecyclerAdapter? = null

    private var clazzLogAttendanceRecordRecyclerView: RecyclerView? = null

//    private val clazzLogAttendanceRecordObserver = Observer<List<ClazzLogAttendanceRecordWithPerson>?> {
//        t -> clazzLogAttendanceRecordRecyclerAdapter?.submitList(t)
//    }
//
//    override fun onClickEditClazzLogAttendanceRecord(clazzLogAttendanceRecord: ClazzLogAttendanceRecordWithPerson?) {
//
//    }
//
//    override fun onClickNewClazzLogAttendanceRecord() = onClickEditClazzLogAttendanceRecord(null)

    /*
    TODO 1: Put these method signatures into the ClazzLogEditAttendanceFragmentEventHandler interface (at the top)
    fun onClickEditClazzLogAttendanceRecord(clazzLogAttendanceRecord: ClazzLogAttendanceRecord?)
    fun onClickNewClazzLogAttendanceRecord()
    */

    /*
    TODO 2: put this into onCreate:
    clazzLogAttendanceRecordRecyclerView = findViewById(R.id.activity_ClazzLogAttendanceRecord_recycleradapter
    clazzLogAttendanceRecordRecyclerAdapter = ClazzLogAttendanceRecordRecyclerAdapter(this, null)
    clazzLogAttendanceRecordRecyclerView?.adapter = clazzLogAttendanceRecordRecyclerAdapter
    clazzLogAttendanceRecordRecyclerView?.layoutManager = LinearLayoutManager(this)

    //After the presenter is created
    clazzLogAttendanceRecordRecyclerAdapter?.presenter = mPresenter
    */

    /*
    TODO 3
    Make a layout for the item in the recyclerview named item_ClazzLogAttendanceRecord (PS: convert ClazzLogAttendanceRecord to snake case).
    Use the Ustad Edit Screen 1-N ListItem XML (right click on res/layout, click new, and select the template)
*/

    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzLogEditAttendanceBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzLogEditAttendancePresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        //setEditFragmentTitle(R.string.clazzlog)
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzLog = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}