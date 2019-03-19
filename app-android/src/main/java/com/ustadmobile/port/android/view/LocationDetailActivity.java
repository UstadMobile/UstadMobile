package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LocationDetailPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.LocationDetailView;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

public class LocationDetailActivity extends UstadBaseActivity implements LocationDetailView {

    private Toolbar toolbar;
    private LocationDetailPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private EditText locationTitle;
    //Adapter for tree
    private TreeViewAdapter adapter;
    LocationDao locationDao;
    HashMap<String, Long> selectedOptions;
    List<Long> selectedLocationList;

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
        setContentView(R.layout.activity_location_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_location_detail_toolbar);
        toolbar.setTitle(getText(R.string.add_edit_location));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        locationTitle = findViewById(R.id.activity_location_detail_name);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_location_detail_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(getContext());
        locationDao = repository.getLocationDao();

        //Call the Presenter
        mPresenter = new LocationDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        selectedLocationList = mPresenter.getSelectedLocationsList();

        locationTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.updateLocationTitle(s.toString());
            }
        });

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_location_detail_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickDone());


    }

    @Override
    public void populateTopLocation(List<Location> locations) {
        List<TreeNode> nodes = new ArrayList<>();

        for(Location every_location : locations){
            long childLocationUid = every_location.getLocationUid();
            boolean selected = false;
            if(selectedLocationList.contains(childLocationUid)){
                selected = true;
            }
            TreeNode<LocationLayoutType> app = new TreeNode<>(
                    new LocationLayoutType(
                            every_location.getTitle(), every_location.getLocationUid(),
                            selected, false
                    )
            );
            nodes.add(app);
        }

        for(TreeNode childNode : nodes) {
            long childLocationUid = ((LocationLayoutType) childNode.getContent()).getUid();
            locationDao.findAllChildLocationsForUidAsync(childLocationUid,
                    new LocationDetailActivity.PopulateTreeNodeCallback(childNode));
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = new TreeViewAdapterWithBind(nodes,
                Arrays.asList(new LocationNodeBinder(mPresenter)));

        //Set adapter to Recycler view.
        runOnUiThread(() -> mRecyclerView.setAdapter(adapter));


        //Set adapter listener
        adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {

            @Override
            public boolean onClick(TreeNode treeNode, RecyclerView.ViewHolder viewHolder) {
                if(!treeNode.isLeaf()){
                    List<TreeNode> nodeList = treeNode.getChildList();
                    for(TreeNode childNode : nodeList) {
                        if(childNode.isLeaf()) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            long childLocationUid = ((LocationLayoutType) childNode.getContent()).getUid();
                            locationDao.findAllChildLocationsForUidAsync(childLocationUid,
                                    new LocationDetailActivity.PopulateTreeNodeCallback(childNode));
                        }
                    }
                    onToggle(treeNode.isExpand(), viewHolder);
                }

                return false;
            }

            @Override
            public void onToggle(boolean b, RecyclerView.ViewHolder viewHolder) {

                //Change icon of the item.
                LocationNodeBinder.ViewHolder locationViewHolder =
                        (LocationNodeBinder.ViewHolder) viewHolder;
                ImageView arrowImage = locationViewHolder.getIvArrow();
                int rotateDegree = b ? 90 : -90;
                arrowImage.animate().rotationBy(rotateDegree).start();

            }
        });
    }

    @Override
    public void updateLocationOnView(Location location) {
        if(location != null){
            locationTitle.setText(location.getTitle());

            selectedLocationList = new ArrayList<>();
            selectedLocationList.add(location.getParentLocationUid());


        }
    }

    private class PopulateTreeNodeCallback implements UmCallback<List<Location>> {

        private TreeNode node;

        private PopulateTreeNodeCallback(TreeNode node) {
            this.node = node;
        }

        @Override
        public void onSuccess(List<Location> result) {
            runOnUiThread(() -> {
                for(Location childLocations : result) {
                    long childLocationUid = childLocations.getLocationUid();
                    boolean selected = false;
                    if(selectedLocationList.contains(childLocationUid)){
                        selected = true;
                    }

                    node.addChild(new TreeNode<>(
                            new LocationLayoutType(childLocations.getTitle(),
                                    childLocationUid, selected, false)));
                }
                if(!result.isEmpty()){
                    ((LocationLayoutType)node.getContent()).leaf = false;
                }else{
                    ((LocationLayoutType)node.getContent()).leaf = true;
                }

            });

            if(!result.isEmpty()){
                ((LocationLayoutType)node.getContent()).leaf = false;
            }else{
                ((LocationLayoutType)node.getContent()).leaf = true;
            }
        }

        @Override
        public void onFailure(Throwable exception) { exception.printStackTrace();}
    }

    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node.
     *
     */
    public class TreeViewAdapterWithBind extends TreeViewAdapter{

        public TreeViewAdapterWithBind(List<TreeNode> nodes, List<? extends TreeViewBinder> viewBinders) {
            super(nodes, viewBinders);
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);

            CheckBox locationCB = holder.itemView.findViewById(
                    R.id.item_select_multiple_tree_dialog_checkbox);
            ImageView arrowIV = holder.itemView.findViewById(
                    R.id.item_select_multiple_tree_dialog_arrow);

            Iterator<TreeNode> displayNodesIterator = getDisplayNodesIterator();
            TreeNode displayNode = null;
            int i=0;
            while(displayNodesIterator.hasNext()){
                displayNode = displayNodesIterator.next();
                if(i>=position){
                    break;
                }else{
                    i++;
                }
            }

            LocationLayoutType displayNodeContent = (LocationLayoutType) displayNode.getContent();
            long locationUid = displayNodeContent.uid;

            if (selectedLocationList != null && selectedLocationList.contains(locationUid)){
                locationCB.setChecked(true);
            }else{
                locationCB.setChecked(false);
            }

            arrowIV.setVisibility(displayNodeContent.leaf? View.INVISIBLE:View.VISIBLE);

        }
    }

}
