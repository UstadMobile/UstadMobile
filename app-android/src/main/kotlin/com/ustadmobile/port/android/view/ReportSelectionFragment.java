package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.BaseReportPresenter;
import com.ustadmobile.core.view.BaseReportView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportSelectionFragment extends UstadBaseFragment implements BaseReportView {

    View rootContainer;
    private BaseReportPresenter mPresenter;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;

    HashMap<String, ExpandableListDataReports> expandableListDataReportsHashMap;


    public static ReportSelectionFragment newInstance(){
        ReportSelectionFragment fragment = new ReportSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootContainer = inflater.inflate(R.layout.activity_report_selection, container, false);
        setHasOptionsMenu(true);


        //Call the Presenter
        mPresenter = new BaseReportPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        expandableListView = rootContainer.findViewById(R.id.activity_report_selection_expandable_report_list);

        //new:
        expandableListDataReportsHashMap = ExpandableListDataReports.getDataAll(getContext());
        expandableListTitle = new ArrayList<>(expandableListDataReportsHashMap.keySet());

        expandableListAdapter = new CustomExpandableListAdapter(getContext(),
                expandableListDataReportsHashMap,  expandableListTitle);

        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnGroupExpandListener(groupPosition -> {});

        expandableListView.setOnGroupCollapseListener(groupPosition -> {});

        //If Groups have no children, go to their link (default: expand)
        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {

            ExpandableListDataReports groupItem = expandableListDataReportsHashMap
                    .get(expandableListTitle.get(groupPosition));
            assert groupItem != null;
            if(groupItem.children.size() == 0 && !groupItem.reportLink.isEmpty()){

                mPresenter.goToReport(groupItem.name, groupItem.desc, groupItem.reportLink,
                        groupItem.showThreshold, groupItem.showRadioGroup,
                        groupItem.showGenderDisaggregate, groupItem.showClazzes,
                        groupItem.showLocations);
            }

            return false;
        });

        //Go to child's link
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {


            ExpandableListDataReports report = Objects.requireNonNull(expandableListDataReportsHashMap
                    .get(expandableListTitle.get(groupPosition))).children
                    .get(childPosition);

            mPresenter.goToReport(report.name,report.desc, report.reportLink, report.showThreshold,
                    report.showRadioGroup, report.showGenderDisaggregate, report.showClazzes,
                    report.showLocations);
            return false;
        });

        return rootContainer;

    }

    @Override
    public void finish() {

    }
}
