package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.RoleDetailPresenter;
import com.ustadmobile.core.view.RoleDetailView;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class RoleDetailActivity extends UstadBaseActivity implements RoleDetailView {

    private Toolbar toolbar;
    private RoleDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private EditText title;


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_role_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_role_detail_toolbar);
        toolbar.setTitle(getText(R.string.new_role));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_role_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        title = findViewById(R.id.activity_role_detail_name);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateRoleName(s.toString());
            }
        });

        //Call the Presenter
        mPresenter = new RoleDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_role_detail_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickDone());


    }

    @Override
    public void updateRoleOnView(Role role) {

        String roleName = "";


        if(role != null){
            if(role.getRoleName() != null){
                roleName = role.getRoleName();
            }
        }

        String finalRoleName = roleName;
        runOnUiThread(() -> title.setText(finalRoleName));

        //TODO: update ticks on permissions as well.

    }
}
