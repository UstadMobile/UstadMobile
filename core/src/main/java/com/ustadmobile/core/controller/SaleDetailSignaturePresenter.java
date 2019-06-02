package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.SaleDetailSignatureView;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;

import static com.ustadmobile.core.view.SaleDetailView.ARG_SALE_UID;


/**
 * Presenter for SaleDetailSignature view
 **/
public class SaleDetailSignaturePresenter extends UstadBaseController<SaleDetailSignatureView> {

    UmAppDatabase repository;
    private String currentSignSvg = null;
    private Sale currentSale = null;
    private SaleDao saleDao;
    private long currentSaleUid = 0L;


    public SaleDetailSignaturePresenter(Object context, Hashtable arguments,
                                        SaleDetailSignatureView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        saleDao = repository.getSaleDao();
        if(getArguments().containsKey(ARG_SALE_UID)){
            currentSaleUid = Long.parseLong(getArguments().get(ARG_SALE_UID).toString());
        }

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(currentSaleUid!=0L){
            saleDao.findByUidAsync(currentSaleUid, new UmCallback<Sale>() {
                @Override
                public void onSuccess(Sale result) {
                    currentSale = result;
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }


    public void handleClickAccept() {
        if(currentSale != null){
            if(currentSignSvg != null && !currentSignSvg.isEmpty()) {
                currentSale.setSaleSignature(currentSignSvg);
                saleDao.updateAsync(currentSale, new UmCallback<Integer>() {
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
        }
    }

    public void updateSignatureSvg(String signSvg) {
        currentSignSvg = signSvg;
    }
}
