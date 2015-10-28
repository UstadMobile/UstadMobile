package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.toughra.ustadmobile.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AttendanceSelectClassListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AttendanceSelectClassListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceSelectClassListFragment extends Fragment implements AdapterView.OnItemClickListener{

    private static final String ARG_CLASSLIST= "classlist";

    private String[] mClassList;

    private ListView mList;

    private ArrayAdapter<String> mListAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param classList List of classes to choose from
     * @return A new instance of fragment AttendanceSelectClassListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceSelectClassListFragment newInstance(String[] classList) {
        AttendanceSelectClassListFragment fragment = new AttendanceSelectClassListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLASSLIST, classList);
        fragment.setArguments(args);
        return fragment;
    }

    public AttendanceSelectClassListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClassList = (String[])getArguments().getSerializable(ARG_CLASSLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_attendance_select_class_list, container, false);
        mList = (ListView)rootView.findViewById(R.id.attendance_selectclass_list);
        mListAdapter = new ArrayAdapter<>(getContext(), R.layout.item_attendanceclass,
                R.id.attendanceclass_name, mClassList);
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(this);
        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int selectedIndex, long id) {
        ((AttendanceActivity)getActivity()).mController.handleClassSelected(selectedIndex);
    }
}
