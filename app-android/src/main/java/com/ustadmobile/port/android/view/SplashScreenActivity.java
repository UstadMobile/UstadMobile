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

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductGroupDao;
import com.ustadmobile.core.db.dao.SaleProductGroupJoinDao;
import com.ustadmobile.core.db.dao.SaleProductPictureDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductGroup;
import com.ustadmobile.lib.db.entities.SaleProductGroupJoin;
import com.ustadmobile.lib.db.entities.SaleProductPicture;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


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
        UmAppDatabase repo  = UmAppDatabase.getInstance(getContext());


        addDummyData(repo);

    }

    @Override
    public void onStart() {
        super.onStart();
        UstadMobileSystemImpl.getInstance().startUI(SplashScreenActivity.this);
    }


    public void addPicToProduct(SaleProduct product, String assetPath, UmAppDatabase repo){
        //Create picture entry
        SaleProductPictureDao pictureDao = repo.getSaleProductPictureDao();
        SaleProductPicture productPicture = new SaleProductPicture();
        productPicture.setSaleProductPictureSaleProductUid(product.getSaleProductUid());
        productPicture.setSaleProductPictureTimestamp(System.currentTimeMillis());

        pictureDao.insertAsync(productPicture, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long productPictureUid) {

                InputStream assetIS;
                try {
                    assetIS = getAssets().open(assetPath);
                    pictureDao.setAttachment(productPictureUid, assetIS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    private void addDummyData(UmAppDatabase repo){

        SaleDao saleDao = repo.getSaleDao();
        LocationDao locationDao = repo.getLocationDao();
        SaleItemDao saleItemDao = repo.getSaleItemDao();
        SaleProductDao saleProductDao = repo.getSaleProductDao();
        PersonDao personDao = repo.getPersonDao();
        SaleProductGroupDao productGroupDao = repo.getSaleProductGroupDao();
        SaleProductGroupJoinDao productGroupJoinDao = repo.getProductGroupJoinDao();


        //Create a new location
        locationDao.findByTitleAsync("Test location", new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> result) {
                Location newLocation;
                if (result.isEmpty()) {
                    newLocation = new Location();
                    newLocation.setTitle("Test location");
                    newLocation.setParentLocationUid(0);
                    newLocation.setDescription("Test location added from Dummy data");
                    newLocation.setLocationUid(locationDao.insert(newLocation));

                    //Also add some Producers (People)

                    Person person1 = new Person("royarahimi", "Roya", "Rahimi");
                    Person person2 = new Person("lailagulzar", "Laila", "Gulzar");
                    Person person3 = new Person("meenahotaki", "Meena", "Hotaki");
                    Person person4 = new Person("nargisyousafzi", "Nargis", "Yousafzi");

                    person1.setActive(true);
                    person2.setActive(true);
                    person3.setActive(true);
                    person4.setActive(true);

                    personDao.insert(person1);
                    personDao.insert(person2);
                    personDao.insert(person3);
                    personDao.insert(person4);


                } else {
                    newLocation = result.get(0);
                }

                //Insert Products
                String pinkHatName = "Pink Hat";
                SaleProduct pinkHatProduct = saleProductDao.findByName(pinkHatName);
                if(pinkHatProduct == null){
                    pinkHatProduct = new SaleProduct(pinkHatName, "Testing dummy data");
                    pinkHatProduct.setSaleProductUid(saleProductDao.insert(pinkHatProduct));
                    addPicToProduct(pinkHatProduct,
                            "goldozi/goldozi_product_placeholder_pink_hats.png", repo);

                    //Create other products
                    SaleProduct greenSheets = new SaleProduct("Green Sheet", "Testing dummy data");
                    greenSheets.setSaleProductUid(saleProductDao.insert(greenSheets));
                    addPicToProduct(greenSheets,
                            "goldozi/goldozi_product_placeholder_green_sheets.png", repo);

                    SaleProduct floralScarves  = new SaleProduct("Floral Scarf", "Dummy data");
                    floralScarves.setSaleProductUid(saleProductDao.insert(floralScarves));
                    addPicToProduct(floralScarves,
                            "goldozi/goldozi_product_placeholder_floral_scarves.png", repo);

                    SaleProduct pinkBedLinen = new SaleProduct("Pink Bed Linen", "Dummy data");
                    pinkBedLinen.setSaleProductUid(saleProductDao.insert(pinkBedLinen));
                    addPicToProduct(pinkBedLinen,
                            "goldozi/goldozi_product_placeholder_bed_linen.png", repo);

                    SaleProduct redTableLinen = new SaleProduct("Red Table Linen", "Dummy data");
                    redTableLinen.setSaleProductUid(saleProductDao.insert(redTableLinen));
                    addPicToProduct(redTableLinen,
                            "goldozi/goldozi_product_placeholder_table_linen.png", repo);

                    SaleProduct zariToy = new SaleProduct("Zari Bagchesimsim", "Dummy data");
                    zariToy.setSaleProductUid(saleProductDao.insert(zariToy));
                    addPicToProduct(zariToy,
                            "goldozi/goldozi_product_placeholder_toys.png", repo);

                    SaleProduct womenSuit  = new SaleProduct("Women Suit", "Dummy data");
                    womenSuit.setSaleProductUid(saleProductDao.insert(womenSuit));
                    addPicToProduct(womenSuit,
                            "goldozi/goldozi_product_placeholder_women.png", repo);

                    SaleProduct mensBelt  = new SaleProduct("Men Belt", "Dummy data");
                    mensBelt.setSaleProductUid(saleProductDao.insert(mensBelt));
                    addPicToProduct(mensBelt,
                            "goldozi/goldozi_product_placeholder_men.png", repo);

                    SaleProduct beigeClutch  = new SaleProduct("Beige Clutch", "Dummy data");
                    beigeClutch.setSaleProductUid(saleProductDao.insert(beigeClutch));
                    addPicToProduct(beigeClutch,
                            "goldozi/goldozi_product_placeholder_accessories.png", repo);


                    //Create categories
                    SaleProductGroup bedLinenGroup = new SaleProductGroup("Bed Linen");
                    bedLinenGroup.setSaleProductGroupUid(productGroupDao.insert(bedLinenGroup));
                    SaleProductGroupJoin bedLinen1 =
                            new SaleProductGroupJoin(pinkBedLinen.getSaleProductUid(),
                                    bedLinenGroup.getSaleProductGroupUid());
                    productGroupJoinDao.insert(bedLinen1);

                    SaleProductGroup tableLinenGroup = new SaleProductGroup("Table Linen");
                    tableLinenGroup.setSaleProductGroupUid(productGroupDao.insert(tableLinenGroup));
                    SaleProductGroupJoin tableLinen1 =
                            new SaleProductGroupJoin(redTableLinen.getSaleProductUid(),
                                    tableLinenGroup.getSaleProductGroupUid());
                    productGroupJoinDao.insert(tableLinen1);

                    SaleProductGroup toyGroup = new SaleProductGroup("Toys");
                    toyGroup.setSaleProductGroupUid(productGroupDao.insert(toyGroup));
                    SaleProductGroupJoin toy1 =
                            new SaleProductGroupJoin(zariToy.getSaleProductUid(),
                                    toyGroup.getSaleProductGroupUid());
                    productGroupJoinDao.insert(toy1);

                    //Create collections
                    SaleProductGroup womenEidCollection = new SaleProductGroup("Women");
                    womenEidCollection.setSaleProductGroupType(SaleProductGroup.PRODUCT_GROUP_TYPE_COLLECTION);
                    womenEidCollection.setSaleProductGroupUid(productGroupDao.insert(womenEidCollection));
                    SaleProductGroupJoin womenCollection1 =
                            new SaleProductGroupJoin(womenSuit.getSaleProductUid(),
                                    womenEidCollection.getSaleProductGroupUid());
                    productGroupJoinDao.insert(womenCollection1);

                    SaleProductGroup menCollection = new SaleProductGroup("Men");
                    menCollection.setSaleProductGroupType(SaleProductGroup.PRODUCT_GROUP_TYPE_COLLECTION);
                    menCollection.setSaleProductGroupUid(productGroupDao.insert(menCollection));
                    SaleProductGroupJoin menCollection1 =
                            new SaleProductGroupJoin(mensBelt.getSaleProductUid(),
                                    menCollection.getSaleProductGroupUid());
                    productGroupJoinDao.insert(menCollection1);

                    SaleProductGroup clutches = new SaleProductGroup("Accessories");
                    clutches.setSaleProductGroupType(SaleProductGroup.PRODUCT_GROUP_TYPE_COLLECTION);
                    clutches.setSaleProductGroupUid(productGroupDao.insert(clutches));
                    SaleProductGroupJoin accessoriesJoin =
                            new SaleProductGroupJoin(beigeClutch.getSaleProductUid(),
                                    clutches.getSaleProductGroupUid());
                    productGroupJoinDao.insert(accessoriesJoin);
                }

                String pinkHatSaleTitle = "20x Pink Hat";
                SaleProduct finalPinkHatProduct = pinkHatProduct;
                saleDao.findAllSaleWithTitleAsync(pinkHatSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()) {
                            Sale pinkHatSale = new Sale();
                            pinkHatSale.setSaleTitle(pinkHatSaleTitle);
                            pinkHatSale.setSaleActive(true);
                            pinkHatSale.setSaleCancelled(false);
                            pinkHatSale.setSalePreOrder(false);
                            pinkHatSale.setSaleDone(true);
                            pinkHatSale.setSalePaymentDone(false);
                            pinkHatSale.setSaleLocationUid(newLocation.getLocationUid());
                            pinkHatSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(0));
                            pinkHatSale.setSaleDueDate(0);
                            pinkHatSale.setSaleUid(saleDao.insert(pinkHatSale));

                            //Insert items.
                            SaleItem thisSaleItem = new SaleItem(finalPinkHatProduct.getSaleProductUid(),
                                    20, 500, pinkHatSale.getSaleUid(),
                                    UMCalendarUtil.getDateInMilliPlusDays(-1));
                            saleItemDao.insert(thisSaleItem);



                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {exception.printStackTrace();}
                });

                
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }
}
