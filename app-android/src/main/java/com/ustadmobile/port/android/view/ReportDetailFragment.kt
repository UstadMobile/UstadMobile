package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportDetailBinding
import com.toughra.ustadmobile.databinding.ItemReportListBinding
import com.ustadmobile.core.controller.ReportDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.port.android.view.ext.navigateToEditEntity


interface ReportDetailFragmentEventHandler {
    fun handleEditClick()
}

class ReportDetailFragment : UstadDetailFragment<ReportWithFilters>(), ReportDetailView, ReportDetailFragmentEventHandler {

    private var mBinding: FragmentReportDetailBinding? = null

    private var mPresenter: ReportDetailPresenter? = null

    private var chartAdapter: RecyclerViewChartAdapter? = null

    private var statementAdapter: RecyclerViewStatmentAdapter? = null

    private var mergeAdapter: MergeAdapter? = null

    class ChartViewHolder(val itemBinding: ItemReportListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class StatementViewHolder(val itemBinding: ItemReportListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewChartAdapter : RecyclerView.Adapter<ChartViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getItemCount(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class RecyclerViewStatmentAdapter : RecyclerView.Adapter<StatementViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatementViewHolder {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getItemCount(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBindViewHolder(holder: StatementViewHolder, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentReportDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ReportDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        chartAdapter = RecyclerViewChartAdapter()
        statementAdapter = RecyclerViewStatmentAdapter()
        mergeAdapter = MergeAdapter(chartAdapter, statementAdapter)
        mBinding?.fragmentDetailReportList?.adapter = mergeAdapter

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        chartAdapter = null
        mergeAdapter = null
        statementAdapter = null
    }

    override fun onResume() {
        super.onResume()
        //TODO: Set title here
    }

    override var entity: ReportWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }
    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun handleEditClick() {
        navigateToEditEntity(entity, R.id.report_edit_dest, Report::class.java, overwriteDestination = true)
    }

}