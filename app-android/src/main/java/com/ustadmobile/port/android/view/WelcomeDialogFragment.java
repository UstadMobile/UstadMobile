package com.ustadmobile.port.android.view;


import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.WelcomeController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.WelcomeView;
import com.ustadmobile.port.android.impl.UMLogAndroid;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeDialogFragment extends UstadDialogFragment implements AdapterView.OnItemSelectedListener, WelcomeView, View.OnClickListener, CheckBox.OnCheckedChangeListener {

    private WelcomeController mController;

    private CheckBox mDontShowAgainCheckbox;

    protected View mView;

    boolean disableRecreate = false;

    public WelcomeDialogFragment() {
        // Required empty public constructor
    }


    ArrayList<String> languageList=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_welcome_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        UstadMobileSystemImpl impl= UstadMobileSystemImpl.getInstance();

        ((TextView)mView.findViewById(R.id.download_message_view)).setText(
            Html.fromHtml(getResources().getString(R.string.welcome_dialog_box1)));
        ((TextView)mView.findViewById(R.id.learn_content_view)).setText(
                Html.fromHtml(getResources().getString(R.string.welcome_dialog_box2)));
        ((TextView)mView.findViewById(R.id.inperson_lasses_content_view)).setText(
                Html.fromHtml(getResources().getString(R.string.welcome_dialog_box3)));

        String translatedTitle = UstadMobileSystemImpl.getInstance().getString(MessageID.welcome,
                getContext());
        Log.i(UMLogAndroid.LOGTAG, "translated title = " + translatedTitle);

        final Spinner languageSpinnerView= (Spinner) mView.findViewById(R.id.language_choice_spinner);
        int numLangs = UstadMobileConstants.SUPPORTED_LOCALES.length + 1;
        languageList=new ArrayList<>();
        languageList.add(getResources().getString(R.string.select_language));
        for(int i = 1; i < numLangs; i++) {
            languageList.add(UstadMobileConstants.SUPPORTED_LOCALES[i-1][UstadMobileConstants.LOCALE_NAME]);
        }

        final ArrayAdapter<String> languageAdapter= new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, languageList);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinnerView.setAdapter(languageAdapter);
        languageSpinnerView.setOnItemSelectedListener(this);


        mView.findViewById(R.id.language_icon_choice_holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageSpinnerView.performClick();
            }
        });
        mDontShowAgainCheckbox = (CheckBox)mView.findViewById(
                R.id.fragment_welcome_dont_show_next_time_checkbox);
        mDontShowAgainCheckbox.setOnCheckedChangeListener(this);

        mView.findViewById(R.id.welcome_dialog_got_it_button).setOnClickListener(this);

        mController = new WelcomeController(getContext(), this);
        if(savedInstanceState != null)
            disableRecreate = true;

        return mView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            return;
        }


        if(Build.VERSION.SDK_INT >= 17 && !disableRecreate) {

//            String chosenLocale = UstadMobileConstants.SUPPORTED_LOCALES[position][UstadMobileConstants.LOCALE_CODE];
//            Configuration conf = getContext().getResources().getConfiguration();
//            conf.setLocale(new Locale(chosenLocale));
//            Context localizedContext = getContext().createConfigurationContext(conf);
//            Resources localizedResources = localizedContext.getResources();
//            int[][] resIds = new int[][] {
//                    {R.id.fragment_welcome_title_text, R.string.welcome}
//            };
//
//            for(int i = 0; i < resIds.length; i++) {
//                TextView tv = (TextView)mView.findViewById(resIds[i][0]);
//                tv.setText(localizedResources.getText(resIds[i][1]));
//            }
            String chosenLocale = UstadMobileConstants.SUPPORTED_LOCALES[position-1][UstadMobileConstants.LOCALE_CODE];
            UstadMobileSystemImpl.getInstance().setLocale(chosenLocale, getContext());
            UstadBaseActivity activity = (UstadBaseActivity)getActivity();
            activity.recreate();
        }else {
            disableRecreate = false;
        }


        //TODO: implement the language selection logic to the app (position 0 is for "Select language" placeholder
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.welcome_dialog_got_it_button:
                dismiss();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(mController != null)
            mController.handleClickHideWelcomeNextTime(isChecked);
    }

    @Override
    public void setDontShowAgainChecked(boolean checked) {
        mDontShowAgainCheckbox.setChecked(checked);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
