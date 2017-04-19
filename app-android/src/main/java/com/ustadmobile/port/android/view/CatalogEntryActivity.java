package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogEntryPresenter;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Hashtable;

public class CatalogEntryActivity extends UstadBaseActivity implements CatalogEntryView {

    private CatalogEntryPresenter mPresenter;

    private CollapsingToolbarLayout mCollapsingToolbar;

    private RecyclerView seeAlsoRecyclerView;

    private class SeeAlsoViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;

        private TextView titleView;

        public SeeAlsoViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView)itemView.findViewById(R.id.item_catalog_entry_see_also_imageview);
            titleView = (TextView)itemView.findViewById(R.id.item_catalog_entry_see_also_title);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_catalog_entry);
        setUMToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView descTmp = (TextView)findViewById(R.id.activity_catalog_entry_description);
        String str = "";
        for(int i = 0; i < 80; i++) {
            str += "I am a fish. ";
        }
        descTmp.setText(str);

        //mCollapsingToolbar = (CollapsingToolbarLayout)findViewById(R.id.activity_catalog_entry_collapsing_toolbar);

        Hashtable args = UMAndroidUtil.bundleToHashtable(getIntent().getExtras());
        mPresenter = new CatalogEntryPresenter(this, this, args);
        mPresenter.onCreate();

        seeAlsoRecyclerView = (RecyclerView)findViewById(R.id.activity_catalog_entry_see_also_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        seeAlsoRecyclerView.setLayoutManager(linearLayoutManager);
        final Context ctx = this;
        seeAlsoRecyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(ctx).inflate(R.layout.item_catalog_entry_see_also, parent, false);
                return new SeeAlsoViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 5;
            }
        });
        seeAlsoRecyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public void setHeader(String headerFileUri) {

    }

    @Override
    public void setIcon(String iconFileUri) {

    }

    @Override
    public void setMode(int mode) {

    }

    @Override
    public void setProgress(float progress) {

    }

    @Override
    public void setSize(long downloadSize) {

    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        //mCollapsingToolbar.setTitle(title);
    }
}
