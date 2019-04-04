/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;
import com.ustadmobile.port.android.impl.DbInitialEntriesInserter;

import java.util.List;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


public class SplashScreenActivity extends UstadBaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        /*OneTimeWorkRequest dbWork =
                new OneTimeWorkRequest.Builder(
                        DbInitialEntriesInserter.DbInitialEntriesInserterWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(dbWork);
        */
        //Add dummy data
        addDummyData();

    }

    @Override
    public void onStart() {
        super.onStart();
        UstadMobileSystemImpl.getInstance().startUI(SplashScreenActivity.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leavecontainer) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addDummyData(){
        UmAppDatabase repo  = UmAppDatabase.getInstance(getContext());
        SaleDao saleDao = repo.getSaleDao();
        LocationDao locationDao = repo.getLocationDao();


        //Create a new location
        Location newLocation = new Location();
        newLocation.setTitle("Test location");
        newLocation.setParentLocationUid(0);
        newLocation.setDescription("Test location added from Dummy data");
        locationDao.insertAsync(newLocation, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                newLocation.setLocationUid(result);


                String pinkHatSaleTitle = "20x pink hat";
                saleDao.findAllSaleWithTitleAsync(pinkHatSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()){
                            Sale pinkHatSale = new Sale();
                            pinkHatSale.setSaleTitle(pinkHatSaleTitle);
                            pinkHatSale.setSaleActive(true);
                            pinkHatSale.setSaleCancelled(false);
                            pinkHatSale.setSalePreOrder(true);
                            pinkHatSale.setSaleDone(true);
                            pinkHatSale.setSalePaymentDone(false);
                            pinkHatSale.setSaleLocationUid(newLocation.getLocationUid());
                            pinkHatSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(0));
                            pinkHatSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(-1));
                            saleDao.insert(pinkHatSale);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });



                String tableClothTitle = "20x table Cloth";
                saleDao.findAllSaleWithTitleAsync(tableClothTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()){
                            Sale tableClothSale = new Sale();
                            tableClothSale.setSaleTitle(tableClothTitle);
                            tableClothSale.setSaleActive(true);
                            tableClothSale.setSaleCancelled(false);
                            tableClothSale.setSaleDone(true);
                            tableClothSale.setSalePreOrder(true);
                            tableClothSale.setSaleLocationUid(newLocation.getLocationUid());
                            tableClothSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(-2));
                            tableClothSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(2));
                            saleDao.insert(tableClothSale);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {

                    }
                });


                String hatSaleTitle = "10x hat";
                saleDao.findAllSaleWithTitleAsync(hatSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {

                        if (result.isEmpty()){
                            Sale hatSale = new Sale();
                            hatSale.setSaleTitle(hatSaleTitle);
                            hatSale.setSaleActive(true);
                            hatSale.setSaleCancelled(false);
                            hatSale.setSaleDone(true);
                            hatSale.setSaleLocationUid(newLocation.getLocationUid());
                            hatSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(-2));
                            hatSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(2));
                            saleDao.insert(hatSale);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


                String shawlSaleTitle = "20x shawl";
                saleDao.findAllSaleWithTitleAsync(shawlSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()){
                            Sale shawlSale = new Sale();
                            shawlSale.setSaleTitle(shawlSaleTitle);
                            shawlSale.setSaleActive(true);
                            shawlSale.setSaleCancelled(false);
                            shawlSale.setSaleDone(true);
                            shawlSale.setSalePreOrder(true);
                            shawlSale.setSalePaymentDone(false);
                            shawlSale.setSaleLocationUid(newLocation.getLocationUid());
                            shawlSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(-3));
                            shawlSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(1));
                            saleDao.insert(shawlSale);

                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


                String pilloSaleTitle = "1x pillow case";
                saleDao.findAllSaleWithTitleAsync(pilloSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()){
                            Sale pillowSale = new Sale();
                            pillowSale.setSaleTitle(pilloSaleTitle);
                            pillowSale.setSaleActive(true);
                            pillowSale.setSaleCancelled(false);
                            pillowSale.setSaleDone(true);
                            pillowSale.setSaleLocationUid(newLocation.getLocationUid());
                            pillowSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(-4));
                            pillowSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(3));
                            saleDao.insert(pillowSale);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });






    }


}
