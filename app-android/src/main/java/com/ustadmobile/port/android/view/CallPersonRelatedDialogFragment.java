package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter;
import com.ustadmobile.core.view.CallPersonRelatedDialogView;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

import static com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.NUMBER_FATHER;
import static com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.NUMBER_MOTHER;
import static com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.NUMBER_RETENTION_OFFICER;
import static com.ustadmobile.core.controller.CallPersonRelatedDialogPresenter.NUMBER_TEACHR;

public class CallPersonRelatedDialogFragment extends UstadDialogFragment implements
        CallPersonRelatedDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {

    View rootView;
    AlertDialog dialog;

    TextView fatherEntry, motherEntry, teacherEntry;
    CallPersonRelatedDialogPresenter mPresenter;

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;


        rootView = inflater.inflate(R.layout.fragment_call_person_related_dialog, null);

        fatherEntry =
                rootView.findViewById(R.id.fragment_call_person_related_dialog_father);
        motherEntry =
                rootView.findViewById(R.id.fragment_call_person_related_dialog_mother);
        teacherEntry =
                rootView.findViewById(R.id.fragment_call_person_related_dialog_teacher);

        //Presenter stuff
        mPresenter = new CallPersonRelatedDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getArguments()));

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle("");
        builder.setView(rootView);
        dialog = builder.create();
        dialog.setOnShowListener(this);



        return dialog;
    }

    @Override
    public void setOnDisplay(LinkedHashMap<Integer, CallPersonRelatedDialogPresenter.NameWithNumber> numbers) {
        Iterator<Map.Entry<Integer, CallPersonRelatedDialogPresenter.NameWithNumber>> iterator =
                numbers.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Integer, CallPersonRelatedDialogPresenter.NameWithNumber> next = iterator.next();
            Integer key = next.getKey();
            CallPersonRelatedDialogPresenter.NameWithNumber nameWithNumber = next.getValue();
            runOnUiThread(() -> {
                switch(key){

                    case NUMBER_FATHER:
                        fatherEntry.setText(nameWithNumber.name);
                        fatherEntry.setOnClickListener(v -> handleClickCall(nameWithNumber.number));
                        break;
                    case NUMBER_MOTHER:
                        motherEntry.setText(nameWithNumber.name);
                        motherEntry.setOnClickListener(v -> handleClickCall(nameWithNumber.number));
                        break;
                    case NUMBER_TEACHR:
                        teacherEntry.setText(nameWithNumber.name);
                        teacherEntry.setOnClickListener(v -> handleClickCall(nameWithNumber.number));
                        break;
                    case NUMBER_RETENTION_OFFICER:
                        //TODO: Retention Offiver
                        break;
                    default:
                        break;

                }
            });

        }
    }

    @Override
    public void handleClickCall(String number) {
        startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + number)));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void finish() {

    }
}
