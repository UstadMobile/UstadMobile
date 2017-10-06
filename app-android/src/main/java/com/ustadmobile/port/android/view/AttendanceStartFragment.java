package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.toughra.ustadmobile.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AttendanceStartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AttendanceStartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AttendanceStartFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AttendanceStartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceStartFragment newInstance() {
        AttendanceStartFragment fragment = new AttendanceStartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AttendanceStartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  (ViewGroup)inflater.inflate(R.layout.fragment_attendance_start, container,
                false);
        ((Button)rootView.findViewById(R.id.attendance_snap_button)).setOnClickListener(this);
        return rootView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onClick(View view) {
        ((AttendanceActivity)getActivity()).mController.handleClickSnap();
    }
}
