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
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.db.dao.SaleProductPictureDao;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.OnBoardingView;
import com.ustadmobile.lib.db.entities.DashboardEntry;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductGroup;
import com.ustadmobile.lib.db.entities.SaleProductGroupJoin;
import com.ustadmobile.lib.db.entities.SaleProductParentJoin;
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
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(OnBoardingView.VIEW_NAME, null, getContext());
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
        SaleProductParentJoinDao productParentJoinDao = repo.getSaleProductParentJoinDao();
        PersonDao personDao = repo.getPersonDao();



        //Create a new location
        locationDao.findByTitleAsync("Test location", new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> result) {
                Location newLocation;
                if (result.isEmpty()) {
                    newLocation = new Location();
                    newLocation.setTitle("Test location");
                    newLocation.setParentLocationUid(0);
                    newLocation.setLocationActive(false);
                    newLocation.setDescription("Test location added from Dummy data");
                    newLocation.setLocationUid(locationDao.insert(newLocation));

                    //Add more locations
                    Location afg = new Location("Afghanistan",
                            "Afghanistan whole region", true);
                    long afgLocationUid = locationDao.insert(afg);

                    Location centralAfg = new Location("Central Afghanistan", "Center region", true, afgLocationUid);
                    long centralAfgLocationUid = locationDao.insert(centralAfg);
                    Location easternAfg = new Location("Eastern Afghanistan", "Eastern region", true, afgLocationUid);
                    locationDao.insert(new Location("Kabul Province",
                            "Kabul area", true, centralAfgLocationUid));

                    long easternAfgLocationUid = locationDao.insert(easternAfg);
                    Location northernAfg= new Location("Northern Afghanistan", "Northern region", true, afgLocationUid);
                    long northernAfgLocationUid = locationDao.insert(northernAfg);
                    Location westernAfg = new Location("Western Afghanistan", "Western region", true, afgLocationUid);
                    locationDao.insert(new Location("Kunduz Province",
                            "Kunduz area", true, northernAfgLocationUid));

                    long westernAfgLocationUid = locationDao.insert(westernAfg);
                    Location southeastAfg = new Location("Southeast Afghanistan", "Southeast region", true, afgLocationUid);
                    locationDao.insert(new Location("Herat Province",
                            "Herat area", true, westernAfgLocationUid));
                    long southeastAfgLocationUid = locationDao.insert(southeastAfg);
                    locationDao.insert(new Location("Khost Province",
                            "Khost area", true, southeastAfgLocationUid));
                    locationDao.insert(new Location("Paktika Province",
                            "Paktika area", true, southeastAfgLocationUid));

                    Location southWestAfg = new Location("Southwest Afghanistan",
                            "Southwest region", true, afgLocationUid);
                    long southWestAfgLocationUid = locationDao.insert(southWestAfg);

                    //Also add some Producers (People)

                    Person person1 = new Person("royarahimi", "Roya",
                            "Rahimi");
                    Person person2 = new Person("lailagulzar", "Laila",
                        "Gulzar");
                    Person person3 = new Person("meenahotaki", "Meena",
                            "Hotaki");
                    Person person4 = new Person("nargisyousafzi","Nargis",
                            "Yousafzi");

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


                    SaleProduct  brownBag = new SaleProduct("Brown Bag", "Dummy data");
                    brownBag.setSaleProductUid(saleProductDao.insert(brownBag));
                    addPicToProduct(brownBag,
                            "goldozi/goldozi_product_placeholder_bag.jpg", repo);


                    SaleProduct blueClutch = new SaleProduct("Blue Clutch", "Dummy data");
                    blueClutch.setSaleProductUid(saleProductDao.insert(blueClutch));
                    addPicToProduct(blueClutch,
                            "goldozi/goldozi_product_placeholder_blue_clutch.png", repo);

                    SaleProduct bowKeychain = new SaleProduct("Bow Keychain", "Dummy data");
                    bowKeychain.setSaleProductUid(saleProductDao.insert(bowKeychain));
                    addPicToProduct(bowKeychain,
                            "goldozi/goldozi_product_placeholder_bow_keychain.jpeg", repo);

                    SaleProduct keychain = new SaleProduct("Keychain", "Dummy data");
                    keychain.setSaleProductUid(saleProductDao.insert(keychain));
                    addPicToProduct(keychain,
                            "goldozi/goldozi_product_placeholder_keychain.jpg", repo);

                    SaleProduct keychains = new SaleProduct("Keychains", "Dummy data");
                    keychains.setSaleProductUid(saleProductDao.insert(keychains));
                    addPicToProduct(keychains,
                            "goldozi/goldozi_product_placeholder_keychains.jpg", repo);

                    SaleProduct lightBlueClutch = new SaleProduct("Light Blue Clutch", "Dummy data");
                    lightBlueClutch.setSaleProductUid(saleProductDao.insert(lightBlueClutch));
                    addPicToProduct(lightBlueClutch,
                            "goldozi/goldozi_product_placeholder_light_blue_clutch.png", repo);

                    SaleProduct metallicClutch = new SaleProduct("Metallic Clutch", "Dummy data");
                    metallicClutch.setSaleProductUid(saleProductDao.insert(metallicClutch));
                    addPicToProduct(metallicClutch,
                            "goldozi/goldozi_product_placeholder_metallic_clutch.png", repo);

                    SaleProduct rubyClutch = new SaleProduct("Ruby Clutch", "Dummy data");
                    rubyClutch.setSaleProductUid(saleProductDao.insert(rubyClutch));
                    addPicToProduct(rubyClutch,
                            "goldozi/goldozi_product_placeholder_ruby_clutch.png", repo);

                    SaleProduct rubyPurse = new SaleProduct("Ruby purse", "Dummy data");
                    rubyPurse.setSaleProductUid(saleProductDao.insert(rubyPurse));
                    addPicToProduct(rubyPurse,
                            "goldozi/goldozi_product_placeholder_ruby_purse.png", repo);

                    SaleProduct rubyScarf = new SaleProduct("Ruby Scarf", "Dummy data");
                    rubyScarf.setSaleProductUid(saleProductDao.insert(rubyScarf));
                    addPicToProduct(rubyScarf,
                            "goldozi/goldozi_product_placeholder_ruby_scarf.jpg", repo);

                    SaleProduct laceholderRubyScarf = new SaleProduct("Laceholder Ruby Scarf", "Dummy data");
                    laceholderRubyScarf.setSaleProductUid(saleProductDao.insert(laceholderRubyScarf));
                    addPicToProduct(laceholderRubyScarf,
                            "goldozi/goldozi_product_placeholder_scarves.jpg", repo);

                    SaleProduct turquoiseClutch = new SaleProduct("Turquoise Clutch", "Dummy data");
                    turquoiseClutch.setSaleProductUid(saleProductDao.insert(turquoiseClutch));
                    addPicToProduct(turquoiseClutch,
                            "goldozi/goldozi_product_placeholder_turquoise_clutch.png", repo);

                    SaleProduct turquoisePurse = new SaleProduct("Turquoise Purse", "Dummy data");
                    turquoisePurse.setSaleProductUid(saleProductDao.insert(turquoisePurse));
                    addPicToProduct(turquoisePurse,
                            "goldozi/goldozi_product_placeholder_turquoise_purse .png", repo);

                    SaleProduct yellowClutch = new SaleProduct("Yellow Clutch", "Dummy data");
                    yellowClutch.setSaleProductUid(saleProductDao.insert(yellowClutch));
                    addPicToProduct(yellowClutch,
                            "goldozi/goldozi_product_placeholder_yellow_clutch.png", repo);

                    SaleProduct yellowPurse = new SaleProduct("Yellow Purse", "Dummy data");
                    yellowPurse.setSaleProductUid(saleProductDao.insert(yellowPurse));
                    addPicToProduct(yellowPurse,
                            "goldozi/goldozi_product_placeholder_yellow_purse.png", repo);



                    //Create categories (NEW)

                    SaleProduct bedLinenCategry = new SaleProduct("Bed Linen", "Dummy data", true);
                    bedLinenCategry.setSaleProductUid(saleProductDao.insert(bedLinenCategry));
                    addPicToProduct(bedLinenCategry,
                            "goldozi/goldozi_product_placeholder_bed_linen.png", repo);

                    SaleProduct toysCategory = new SaleProduct("Toys", "Dummy data", true);
                    toysCategory.setSaleProductUid(saleProductDao.insert(toysCategory));
                    addPicToProduct(toysCategory,
                            "goldozi/goldozi_product_placeholder_toys.png", repo);

                    SaleProduct accessoriesCategory = new SaleProduct("Accessories", "Dummy data", true);
                    accessoriesCategory.setSaleProductUid(saleProductDao.insert(accessoriesCategory));
                    addPicToProduct(accessoriesCategory,
                            "goldozi/goldozi_product_placeholder_accessories.png", repo);


                    //Create joins to categories (new)
                    SaleProductParentJoin bedLinenJoin =
                            new SaleProductParentJoin(pinkBedLinen.getSaleProductUid(),
                                    bedLinenCategry.getSaleProductUid(), true);
                    bedLinenJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(bedLinenJoin));

                    SaleProductParentJoin accessoriesJoin =
                            new SaleProductParentJoin(beigeClutch.getSaleProductUid(),
                                    accessoriesCategory.getSaleProductUid(), true);
                    accessoriesJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(accessoriesJoin));

                    SaleProductParentJoin toysJoin =
                            new SaleProductParentJoin(zariToy.getSaleProductUid(),
                                    toysCategory.getSaleProductUid(), true);
                    toysJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(toysJoin));

                    //two level categories
                    SaleProduct categoryclutches = new SaleProduct("Clutches", "All clutches", true);
                    categoryclutches.setSaleProductUid(saleProductDao.insert(categoryclutches));
                    addPicToProduct(categoryclutches, "goldozi/goldozi_product_placeholder_ruby_clutch.png", repo);

                    SaleProduct pursesCategory = new SaleProduct("Purses", "All purses", true);
                    pursesCategory.setSaleProductUid(saleProductDao.insert(pursesCategory));
                    addPicToProduct(pursesCategory, "goldozi/goldozi_product_placeholder_ruby_purse.png", repo);

                    SaleProduct scarvesCategory = new SaleProduct("Scarves", "All scarves", true);
                    scarvesCategory.setSaleProductUid(saleProductDao.insert(scarvesCategory));
                    addPicToProduct(scarvesCategory, "goldozi/goldozi_product_placeholder_ruby_scarf.jpg", repo);

                    SaleProductParentJoin clutchAccessoriesJoin =
                            new SaleProductParentJoin(categoryclutches.getSaleProductUid(),
                                    accessoriesCategory.getSaleProductUid(), true);
                    clutchAccessoriesJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(clutchAccessoriesJoin));

                    SaleProductParentJoin pursesAccessoriesJoin =
                            new SaleProductParentJoin(pursesCategory.getSaleProductUid(),
                                    accessoriesCategory.getSaleProductUid(), true);
                    pursesAccessoriesJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(pursesAccessoriesJoin));

                    SaleProductParentJoin scarfAccessoriesJoin =
                            new SaleProductParentJoin(scarvesCategory.getSaleProductUid(),
                                    accessoriesCategory.getSaleProductUid(), true);
                    scarfAccessoriesJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(scarfAccessoriesJoin));


                    //Create collections new
                    SaleProduct womenCategory = new SaleProduct("Women", "All women products", true);
                    womenCategory.setSaleProductUid(saleProductDao.insert(womenCategory));
                    addPicToProduct(womenCategory, "goldozi/goldozi_product_placeholder_women.png", repo);

                    SaleProduct menCategory = new SaleProduct("Men", "All men products", true);
                    menCategory.setSaleProductUid(saleProductDao.insert(menCategory));
                    addPicToProduct(menCategory, "goldozi/goldozi_product_placeholder_men.png", repo);

                    SaleProductParentJoin suitWomenJoin =
                            new SaleProductParentJoin(womenSuit.getSaleProductUid(),
                                    womenCategory.getSaleProductUid(), true);
                    suitWomenJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(suitWomenJoin));

                    SaleProductParentJoin beltMenJoin =
                            new SaleProductParentJoin(mensBelt.getSaleProductUid(),
                                    menCategory.getSaleProductUid(), true);
                    beltMenJoin.setSaleProductParentJoinUid(productParentJoinDao.insert(beltMenJoin));


                    //Create collection (new)
                    SaleProduct collectionCategory = new SaleProduct("Collection",
                            "All collections", true);
                    collectionCategory.setSaleProductUid(saleProductDao.insert(collectionCategory));
                    addPicToProduct(collectionCategory, "", repo);

                    SaleProductParentJoin womenCategoryCollectionJoin =
                            new SaleProductParentJoin(womenCategory.getSaleProductUid(),
                                    collectionCategory.getSaleProductUid(), true);
                    womenCategoryCollectionJoin.setSaleProductParentJoinUid(
                            productParentJoinDao.insert(womenCategoryCollectionJoin));

                    SaleProductParentJoin menCategoryCollectionJoin =
                            new SaleProductParentJoin(menCategory.getSaleProductUid(),
                                    collectionCategory.getSaleProductUid(), true);
                    menCategoryCollectionJoin.setSaleProductParentJoinUid(
                            productParentJoinDao.insert(menCategoryCollectionJoin));

                    SaleProductParentJoin accessoriesCategoryCollectionJoin =
                            new SaleProductParentJoin(accessoriesCategory.getSaleProductUid(),
                                    collectionCategory.getSaleProductUid(), true);
                    accessoriesCategoryCollectionJoin.setSaleProductParentJoinUid(
                            productParentJoinDao.insert(accessoriesCategoryCollectionJoin));
                }


                
            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }
}
