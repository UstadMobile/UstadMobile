package com.ustadmobile.port.android.view;


import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.WelcomeController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.WelcomeView;
import com.ustadmobile.port.android.impl.UMLogAndroid;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeDialogFragment extends UstadDialogFragment implements AdapterView.OnItemSelectedListener, WelcomeView, View.OnClickListener, CheckBox.OnCheckedChangeListener {

    private WelcomeController mController;

    private CheckBox mDontShowAgainCheckbox;

    protected View mView;

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
        int numLangs = CoreBuildConfig.SUPPORTED_LOCALES.length;
        languageList=new ArrayList<>();
        languageList.add(impl.getString(MessageID.device_language, getContext()));
        String userLocale = impl.getLocale(getContext());
        String localeName;
        int selectedLocale = 0;
        for(int i = 0; i < numLangs; i++) {
            localeName = (String)UstadMobileConstants.LANGUAGE_NAMES.get(
                    CoreBuildConfig.SUPPORTED_LOCALES[i]);
            if(userLocale.equals(CoreBuildConfig.SUPPORTED_LOCALES[i]))
                selectedLocale = i + 1;

            languageList.add(localeName);
        }

        final ArrayAdapter<String> languageAdapter= new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, languageList);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinnerView.setAdapter(languageAdapter);
        languageSpinnerView.setSelection(selectedLocale, false);
        Log.i(UMLogAndroid.LOGTAG, "Selected lang: " + selectedLocale);
        languageSpinnerView.setOnItemSelectedListener(this);


        mView.findViewById(R.id.language_icon_choice_holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageSpinnerView.performClick();
            }
        });
        mDontShowAgainCheckbox = (CheckBox)mView.findViewById(
                R.id.fragment_welcome_dont_show_next_time_checkbox);

        String dontShowVal =UstadMobileSystemImpl.getInstance().getAppPref(
                WelcomeController.PREF_KEY_WELCOME_DONT_SHOW_TRANSIENT, getContext());

        if(dontShowVal != null && !dontShowVal.equals("false"))
            mDontShowAgainCheckbox.setChecked(true);

        mDontShowAgainCheckbox.setOnCheckedChangeListener(this);

        mView.findViewById(R.id.welcome_dialog_got_it_button).setOnClickListener(this);

        mController = new WelcomeController(getContext(), this);
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
        if(Build.VERSION.SDK_INT >= 17) {
            String chosenLocale = position == 0 ? UstadMobileSystemImpl.LOCALE_USE_SYSTEM
                    : CoreBuildConfig.SUPPORTED_LOCALES[position - 1];
            if (!chosenLocale.equals(UstadMobileSystemImpl.getInstance().getLocale(getContext()))
                    && getActivity() != null && getActivity() instanceof BasePointActivity) {
                UstadMobileSystemImpl.getInstance().setLocale(chosenLocale, getContext());
                BasePointActivity activity = (BasePointActivity) getActivity();
                activity.setWelcomeScreenDisplayed(false);
                activity.recreate();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.welcome_dialog_got_it_button) {
            UstadMobileSystemImpl.getInstance().setAppPref(
                    WelcomeController.PREF_KEY_WELCOME_DONT_SHOW_TRANSIENT,
                    null, getContext());
            mController.setHideWelcomeNextTime(mDontShowAgainCheckbox.isChecked());
            mController.handleClickOK();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        UstadMobileSystemImpl.getInstance().setAppPref(
                WelcomeController.PREF_KEY_WELCOME_DONT_SHOW_TRANSIENT,
                String.valueOf(isChecked), getContext());
    }

    @Override
    public void setDontShowAgainChecked(boolean checked) {
        mDontShowAgainCheckbox.setChecked(checked);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
