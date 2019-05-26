package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.SelectSaleProductPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SelectSaleProductView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class CatalogListFragment extends UstadBaseFragment implements SelectSaleProductView {

    View rootContainer;
    private SelectSaleProductPresenter mPresenter;
    private RecyclerView recentRV;
    private RecyclerView categoryRV;
    private RecyclerView collectionRV;
    private FloatingActionMenu floatingActionMenu;
    private TextView recentMore, categoryMore, collectionMore;

    public static CatalogListFragment newInstance(){
        CatalogListFragment fragment = new CatalogListFragment();
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

        //Inflate view
        rootContainer = inflater.inflate(R.layout.activity_select_sale_product, container,false);
        setHasOptionsMenu(true);

        //Set recycler views
        //RecyclerView - Recent
        recentRV = rootContainer.findViewById(
                R.id.activity_select_sale_product_recent_rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recentRV.setLayoutManager(layoutManager);

        //RecyclerView - Category
        categoryRV = rootContainer.findViewById(
                R.id.activity_select_sale_product_category_rv);
        LinearLayoutManager layoutManager2
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        categoryRV.setLayoutManager(layoutManager2);

        //Recyclerview - Collection
        collectionRV = rootContainer.findViewById(
                R.id.activity_select_sale_product_collection_rv);
        LinearLayoutManager layoutManager3
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        collectionRV.setLayoutManager(layoutManager3);

        //Set other views
        recentMore= rootContainer.findViewById(R.id.activity_select_sale_product_recent_more);
        categoryMore = rootContainer.findViewById(R.id.activity_select_sale_product_category_more);
        collectionMore = rootContainer.findViewById(R.id.activity_select_sale_product_collections_more);

        //Call the Presenter
        mPresenter = new SelectSaleProductPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getArguments()), this, true);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        //Set listeners
        floatingActionMenu = rootContainer.findViewById(R.id.activity_select_sale_product_fab_menu);
        rootContainer.findViewById(R.id.activity_select_sale_product_fab_subcategory)
                .setOnClickListener(v -> {
                    floatingActionMenu.close(true);
                    mPresenter.handleClickAddSubCategory();
                });

        rootContainer.findViewById(R.id.activity_select_sale_product_fab_item)
                .setOnClickListener(v -> {
                    floatingActionMenu.close(true);
                    mPresenter.handleClickAddItem();
                });

        recentMore.setOnClickListener(v -> {
            mPresenter.handleClickRecentMore();
        });

        categoryMore.setOnClickListener(v -> {
            mPresenter.handleClickCategoryMore();
        });

        collectionMore.setOnClickListener(v -> {
            mPresenter.handleClickCollectionMore();
        });

        return rootContainer;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Hide the appbar
        rootContainer.findViewById(R.id.activity_select_sale_product_appbar)
                .setVisibility(View.GONE);
    }

    @Override
    public void finish() {

    }

    /**
     * The DIFF CALLBACK
     */
    public static final DiffUtil.ItemCallback<SaleNameWithImage> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SaleNameWithImage>() {
                @Override
                public boolean areItemsTheSame(SaleNameWithImage oldItem,
                                               SaleNameWithImage newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(SaleNameWithImage oldItem,
                                                  SaleNameWithImage newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @Override
    public void setRecentProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        false,true,
                        getContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        recentRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void setCategoryProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        true,true,
                        getContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        listProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        categoryRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void setCollectionProvider(UmProvider<SaleNameWithImage> collectionProvider) {
        SelectSaleProductRecyclerAdapter recyclerAdapter =
                new SelectSaleProductRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        true,true,
                        getContext());

        // get the provider, set , observe, etc.
        // A warning is expected
        DataSource.Factory<Integer, SaleNameWithImage> factory =
                (DataSource.Factory<Integer, SaleNameWithImage>)
                        collectionProvider.getProvider();
        LiveData<PagedList<SaleNameWithImage>> data =
                new LivePagedListBuilder<>(factory, 20).build();
        //Observe the data:
        data.observe(this, recyclerAdapter::submitList);

        //set the adapter
        collectionRV.setAdapter(recyclerAdapter);
    }

    @Override
    public void showMessage(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String toast = impl.getString(messageId, getContext());
        runOnUiThread(() -> Toast.makeText(
                getContext(),
                toast,
                Toast.LENGTH_SHORT
        ).show());
    }
}
