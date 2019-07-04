package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectMultipleProductTypeTreeDialogPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectMultipleProductTypeTreeDialogView;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.reactivex.annotations.NonNull;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * The activity that is a full screen dialog that selects items from a tree structure.
 * Designed to be common for both Location and Classes (although classes wont be in a tree structure).
 * The data should be provided to it to load.
 */
public class SelectMultipleProductTypeTreeDialogFragment extends UstadDialogFragment implements
        SelectMultipleProductTypeTreeDialogView, DismissableDialog {

    //Fragment view components:
    Toolbar toolbar;
    AlertDialog dialog;
    View rootView;

    //Recycler view for the tree
    private RecyclerView recyclerView;
    //Adapter for tree - tellh's TreeViewAdapter
    private TreeViewAdapter adapter;

    //Context (activity calling this)
    private Context mAttachedContext;

    //Daos
    SaleProductParentJoinDao joinDao;

    //Presenter
    SelectMultipleProductTypeTreeDialogPresenter mPresenter;

    //Selected Location Items
    HashMap<String, Long> selectedOptions;
    List<Long> selectedSaleProductUidList;

    //The interface that the hosting activity will implement. The internal method will get called
    // back with the values
    public interface MultiSelectProductTypeTreeDialogListener {

        void onProductTypesResult(HashMap<String, Long> selected);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item =  menu.findItem(R.id.menu_done);

        //Get the icon itself.
        Drawable itemIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp);
        itemIcon.setTint(getResources().getColor(R.color.icons));
        itemIcon.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_IN);
        item.setIcon(itemIcon);

    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_multiple_tree_dialog , null);

        //Set up Recycler view
        initView();

        //Toolbar
        toolbar = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_toolbar);

        //Set up icon to toolbar
        Drawable upIcon = AppCompatResources.getDrawable(getContext(),
                R.drawable.ic_arrow_back_white_24dp);
        upIcon = getTintedDrawable(upIcon, R.color.icons);
        toolbar.setNavigationIcon(upIcon);
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());

        toolbar.inflateMenu(R.menu.menu_done);
        toolbar.setOnMenuItemClickListener(item -> {
            int i = item.getItemId();
            if (i == R.id.menu_done) {
                mPresenter.handleClickPrimaryActionButton();
            }
            return false;
        });
        toolbar.setTitle(R.string.select_product_types);

        //TODO: Get dao - Were using DAOs in the Fragment - Something we should consider changing.
        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(getContext());
        joinDao = repository.getSaleProductParentJoinDao();

        mPresenter = new SelectMultipleProductTypeTreeDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        selectedSaleProductUidList = mPresenter.getSelectedProductTypeUidsList();

        dialog = new AlertDialog.Builder(getContext(), R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create();
        return dialog;

    }

    public Drawable getTintedDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        int tintColor = ContextCompat.getColor(mAttachedContext,color);
        DrawableCompat.setTint(drawable, tintColor);
        return drawable;
    }

    private void initView(){
        //Set recycler view
        recyclerView =
                rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void populateTopProductType(List<SaleProduct> productTypes) {
        List<TreeNode> nodes = new ArrayList<>();

        for(SaleProduct everyProductType : productTypes){
            long childLocationUid = everyProductType.getSaleProductUid();
            boolean selected = false;
            if(selectedSaleProductUidList.contains(childLocationUid)){
                selected = true;
            }
            TreeNode<EntityLayoutType> app = new TreeNode<>(
                    new EntityLayoutType(
                        everyProductType.getSaleProductName(), everyProductType.getSaleProductUid(),
                        selected, false
                    )
            );
            nodes.add(app);
        }

        for(TreeNode childNode : nodes) {
            long childProductTypeUid = ((EntityLayoutType) childNode.getContent()).getUid();
            joinDao.findAllChildProductTypesForUidAsync(childProductTypeUid,
                    new PopulateSaleProductTreeNodeCallback(childNode));
        }

        //Init adapter with the location node binder as types of data to accept
        adapter = new TreeViewAdapterWithBind(this, nodes,
                Arrays.asList(new EntityNodeBinder(mPresenter)));

        //Set adapter to Recycler view.
        runOnUiThread(() -> recyclerView.setAdapter(adapter));

        //Set adapter listener
        adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {

            @Override
            public boolean onClick(TreeNode treeNode, RecyclerView.ViewHolder viewHolder) {
                if(!treeNode.isLeaf()){
                    //A warning is expected
                    List<TreeNode> nodeList = treeNode.getChildList();
                    for(TreeNode childNode : nodeList) {
                        if(childNode.isLeaf()) {
                            //Find all child's children and add then to the node
                            // (via PopulateTreeNodeCallback class)
                            long childProductTypeUid =
                                    ((EntityLayoutType) childNode.getContent()).getUid();
                            //Get child locations :
                            joinDao.findAllChildProductTypesForUidAsync(childProductTypeUid,
                                    new PopulateSaleProductTreeNodeCallback(childNode));
                        }
                    }
                    onToggle(treeNode.isExpand(), viewHolder);
                }else{

                    ImageView arrowIV = viewHolder.itemView.findViewById(
                            R.id.item_select_multiple_tree_dialog_arrow);

                    arrowIV.setVisibility(((EntityLayoutType)treeNode.getContent()).leaf?
                            View.INVISIBLE:View.VISIBLE);
                }

                return false;
            }

            @Override
            public void onToggle(boolean b, RecyclerView.ViewHolder viewHolder) {

                //Change icon of the item.
                EntityNodeBinder.ViewHolder locationViewHolder =
                        (EntityNodeBinder.ViewHolder) viewHolder;
                ImageView arrowImage = locationViewHolder.getIvArrow();
                int rotateDegree = b ? 90 : -90;
                arrowImage.animate().rotationBy(rotateDegree).start();

            }
        });
    }

    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void finish() {
        //Get the selected Option, and pass it to the activity implementing this
        // (parent activity usually)
        selectedOptions = mPresenter.getSelectedOptions();
        if(mAttachedContext instanceof MultiSelectProductTypeTreeDialogListener) {
            ((MultiSelectProductTypeTreeDialogListener)
                    mAttachedContext).onProductTypesResult(selectedOptions);
        }
        dialog.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mAttachedContext = context;
        this.selectedOptions = new HashMap<>();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mAttachedContext = null;
        this.selectedOptions = null;
    }


    /**
     * A Custom callback - on success will check the return list of entities,
     *  loop over them, and add them to the node as children on the view.
     *  If there are no entities returned, it will treat the current node as a leaf.
     */
    private class PopulateSaleProductTreeNodeCallback implements UmCallback<List<SaleProduct>> {

        private TreeNode node;

        private PopulateSaleProductTreeNodeCallback(TreeNode node) {
            this.node = node;
        }

        @Override
        public void onSuccess(List<SaleProduct> result) {
            runOnUiThread(() -> {
                for(SaleProduct childLocations : result) {
                    long childLocationUid = childLocations.getSaleProductUid();
                    boolean selected = false;
                    if(selectedSaleProductUidList.contains(childLocationUid)){
                        selected = true;
                    }

                    node.addChild(new TreeNode<>(
                            new EntityLayoutType(childLocations.getSaleProductName(),
                                    childLocationUid, selected, false)));
                }
                if(!result.isEmpty()){
                    ((EntityLayoutType)node.getContent()).leaf = false;
                }else{
                    ((EntityLayoutType)node.getContent()).leaf = true;
                }

            });

            if(!result.isEmpty()){
                ((EntityLayoutType)node.getContent()).leaf = false;
            }else{
                ((EntityLayoutType)node.getContent()).leaf = true;
            }
        }

        @Override
        public void onFailure(Throwable exception) { exception.printStackTrace();}
    }

    /**
     * Custom TreeView Adapter written so that we can work with onBindView and manipulate the
     * view on every tree node. The type is of the EntityLayoutType POJO
     *
     */
    public class TreeViewAdapterWithBind extends TreeViewAdapter {

        private SelectMultipleProductTypeTreeDialogFragment selectMultipleTreeDialogFragment;

        public TreeViewAdapterWithBind(SelectMultipleProductTypeTreeDialogFragment fragment,
                                       List<TreeNode> nodes,
                                       List<? extends TreeViewBinder> viewBinders) {
            super(nodes, viewBinders);
            this.selectMultipleTreeDialogFragment = fragment;
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

            EntityLayoutType displayNodeContent = (EntityLayoutType) displayNode.getContent();
            long locationUid = displayNodeContent.uid;

            if (selectMultipleTreeDialogFragment.selectedSaleProductUidList != null
                    && selectMultipleTreeDialogFragment.selectedSaleProductUidList.contains(locationUid)){
                locationCB.setChecked(true);
            }else{
                locationCB.setChecked(false);
            }

            arrowIV.setVisibility(displayNodeContent.leaf? View.INVISIBLE:View.VISIBLE);

        }
    }
}
