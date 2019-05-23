package com.ustadmobile.core.view;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.SaleNameWithImage;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductWithPicture;


/**
 * Core View. Screen is for SelectSaleProduct's View
 */
public interface SelectSaleProductView extends UstadView {


    // This defines the view name that is an argument value in the go() in impl.
    String VIEW_NAME = "SelectSaleProduct";

    //Any argument keys:


    /**
     * Method to finish the screen / view.
     */
    void finish();


    /**
     * Sets the given provider to the view's provider adapter.
     *
     * @param recentProvider The provider to set to the view
     */
    void setRecentProvider(UmProvider<SaleNameWithImage> recentProvider);

    void setCategoryProvider(UmProvider<SaleNameWithImage> categoryProvider);

    void setCollectionProvider(UmProvider<SaleNameWithImage> collectionProvider);

    void showMessage(int messageId);



}

