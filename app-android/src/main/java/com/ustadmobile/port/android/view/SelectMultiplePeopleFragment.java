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
import com.ustadmobile.core.controller.SelectMultiplePeoplePresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.SelectMultiplePeopleView;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.annotations.NonNull;

/**
 * SelectPeopleDialogFragment Android fragment extends UstadBaseFragmentRe
 */
public class SelectMultiplePeopleFragment extends UstadDialogFragment implements
        SelectMultiplePeopleView,  DismissableDialog {


    AlertDialog dialog;
    View rootView;

    //RecyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private RecyclerView.Adapter mAdapter;

    private SelectMultiplePeoplePresenter mPresenter;
    //Context (Activity calling this)
    private Context mAttachedContext;

    boolean forActor;

    Toolbar toolbar;

    HashMap<String, Long> selectedPeople;

    //Main Activity should implement this ?
    public interface PersonSelectDialogListener{
        void onSelectPeopleListener(HashMap<String, Long> selected, boolean actor);
    }

    /**
     * Generates a new Fragment for a page fragment
     *
     * @return A new instance of fragment SelectClazzesDialogFragment.
     */
    public static SelectMultiplePeopleFragment newInstance() {
        SelectMultiplePeopleFragment fragment = new SelectMultiplePeopleFragment();
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

        rootView = inflater.inflate(R.layout.fragment_select_people_dialog, null);

        //Set up Recycler View
        initView();

        //Toolbar:
        toolbar = rootView.findViewById(R.id.fragment_select_people_dialog_toolbar);
        toolbar.setTitle(R.string.selected_les);

        Drawable upIcon = AppCompatResources.getDrawable(getContext(),
                R.drawable.ic_arrow_back_white_24dp);

        upIcon = getTintedDrawable(upIcon, R.color.icons);

        toolbar.setNavigationIcon(upIcon);
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());


        toolbar.inflateMenu(R.menu.menu_done);
        //Click the tick button on the toolbar:
        toolbar.setOnMenuItemClickListener(item -> {
            int i = item.getItemId();
            if (i == R.id.menu_done) {
                mPresenter.handleCommonPressed(-1);
            }
            return false;
        });

        mPresenter = new SelectMultiplePeoplePresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Dialog stuff:
        //Set any view components and its listener (post presenter work)
        dialog = new AlertDialog.Builder(getContext(),
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
        mRecyclerView = rootView.findViewById(R.id.fragment_select_people_dialog_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.mAttachedContext = context;
        this.selectedPeople = new HashMap<>();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        this.mAttachedContext = null;
        this.selectedPeople = null;
    }

    @Override
    public void finish(){
        selectedPeople = mPresenter.getPeople();
        if(mAttachedContext instanceof PersonSelectDialogListener){
             ((PersonSelectDialogListener) mAttachedContext).onSelectPeopleListener(selectedPeople,
                     forActor);
        }
        dialog.dismiss();
    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<Person> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Person>() {
            @Override
            public boolean areItemsTheSame(Person oldItem,
                                           Person newItem) {
                return oldItem.getPersonUid() == newItem.getPersonUid();
            }

            @Override
            public boolean areContentsTheSame(Person oldItem,
                                              Person newItem) {
                return oldItem.equals(newItem);
            }
        };

    @Override
    public void setListProvider(UmProvider<Person> personListProvider) {
        PersonListReturnSelectedRecyclerAdapter mAdapter =
                new PersonListReturnSelectedRecyclerAdapter(DIFF_CALLBACK, getContext(),
                        this, mPresenter);
        //A warning is expected
        DataSource.Factory<Integer, Person> factory =
                (DataSource.Factory<Integer, Person>)
                        personListProvider.getProvider();
        LiveData<PagedList<Person>> data =
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
    public void dismiss() {

    }
}
