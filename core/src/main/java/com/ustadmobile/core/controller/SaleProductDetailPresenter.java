package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.db.dao.SaleProductPictureDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.SaleProductDetailView;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductParentJoin;
import com.ustadmobile.lib.db.entities.SaleProductPicture;
import com.ustadmobile.lib.db.entities.SaleProductSelected;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import static com.ustadmobile.core.view.SaleProductDetailView.ARG_ASSIGN_TO_CATEGORY_UID;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_CATEGORY;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_NEW_TITLE;
import static com.ustadmobile.core.view.SaleProductDetailView.ARG_SALE_PRODUCT_UID;

/**
 * Presenter for SaleProductDetail view
 **/
public class SaleProductDetailPresenter extends UstadBaseController<SaleProductDetailView> {

    UmAppDatabase repository;
    private SaleProductDao saleProductDao;
    private SaleProductParentJoinDao productParentJoinDao;
    private UstadMobileSystemImpl impl;
    private SaleProduct currentSaleProduct;
    UmProvider<SaleProductSelected> categoriesProvider;
    private boolean isCategory;

    SaleProductPictureDao pictureDao;

    private Hashtable<Long, Boolean> selectedToCategoriesUid;

    private UmLiveData<SaleProductPicture> pictureLiveData;

    public SaleProductDetailPresenter(Object context, Hashtable arguments, SaleProductDetailView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        saleProductDao = repository.getSaleProductDao();
        productParentJoinDao = repository.getSaleProductParentJoinDao();
        pictureDao = repository.getSaleProductPictureDao();

        impl = UstadMobileSystemImpl.getInstance();

        selectedToCategoriesUid = new Hashtable<>();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Update toolbar title
        String toolbarTitle = "";
        String categoryTitle = "";
        if(getArguments().containsKey(ARG_NEW_TITLE)){
            isCategory = false;
            toolbarTitle = impl.getString(MessageID.create_new_item, context);
            categoryTitle = impl.getString(MessageID.category, context);
        }else if(getArguments().containsKey(ARG_NEW_CATEGORY)){
            toolbarTitle = impl.getString(MessageID.create_new_subcategory, context);
            categoryTitle = impl.getString(MessageID.subcategory, context);
            isCategory = true;
        }
        view.updateToolbarTitle(toolbarTitle);
        view.updateCategoryTitle(categoryTitle);

        //Get SaleProductSelected and update the view
        if(getArguments().containsKey(ARG_SALE_PRODUCT_UID)){
            saleProductDao.findByUidAsync(Long.parseLong(getArguments().get(ARG_SALE_PRODUCT_UID).toString()),
                    new UmCallback<SaleProduct>() {
                @Override
                public void onSuccess(SaleProduct result) {
                    currentSaleProduct = result;
                    updateView();
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }else{
            currentSaleProduct = new SaleProduct("", "", isCategory, false);
            saleProductDao.insertAsync(currentSaleProduct, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    currentSaleProduct.setSaleProductUid(result);
                    updateView();
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }

    }

    private void updateView(){
        long itemUid;
        itemUid = currentSaleProduct.getSaleProductUid();

        view.initFromSaleProduct(currentSaleProduct);

        //Assign
        if(getArguments().containsKey(ARG_ASSIGN_TO_CATEGORY_UID)){
            SaleProductParentJoin defaultJoin = new SaleProductParentJoin(itemUid,
                    Long.parseLong(getArguments().get(ARG_ASSIGN_TO_CATEGORY_UID).toString()), true);
            productParentJoinDao.insert(defaultJoin);
        }

        //Update provider:
        categoriesProvider =
                 productParentJoinDao.findAllSelectedCategoriesForSaleProductProvider(
                        itemUid);
        view.setListProvider(categoriesProvider);

        //Update image on view
        pictureDao.findBySaleProductUidAsync(currentSaleProduct.getSaleProductUid(), new UmCallback<SaleProductPicture>() {
            @Override
            public void onSuccess(SaleProductPicture productPicture) {
                if(productPicture!=null){
                    view.updateImageOnView(pictureDao.getAttachmentPath(productPicture.getSaleProductPictureUid()));
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });

        //Observe the picture
        pictureLiveData = pictureDao.findByProductUidLive(currentSaleProduct.getSaleProductUid());
        pictureLiveData.observe(SaleProductDetailPresenter.this,
                SaleProductDetailPresenter.this::handleProductPictureChanged);

    }

    private void handleProductPictureChanged(SaleProductPicture productPicture){
        if(productPicture!=null){
            view.runOnUiThread(() -> view.updateImageOnView(
                    pictureDao.getAttachmentPath(productPicture.getSaleProductPictureUid())));

        }
    }

    public void handleClickSave() {

        Iterator<Long> selectedIterator = selectedToCategoriesUid.keySet().iterator();
        while(selectedIterator.hasNext()){
            Long productUid = selectedIterator.next();
            Boolean selected = selectedToCategoriesUid.get(productUid);

            //Update assignment.
            productParentJoinDao.createJoin(currentSaleProduct.getSaleProductUid(), productUid, selected);

        }
        currentSaleProduct.setSaleProductActive(true);
        saleProductDao.updateAsync(currentSaleProduct, new UmCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                view.finish();
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }

    public void handleCheckboxChanged(boolean state, long saleProductUid){
        selectedToCategoriesUid.put(saleProductUid, state);
    }

    public void updateTitleEng(String title){
        currentSaleProduct.setSaleProductName(title);
    }
    public void updateTitleDari(String title){
        currentSaleProduct.setSaleProductNameDari(title);
    }
    public void updateTitlePashto(String title){
        currentSaleProduct.setSaleProductNamePashto(title);
    }
    public void updateDescEng(String dec){
        currentSaleProduct.setSaleProductDesc(dec);
    }
    public void updateDescDari(String desc){
        currentSaleProduct.setSaleProductDescDari(desc);
    }
    public void updateDescPashto(String desc){
        currentSaleProduct.setSaleProductDescPashto(desc);
    }

    public SaleProduct getCurrentSaleProduct() {
        return currentSaleProduct;
    }

    public void handleCompressedImage(File imageFile) {

        //Create picture entry

        SaleProductPicture productPicture = new SaleProductPicture();
        productPicture.setSaleProductPictureSaleProductUid(currentSaleProduct.getSaleProductUid());
        productPicture.setSaleProductPictureTimestamp(System.currentTimeMillis());

        pictureDao.insertAsync(productPicture, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long productPictureUid) {
                pictureDao.setAttachmentFromTmpFile(productPictureUid, imageFile);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void openPictureDialog(String imagePath) {
        //TODO if needed.
        //open dialog
    }
}
