package com.ustadmobile.port.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.AddSaleProductToSaleCategoryPresenter;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.view.AddSaleProductToSaleCategoryView;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class AddSaleProductToSaleCategoryActivity extends UstadBaseActivity
        implements AddSaleProductToSaleCategoryView {

    private Toolbar toolbar;
    private AddSaleProductToSaleCategoryPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private ConstraintLayout addItemCL;
    private TextView addTextView;

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
        setContentView(R.layout.activity_add_sale_product_to_sale_category);

        //Toolbar:
        toolbar = findViewById(R.id.activity_add_sale_product_to_sale_category_toolbar);
        toolbar.setTitle(getText(R.string.add_item));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_add_sale_product_to_sale_category_recyclerview);
        RecyclerView.LayoutManager mRecyclerLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        addItemCL = findViewById(R.id.activity_add_sale_product_to_sale_category_add_cl);
        addTextView = findViewById(R.id.activity_add_sale_product_to_sale_category_add_text);

        //Call the Presenter
        mPresenter = new AddSaleProductToSaleCategoryPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));


        //Listener
        addItemCL.setOnClickListener(v -> mPresenter.handleAddNewItem());

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
    public void setListProvider(UmProvider<SaleNameWithImage> listProvider) {
        SelectSaleProductToSaleCategoryRecyclerAdapter recyclerAdapter =
                new SelectSaleProductToSaleCategoryRecyclerAdapter(DIFF_CALLBACK, mPresenter, this,
                        false, getApplicationContext());

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
        mRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void setAddtitle(String title) {
        addTextView.setText(title);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }
}
