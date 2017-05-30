package com.ustadmobile.port.android.view;


import android.app.Dialog;
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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    public WelcomeDialogFragment() {
        // Required empty public constructor
    }


    ArrayList<String> languageList=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_welcome_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        UstadMobileSystemImpl impl= UstadMobileSystemImpl.getInstance();

        ((TextView)view.findViewById(R.id.fragment_welcome_title_text)).setText(
                Html.fromHtml(impl.getString(MessageIDConstants.welcomeTitle)));
        ((TextView)view.findViewById(R.id.text_top_quote)).setText(
                Html.fromHtml(impl.getString(MessageIDConstants.topQuoteMessage)));
        ((TextView)view.findViewById(R.id.download_message_view)).setText(
                Html.fromHtml(impl.getString(MessageIDConstants.downloadTextContent)));
        ((TextView)view.findViewById(R.id.learn_content_view)).setText(
                Html.fromHtml(impl.getString(MessageIDConstants.learnTextContent)));
        ((TextView)view.findViewById(R.id.inperson_lasses_content_view)).setText(
                Html.fromHtml(impl.getString(MessageIDConstants.inpersonClassesTextContent)));
        ((TextView)view.findViewById(R.id.never_show_text_label)).setText(impl.getString(MessageIDConstants.neverShowThisNextTime));
        ((Button)view.findViewById(R.id.accept_button_view)).setText(impl.getString(MessageIDConstants.acceptButton));

        final Spinner languageSpinnerView= (Spinner) view.findViewById(R.id.language_choice_spinner);
        int numLangs = UstadMobileConstants.SUPPORTED_LOCALES.length + 1;
        languageList=new ArrayList<>();
        languageList.add(impl.getString(MessageIDConstants.languageSelectionNote));
        for(int i = 1; i < numLangs; i++) {
            languageList.add(UstadMobileConstants.SUPPORTED_LOCALES[i-1][UstadMobileConstants.LOCALE_NAME]);
        }

        final ArrayAdapter<String> languageAdapter= new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, languageList);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinnerView.setAdapter(languageAdapter);
        languageSpinnerView.setOnItemSelectedListener(this);


        view.findViewById(R.id.language_icon_choice_holder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageSpinnerView.performClick();
            }
        });

        return view;
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
        //TODO: implement the language selection logic to the app (position 0 is for "Select language" placeholder
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
