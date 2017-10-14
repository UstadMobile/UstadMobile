package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.sharedse.model.AttendanceRowModel;

/**
 *
 */
public class AttendanceConfirmFragment extends Fragment implements View.OnClickListener{

    private ListView mList;

    private AttendanceArrayAdapter<AttendanceRowModel> mListAdapter;

    public static final int[] STATUS_DRAWABLE_IDS = new int[] {
            R.drawable.check,R.drawable.late, R.drawable.excused, R.drawable.cross
    };

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AttendanceConfirmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AttendanceConfirmFragment newInstance() {
        AttendanceConfirmFragment fragment = new AttendanceConfirmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AttendanceConfirmFragment() {
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
        final View rootView = inflater.inflate(R.layout.fragment_attendance_confirm, container, false);
        mList = (ListView)rootView.findViewById(R.id.attendance_confirm_list);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                ImageView iconImageView = (ImageView) view.findViewById(R.id.attendance_item_icon);
                int currentStatus =
                        ((AttendanceActivity)getActivity()).mController.attendanceResult[(int)id].attendanceStatus;
                //currentStatus values and meaning
                //0: Attended
                //1: Late
                //2: Absent - Excused
                //3: Absent
                //-1: Nothing set

                if (currentStatus == 3 ){
                    currentStatus = 0;
                    iconImageView.setImageResource(R.drawable.check);
                }else if (currentStatus == 0 ){
                    currentStatus = 3;
                    iconImageView.setImageResource(R.drawable.cross);
                }else{
                    currentStatus = 0;
                    iconImageView.setImageResource(R.drawable.check);
                }

                ((AttendanceActivity)getActivity()).mController.attendanceResult[(int)id].attendanceStatus = currentStatus;

                int newStatus =
                        ((AttendanceActivity)getActivity()).mController.attendanceResult[(int)id].attendanceStatus;
            }
        });
        mListAdapter = new AttendanceArrayAdapter<>(getContext(), 0,
                ((AttendanceActivity)getActivity()).getAttendanceResults());
        mList.setAdapter(mListAdapter);
        ((Button)rootView.findViewById(R.id.attendance_confirm_button)).setOnClickListener(this);
        ((Button)rootView.findViewById(R.id.attendance_tryagain_button)).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.attendance_confirm_button) {
            ((AttendanceActivity)getActivity()).mController.handleClickSubmitResults();
        }else if(view.getId() == R.id.attendance_tryagain_button){
            ((AttendanceActivity)getActivity()).mController.handleGoBack();
        }
    }

    class AttendanceArrayAdapter<T> extends ArrayAdapter {
        AttendanceArrayAdapter(Context context, int resource, T[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            AttendanceRowModel item = (AttendanceRowModel)getItem(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_attendancerow, null);
            }

            String rowText = (item.rollNum + 1) + ". " + item.full_name;
            ((TextView)convertView.findViewById(R.id.attendance_person_name)).setText(rowText);

            ImageView imgView =(ImageView) convertView.findViewById(R.id.attendance_item_icon);
            if(item.attendanceStatus >= 0) {
                imgView.setImageDrawable(getResources().getDrawable(STATUS_DRAWABLE_IDS[item.attendanceStatus]));
            }else {
                imgView.setImageResource(android.R.color.transparent);
            }

            return convertView;
        }

    }


}
