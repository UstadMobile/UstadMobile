package com.ustadmobile.port.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.sharedse.model.AttendanceClassStudent;

/**
 * Created by varuna on 21/02/16.
 */
public class StudentListView extends ListView {

    private ArrayAdapter<AttendanceClassStudent> mListAdapter;

    public StudentListView(Context ctx) {
        super(ctx);
    }

    public StudentListView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public StudentListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStudents(AttendanceClassStudent[] students) {
        mListAdapter = new ArrayAdapter<AttendanceClassStudent>(getContext(),
                R.layout.item_classmanagement_student, R.id.classmanagement_student_name,
                students);
        setAdapter(mListAdapter);
    }

}
