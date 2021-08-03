package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.toughra.ustadmobile.databinding.FragmentDateRangeBinding
import com.ustadmobile.core.controller.DateRangePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DateRangeView
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY


interface DateRangeFragmentEventHandler {
    fun onClickRadioSelected(view: View)
}

class DateRangeFragment: UstadEditFragment<DateRangeMoment>(), DateRangeView,
        DateRangeFragmentEventHandler {

    private var mBinding: FragmentDateRangeBinding? = null

    private var mPresenter: DateRangePresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DateRangeMoment>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentDateRangeBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
        }

        mPresenter = DateRangePresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPresenter?.onCreate(backStackSavedState)
    }

    override var entity: DateRangeMoment? = null
        get() = field
        set(value) {
            field = value
            mBinding?.dateRangeMoment = value
            mBinding?.dateTimeMode = MODE_START_OF_DAY
            mBinding?.timeZoneId = "UTC"
        }

    override var relUnitOptions: List<DateRangePresenter.RelUnitMessageIdOption>? = null
        get() = field
        set(value){
            field = value
            mBinding?.relUnitOption = value
        }

    override var relToOptions: List<DateRangePresenter.RelToMessageIdOption>? =null
        get() = field
        set(value){
            field = value
            mBinding?.relToOptions = value
        }
    override var fromFixedDateMissing: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.fromFixedDateMissing = value
        }

    override var toFixedDateMissing: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.toFixedDateMissing = value
        }
    override var toRelativeDateInvalid: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.toRelativeDateInvalid = value
        }
    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onClickRadioSelected(view: View) {
        if(view is RadioButton){

            if(view == mBinding?.dateRangeFromRadioFixedDate && view.isChecked){
                mBinding?.dateRangeMoment?.fromMoment?.typeFlag = Moment.TYPE_FLAG_FIXED
            }else if(view == mBinding?.dateRangeFromRadioRelativeDate && view.isChecked){
                mBinding?.dateRangeMoment?.fromMoment?.typeFlag = Moment.TYPE_FLAG_RELATIVE
            }else if(view == mBinding?.dateRangeToRadioFixedDate && view.isChecked){
                mBinding?.dateRangeMoment?.toMoment?.typeFlag = Moment.TYPE_FLAG_FIXED
            }else if(view == mBinding?.dateRangeToRadioRelativeDate && view.isChecked){
                mBinding?.dateRangeMoment?.toMoment?.typeFlag = Moment.TYPE_FLAG_RELATIVE
            }
            entity = mBinding?.dateRangeMoment
        }
    }


}