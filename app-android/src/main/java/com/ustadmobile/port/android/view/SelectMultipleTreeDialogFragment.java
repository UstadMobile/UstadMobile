package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectMultipleTreeDialogView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.annotations.NonNull;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

/**
 * The activity that is a full screen dialog that selects items from a tree structure.
 * Designed to be common for both Location and Classes (although classes wont be in a tree structure).
 * The data should be provided to it to load.
 */
public class SelectMultipleTreeDialogFragment extends UstadDialogFragment implements
        SelectMultipleTreeDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog  {


    AlertDialog dialog;
    View rootView;

    //Recycler view for the tree
    private RecyclerView recyclerView;
    //Adater for tree
    private TreeViewAdapter adapter;

    //Context (activity calling this)
    private Context mAttachedContext;

    Toolbar toolbar;


    public interface MultiSelectTreeDialogListener {

        void onResult();

    }

    //Presenter?

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

        //Get initial data
        List<TreeNode> nodes = initTestData();

        //Init adapter with the location node binder as types of data to accept
        adapter = new TreeViewAdapter(nodes, Arrays.asList(new LocationNodeBinder()));

        //Set adapter listener
        adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
            @Override
            public boolean onClick(TreeNode treeNode, RecyclerView.ViewHolder viewHolder) {
                //get the location object associated with this node (using treeNodegetContent)
                 // would be something like getContent().isSite() or isLeaf()

                //Location nodeLocation = treeNode.getContent();
                //boolean ourLocationIsLeaf = nodeLocation.isSite();

                if(treeNode.isLeaf()) {
                    //now run the query async, get the result, and add children to treeNode

                    treeNode.addChild(
                            new TreeNode<>(new LocationLayoutType("NEW"))
                    );

                }

                if(!treeNode.isLeaf()){
                    //Update and toggle the node.
                    onToggle(!treeNode.isExpand(), viewHolder);
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

        //Set adapter to Recycler view.
        recyclerView.setAdapter(adapter);

        toolbar = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_toolbar);
        toolbar.setTitle(R.string.select_locations);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());
        toolbar.inflateMenu(R.menu.menu_done);
        toolbar.setOnMenuItemClickListener(item -> {
            int i = item.getItemId();
            if (i == R.id.menu_catalog_entry_presenter_share) {
                System.out.println("DONE");
            }
            return false;
        });

        //Set view components
        //Set component listeners

        //Dialog's positive / negative listeners :
        DialogInterface.OnClickListener positiveOCL =
                (dialog, which) -> System.out.print("Positive");

        DialogInterface.OnClickListener negativeOCL =
                (dialog, which) -> System.out.println("Negative");

        //Set presenter.
        //Call it's onCreate()

        //Set any view components and its listener (post presenter work)

        dialog = new AlertDialog.Builder(getContext(), R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create();
        return dialog;

    }

    private void initView(){
        //Set recycler view
        recyclerView = rootView.findViewById(R.id.fragment_select_multiple_tree_dialog_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private List<TreeNode> initTestData(){
        List<TreeNode> nodes = new ArrayList<>();
        TreeNode<LocationLayoutType> app = new TreeNode<>(new LocationLayoutType(("Earth")));
        nodes.add(app);
        app.addChild(
            new TreeNode<>(new LocationLayoutType("Asia"))
                .addChild(new TreeNode<>(new LocationLayoutType("UAE"))

            ));

        app.addChild(
            new TreeNode<>(new LocationLayoutType("java")).addChild(
                new TreeNode<>(new LocationLayoutType("tellh")).addChild(
                    new TreeNode<>(new LocationLayoutType("com")).addChild(
                        new TreeNode<>(new LocationLayoutType("recyclertreeview"))
                            .addChild(new TreeNode<>(new LocationLayoutType("Dir")))
                            .addChild(new TreeNode<>(new LocationLayoutType("DirectoryNodeBinder")))
                            .addChild(new TreeNode<>(new LocationLayoutType("File")))
                            .addChild(new TreeNode<>(new LocationLayoutType("FileNodeBinder")))
                            .addChild(new TreeNode<>(new LocationLayoutType("TreeViewBinder")))
                    )
                )
            )
        );
        TreeNode<LocationLayoutType> res = new TreeNode<>(new LocationLayoutType("res"));
        nodes.add(res);

        res.addChild(
                new TreeNode<>(new LocationLayoutType("mipmap"))
                        .addChild(new TreeNode<>(new LocationLayoutType("ic_launcher.png")))
        );

        return nodes;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void finish() {
        if(mAttachedContext instanceof MultiSelectTreeDialogListener) {
            ((MultiSelectTreeDialogListener)mAttachedContext).onResult();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mAttachedContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.mAttachedContext = null;
    }
}
