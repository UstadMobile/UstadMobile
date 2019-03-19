package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectClazzFeaturesPresenter;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectClazzFeaturesView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import io.reactivex.annotations.NonNull;


public class SelectClazzFeaturesDialogFragment extends UstadDialogFragment implements
        SelectClazzFeaturesView,
        DismissableDialog {

    AlertDialog dialog;
    View rootView;
    private Clazz updatedClazz;

    private SelectClazzFeaturesPresenter mPresenter;
    //Context (Activity calling this)
    private Context mAttachedContext;
    Toolbar toolbar;
    Switch attendanceSwitch, activitySwitch, selSwitch;



    //Main Activity should implement this ?
    public interface ClazzFeaturesSelectDialogListener{
        void onSelectClazzesFeaturesResult(Clazz clazz);
    }

    public static SelectClazzFeaturesDialogFragment newInstance(){
        SelectClazzFeaturesDialogFragment fragment = new SelectClazzFeaturesDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_clazz_features_dialog, null);

        attendanceSwitch = rootView.findViewById(R.id.fragment_select_clazz_features_dialog_attendance_switch);
        activitySwitch = rootView.findViewById(R.id.fragment_select_clazz_features_dialog_activity_switch);
        selSwitch = rootView.findViewById(R.id.fragment_select_clazz_features_dialog_sel_switch);

        attendanceSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.updateAttendanceFeature(isChecked));
        activitySwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.updateActivityFeature(isChecked));
        selSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.updateSELFeature(isChecked));

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_clazz_features_dialog_toolbar);
        toolbar.setTitle(R.string.features_enabled);

        Drawable upIcon = AppCompatResources.getDrawable(getContext(),
                R.drawable.ic_arrow_back_white_24dp);

        upIcon = getTintedDrawable(upIcon, R.color.icons);


        toolbar.setNavigationIcon(upIcon);
        toolbar.setNavigationOnClickListener(v -> finish());


        mPresenter = new SelectClazzFeaturesPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Dialog stuff:
        //Set any view components and its listener (post presenter work)
        dialog = new android.support.v7.app.AlertDialog.Builder(getContext(),
                R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create();
        return dialog;

    }


    public Drawable getTintedDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        int tintColor = ContextCompat.getColor(getContext(), color);
        DrawableCompat.setTint(drawable, tintColor);
        return drawable;
    }

    @Override
    public void updateFeaturesOnView(Clazz clazz) {
        runOnUiThread(() -> {
            //Set toggle
            attendanceSwitch.setChecked(clazz.isAttendanceFeature());
            activitySwitch.setChecked(clazz.isActivityFeature());
            selSwitch.setChecked(clazz.isSelFeature());
        });

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
        this.updatedClazz = null;
    }

    @Override
    public void finish(){
        updatedClazz = mPresenter.getCurrentClazz();
        if(mAttachedContext instanceof ClazzFeaturesSelectDialogListener){
            ((ClazzFeaturesSelectDialogListener) mAttachedContext).onSelectClazzesFeaturesResult(updatedClazz);
        }
        dialog.dismiss();
    }

}
