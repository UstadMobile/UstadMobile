package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.model.UserSettingItem;

/**
 *
 */
public class UserSettingsListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private UserSettingListAdapter listAdapter;

    private ListView mSettingList;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserSettingsListFragment.
     */
    public static UserSettingsListFragment newInstance() {
        UserSettingsListFragment fragment = new UserSettingsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public UserSettingsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_user_settings_list, container, false);
        listAdapter = new UserSettingListAdapter(getContext());
        mSettingList = (ListView)retView.findViewById(R.id.setting_list);
        mSettingList.setAdapter(listAdapter);
        mSettingList.setOnItemClickListener(this);
        updateSettingsList();

        return retView;
    }

    void updateSettingsList() {
        listAdapter.clear();
        for(UserSettingItem item : ((UserSettingsActivity)getActivity()).settingItems ) {
            listAdapter.add(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        ((UserSettingsActivity)getActivity()).settingsController.handleClickSetting(pos);
    }

    public class UserSettingListAdapter extends ArrayAdapter<UserSettingItem> {

        public UserSettingListAdapter(final Context context) {
            super(context, 0);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            UserSettingItem item = getItem(position);
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_usersettingsitem, null);
            }

            ((TextView)convertView.findViewById(R.id.setting_name_text)).setText(item.settingName);
            ((TextView)convertView.findViewById(R.id.setting_value_text)).setText(item.settingValue);

            return convertView;
        }
    }


}
