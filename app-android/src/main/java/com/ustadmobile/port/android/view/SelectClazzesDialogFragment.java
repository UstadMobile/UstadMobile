package com.ustadmobile.port.android.view;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectClazzesDialogPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectClazzesDialogView;
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

/**
 * SelectClazzesDialogFragment Android fragment extends UstadBaseFragment
 */
public class SelectClazzesDialogFragment extends UstadDialogFragment implements
        SelectClazzesDialogView, AdapterView.OnItemSelectedListener,
        DialogInterface.OnClickListener, DialogInterface.OnShowListener,
        View.OnClickListener, DismissableDialog {


    AlertDialog dialog;
    View rootView;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private SelectClazzesDialogPresenter mPresenter;
    //Context (Activity calling this)
    private Context mAttachedContext;

    Toolbar toolbar;

    HashMap<String, Long> selectedClazzes;

    //Main Activity should implement this ?
    public interface ClazzSelectDialogListener{
        void onSelectClazzesResult(HashMap<String, Long> selected);
    }

    /**
     * Generates a new Fragment for a page fragment
     * TODO: Add any args if needed
     *
     * @return A new instance of fragment SelectClazzesDialogFragment.
     */
    public static SelectClazzesDialogFragment newInstance() {
        SelectClazzesDialogFragment fragment = new SelectClazzesDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public Drawable getTintedDrawable(Drawable drawable, int color) {
        drawable = DrawableCompat.wrap(drawable);
        int tintColor = ContextCompat.getColor(getContext(), color);
        DrawableCompat.setTint(drawable, tintColor);
        return drawable;
    }

    @android.support.annotation.NonNull
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater =
                (LayoutInflater) Objects.requireNonNull(getContext()).getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        rootView = inflater.inflate(R.layout.fragment_select_clazzes_dialog, null);

        //Set up Recycler View
        initView();

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_clazzes_dialog_toolbar);
        toolbar.setTitle(R.string.select_classes);

        Drawable upIcon = AppCompatResources.getDrawable(getContext(),
                R.drawable.ic_arrow_back_white_24dp);

        upIcon = getTintedDrawable(upIcon, R.color.icons);

        toolbar.setNavigationIcon(upIcon);
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());


        toolbar.inflateMenu(R.menu.menu_done);
        //Click the tick button on the toolbar:
        toolbar.setOnMenuItemClickListener(item -> {
            int i = item.getItemId();
            if (i == R.id.menu_catalog_entry_presenter_share) {
                mPresenter.handleCommonPressed(-1);
            }
            return false;
        });

        mPresenter = new SelectClazzesDialogPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Dialog stuff:
        //Set any view components and its listener (post presenter work)
        dialog = new android.support.v7.app.AlertDialog.Builder(getContext(),
                R.style.FullScreenDialogStyle)
                .setView(rootView)
                .setTitle("")
                .create();
        return dialog;

    }


    public static Drawable getTintedDrawable(@NonNull final Context context,
                                             @DrawableRes int drawableRes, @ColorRes int colorRes) {
        Drawable d = ContextCompat.getDrawable(context, drawableRes);
        d = DrawableCompat.wrap(d);
        DrawableCompat.setTint(d.mutate(), ContextCompat.getColor(context, colorRes));
        return d;
    }

    private void initView(){
        //Set recycler view
        mRecyclerView = rootView.findViewById(R.id.fragment_select_clazzes_dialog_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
        this.selectedClazzes = new HashMap<>();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
        this.selectedClazzes = null;
    }

    @Override
    public void finish(){
        selectedClazzes = mPresenter.getClazzes();
        if(mAttachedContext instanceof ClazzSelectDialogListener){
             ((ClazzSelectDialogListener) mAttachedContext).onSelectClazzesResult(selectedClazzes);
        }
        dialog.dismiss();
    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<ClazzWithNumStudents> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ClazzWithNumStudents>() {
            @Override
            public boolean areItemsTheSame(ClazzWithNumStudents oldItem,
                                           ClazzWithNumStudents newItem) {
                return oldItem.getClazzUid() == newItem.getClazzUid();
            }

            @Override
            public boolean areContentsTheSame(ClazzWithNumStudents oldItem,
                                              ClazzWithNumStudents newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setClazzListProvider(UmProvider<ClazzWithNumStudents> clazzListProvider) {
        ClazzListReturnSelectedRecyclerAdapter mAdapter =
                new ClazzListReturnSelectedRecyclerAdapter(DIFF_CALLBACK, getContext(),
                        this, mPresenter);
        //A warning is expected
        DataSource.Factory<Integer, ClazzWithNumStudents> factory =
                (DataSource.Factory<Integer, ClazzWithNumStudents>)
                        clazzListProvider.getProvider();
        LiveData<PagedList<ClazzWithNumStudents>> data =
                new LivePagedListBuilder<>(factory, 20).build();

        data.observe(this, mAdapter::submitList);

        mRecyclerView.setAdapter(mAdapter);
    }

    // This event is triggered soon after onCreateView().
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here

    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onShow(DialogInterface dialog) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void dismiss() {

    }
}
