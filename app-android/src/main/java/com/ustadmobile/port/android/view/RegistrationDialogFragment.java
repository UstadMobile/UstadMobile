package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RegistrationPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.RegistrationView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by varuna on 7/28/2017.
 */

public class RegistrationDialogFragment extends UstadDialogFragment
        implements RegistrationView, DismissableDialog, View.OnClickListener {

    private View mView;

    private RegistrationPresenter mPresenter;

    //private ArrayList<TextInputEditText> fieldList = new ArrayList<>();
    private ArrayList<AutoCompleteTextView> fieldList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.fragment_register_dialog, container, false);

        mView.findViewById(R.id.fragment_register_dialog_register_button).setOnClickListener(this);

        fieldList.add((AutoCompleteTextView) mView.findViewById(
                R.id.fragment_register_dialog_username_text));
        fieldList.add((AutoCompleteTextView) mView.findViewById(
                R.id.fragment_register_dialog_password_text));

        mPresenter = new RegistrationPresenter(getContext(), this);
        if(mResultListener != null)
            mPresenter.setResultListener(mResultListener);

        return mView;
    }

    /**
     * Shows all options
     * @param paramView
     * @param paramMotionEvent
     * @param options
     * @param autoTextField
     * @return
     */
    public boolean showOptions(View paramView, MotionEvent paramMotionEvent,
                               String[] options, AutoCompleteTextView autoTextField ) {
        if (options.length > 0) {
            final ArrayAdapter<String> adapterT = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice, options);
            // show all suggestions
            if (!autoTextField.getText().toString().equals(""))
                adapterT.getFilter().filter(null);
            autoTextField.showDropDown();
        }
        return false;
    }


    @Override
    public void addField(int fieldName, int fieldType, final String[] options) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        LinearLayout fieldLayout = (LinearLayout)mView.findViewById(
                R.id.fragment_register_dialog_field_layout);

        TextInputLayout textInputLayout = new TextInputLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);


        final AutoCompleteTextView autoTextField = new AutoCompleteTextView(getContext());
        ArrayAdapter<String> adapter;
        if(options != null) {
            adapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.select_dialog_singlechoice, options);
            autoTextField.setThreshold(1);
            autoTextField.setAdapter(adapter);
        }

        autoTextField.setId(fieldName);
        autoTextField.setHint(UstadMobileSystemImpl.getInstance().getString(fieldName,
                getContext()));
        autoTextField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autoTextField.setSingleLine();
        autoTextField.setMinLines(1);
        autoTextField.setMaxLines(1);

        switch(fieldType){
            case RegistrationPresenter.TYPE_AUTOCOMPETE_TEXT_VIEW:
                autoTextField.setInputType(InputType.TYPE_NULL);
                autoTextField.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                        return showOptions(paramView, paramMotionEvent, options, autoTextField);
                    }

                    public boolean showMe(View paramView, MotionEvent paramMotionEvent) {
                        return showOptions(paramView, paramMotionEvent, options, autoTextField);
                    }
                });

                autoTextField.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOptions(view, null, options, autoTextField);
                    }
                });

                autoTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (options.length > 0) {
                            final ArrayAdapter<String> adapterT = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.select_dialog_singlechoice, options);
                            // show all suggestions
                            if (!autoTextField.getText().toString().equals(""))
                                adapterT.getFilter().filter(null);
                            autoTextField.showDropDown();
                        }
                    }
                });
                break;
            case RegistrationPresenter.TYPE_SPINNER:
                ArrayAdapter<String> adapterS =
                        new ArrayAdapter<>(getActivity(),
                                android.R.layout.select_dialog_singlechoice, options);
                Spinner dropDownSpinner = new Spinner(getContext());
                dropDownSpinner.setAdapter(adapterS);
                dropDownSpinner.setId(fieldName);
                textInputLayout.addView(dropDownSpinner);
                fieldLayout.addView(textInputLayout, params);
                AutoCompleteTextView prevElS =  fieldList.get(fieldList.size()-1);
                prevElS.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                //fieldList.add(dropDownSpinner);
                break;
            case RegistrationPresenter.TYPE_CLASS_DATETIME:
                autoTextField.setInputType(InputType.TYPE_CLASS_DATETIME);
                break;
            case RegistrationPresenter.TYPE_CLASS_NUMBER:
                autoTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case RegistrationPresenter.TYPE_CLASS_PHONE:
                autoTextField.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case RegistrationPresenter.TYPE_CLASS_TEXT:
                autoTextField.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case RegistrationPresenter.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                autoTextField.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                break;
            default:
                break;
        }


        textInputLayout.addView(autoTextField);
        fieldLayout.addView(textInputLayout, params);
        AutoCompleteTextView prevElN =  fieldList.get(fieldList.size()-1);
        prevElN.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        fieldList.add(autoTextField);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.fragment_register_dialog_register_button:
                boolean allgood = true;
                String username = ((AutoCompleteTextView)mView.findViewById(
                        R.id.fragment_register_dialog_username_text)).getText().toString();
                String password = ((AutoCompleteTextView) mView.findViewById(
                        R.id.fragment_register_dialog_password_text)).getText().toString();

                for(AutoCompleteTextView field: fieldList){
                    if(field.getText().toString().trim().equals("")){
                        field.setError("This field is Required");
                        allgood = false;
                    }
                }

                Hashtable fieldMap = new Hashtable();
                Set<Integer> extraFieldsSet = mPresenter.extraFieldsMap.keySet();
                Integer[] allFields =
                        extraFieldsSet.toArray(new Integer[extraFieldsSet.size()]);

                for(int field:allFields){
                    String value = ((EditText)mView.findViewById(field)).getText().toString();
                    fieldMap.put(field, value);
                }

                //register new user if validation all good
                if(allgood) {
                    mPresenter.handleClickRegister(username, password, fieldMap);
                }
        }
    }

    private boolean validateFields(AutoCompleteTextView[] fields){
        for(int i=0; i<fields.length; i++){
            EditText currentField=fields[i];
            if(currentField.getText().toString().length()<=0){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(mPresenter != null)
            mPresenter.setResultListener(mResultListener);
    }
}
