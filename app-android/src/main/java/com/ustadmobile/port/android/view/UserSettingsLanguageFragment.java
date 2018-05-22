package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.toughra.ustadmobile.R;

/**
 */
public class UserSettingsLanguageFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayAdapter<String> mLanguageListAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserSettingsLanguageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserSettingsLanguageFragment newInstance() {
        UserSettingsLanguageFragment fragment = new UserSettingsLanguageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public UserSettingsLanguageFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_settings_language, container, false);
        ListView languageList = (ListView)view.findViewById(R.id.setting_lang_list);
        mLanguageListAdapter = new ArrayAdapter<>(getContext(), R.layout.item_user_settings_languageitem,
                ((UserSettingsActivity)getActivity()).availableLanguages);
        languageList.setAdapter(mLanguageListAdapter);
        languageList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        ((UserSettingsActivity)getActivity()).settingsController.handleClickLanguage(pos);
    }
}
