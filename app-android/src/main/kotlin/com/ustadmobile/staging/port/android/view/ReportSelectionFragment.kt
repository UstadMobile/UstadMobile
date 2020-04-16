package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.BaseReportPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.BaseReportView
import com.ustadmobile.port.android.view.UstadBaseFragment
import java.util.*

class ReportSelectionFragment : UstadBaseFragment, BaseReportView {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private var mPresenter: BaseReportPresenter? = null

    internal lateinit var expandableListView: ExpandableListView
    internal lateinit var expandableListAdapter: ExpandableListAdapter
    internal lateinit var expandableListTitle: List<String>

    internal lateinit var expandableListDataReportsHashMap: HashMap<String, ExpandableListDataReports>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.activity_report_selection, container, false)
        setHasOptionsMenu(true)


        //Call the Presenter
        mPresenter = BaseReportPresenter(context!!,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        expandableListView = rootContainer.findViewById(R.id.activity_report_selection_expandable_report_list)

        //new:
        expandableListDataReportsHashMap = ExpandableListDataReports.getDataAll(context!!)
        expandableListTitle = ArrayList(expandableListDataReportsHashMap.keys)

        expandableListAdapter = CustomExpandableListAdapter(context!!,
                expandableListDataReportsHashMap, expandableListTitle)

        expandableListView.setAdapter(expandableListAdapter)
        expandableListView.setOnGroupExpandListener { groupPosition -> }

        expandableListView.setOnGroupCollapseListener { groupPosition -> }

        //If Groups have no children, go to their link (default: expand)
        expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->

            val groupItem = expandableListDataReportsHashMap[expandableListTitle[groupPosition]]!!
            if (groupItem.children.size === 0 && !groupItem.reportLink!!.isEmpty()) {

                mPresenter!!.goToReport(groupItem.name!!, groupItem.desc!!, groupItem.reportLink!!,
                        groupItem.showThreshold!!, groupItem.showRadioGroup!!,
                        groupItem.showGenderDisaggregate!!, groupItem.showClazzes!!,
                        groupItem.showLocations!!)
            }

            false
        }

        //Go to child's link
        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->


            val report = expandableListDataReportsHashMap[expandableListTitle[groupPosition]]!!.children
                    .get(childPosition)

            mPresenter!!.goToReport(report.name!!, report.desc!!, report.reportLink!!, report.showThreshold!!,
                    report.showRadioGroup!!, report.showGenderDisaggregate!!, report.showClazzes!!,
                    report.showLocations!!)
            false
        }

        return rootContainer

    }

    override fun finish() {

    }

    constructor()  {
        val args = Bundle()
        arguments = args
    }

    constructor(args:Bundle) : this() {
        arguments = args
    }

    companion object {

        val icon = R.drawable.ic_insert_chart_black_24dp
        val title = R.string.bottomnav_reports_title

        fun newInstance(): ReportSelectionFragment {
            val fragment = ReportSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
