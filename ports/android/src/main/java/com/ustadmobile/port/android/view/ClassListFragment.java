package com.ustadmobile.port.android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ustadmobile.core.view.ClassListView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import com.ustadmobile.core.controller.ClassListController;
import android.widget.AdapterView;

import com.toughra.ustadmobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClassListFragment extends UstadBaseFragment implements ClassListView, AdapterView.OnItemClickListener{


    private String[] mClassList;

    private ListView mListView;

    private ArrayAdapter<String> mListAdapter;

    private ClassListController mController;

    public ClassListFragment() {
        // Required empty public constructor
    }

    public static ClassListFragment newInstance(Bundle args) {
        ClassListFragment fragment = new ClassListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = ClassListController.makeControllerForView(this);
        setBaseController(mController);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_class_list, container,
                false);
        mListView = (ListView)rootView.findViewById(R.id.fragment_class_list_listview);
        mListAdapter = new ArrayAdapter<>(getContext(), R.layout.item_attendanceclass,
                R.id.attendanceclass_name, mClassList);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);
        return rootView;
    }


    @Override
    public void setClassList(String[] classList) {
        this.mClassList = classList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.handleClassSelected(position);
    }

}
