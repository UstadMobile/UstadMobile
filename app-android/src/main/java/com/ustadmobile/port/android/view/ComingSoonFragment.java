package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ComingSoonView;

public class ComingSoonFragment extends UstadBaseFragment implements ComingSoonView {
    @Override
    public void finish() {

    }

    View rootContainer;

    public static ComingSoonFragment newInstance(){
        ComingSoonFragment fragment = new ComingSoonFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        rootContainer =
                inflater.inflate(R.layout.fragment_coming_soon, container, false);
        setHasOptionsMenu(true);

        return rootContainer;

    }
}
