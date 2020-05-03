package com.ustadmobile.staging.port.android.view


import android.os.Bundle
import android.view.MenuItem
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ReportSelectionPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ReportSelectionView
import com.ustadmobile.port.android.view.UstadBaseActivity


/**
 * The ReportSelection activity.
 *
 *
 * This Activity extends UstadBaseActivity and implements ReportSelectionView
 */
class ReportSelectionActivity : UstadBaseActivity(), ReportSelectionView {

    private var mPresenter: ReportSelectionPresenter? = null

    internal lateinit var expandableListView: ExpandableListView
    internal lateinit var expandableListAdapter: ExpandableListAdapter
    internal lateinit var expandableListTitle: List<String>

    internal lateinit var expandableListDataReportsHashMap: HashMap<String, ExpandableListDataReports>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_report_selection)

        //Call the Presenter
        mPresenter = ReportSelectionPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        expandableListView = findViewById(R.id.activity_report_selection_expandable_report_list)

        //new:
        expandableListDataReportsHashMap = ExpandableListDataReports.getDataAll(applicationContext)
        expandableListTitle = ArrayList(expandableListDataReportsHashMap.keys)

        expandableListAdapter = CustomExpandableListAdapter(this,
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

            mPresenter!!.goToReport(report.name!!, report.desc!!, report.reportLink!!,
                    report.showThreshold!!, report.showRadioGroup!!, report.showGenderDisaggregate!!,
                    report.showClazzes!!, report.showLocations!!)
            false
        }

    }

    /**
     * Handles what happens when toolbar menu option selected. Here it is handling what happens when
     * back button is pressed.
     *
     * @param item  The item selected.
     * @return      true if accounted for.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
