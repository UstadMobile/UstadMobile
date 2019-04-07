package com.ustadmobile.core.util;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.SaleDao;
import com.ustadmobile.core.db.dao.SaleItemDao;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductGroupDao;
import com.ustadmobile.core.db.dao.SaleProductGroupJoinDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.Sale;
import com.ustadmobile.lib.db.entities.SaleItem;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductGroup;
import com.ustadmobile.lib.db.entities.SaleProductGroupJoin;

import java.util.List;

public class SetUpUtil {

    public static void addDummyData(UmAppDatabase repo){

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

                    //Create other products
                    SaleProduct greenSheets = new SaleProduct("Green sheets", "Testing dummy data");
                    greenSheets.setSaleProductUid(saleProductDao.insert(greenSheets));

                    SaleProduct floralScarves  = new SaleProduct("Floral scarves", "Dummy data");
                    floralScarves.setSaleProductUid(saleProductDao.insert(floralScarves));

                    SaleProduct blueSheets = new SaleProduct("Blue sheets", "Dummy data");
                    SaleProduct yellowCat = new SaleProduct("Yello cat", "Dummy data");
                    SaleProduct redNotebook = new SaleProduct("Red notebook", "Dummy data");

                    blueSheets.setSaleProductUid(saleProductDao.insert(blueSheets));
                    yellowCat.setSaleProductUid(saleProductDao.insert(yellowCat));
                    redNotebook.setSaleProductUid(saleProductDao.insert(redNotebook));

                    SaleProduct pinkBedLinen = new SaleProduct("Pink Bed linen", "Dummy data");
                    SaleProduct redTableLinen = new SaleProduct("Red table linen", "Dummy data");
                    SaleProduct zariToy = new SaleProduct("Zari Bagchesimsim", "Dummy data");
                    SaleProduct yellowHat = new SaleProduct("Yello hat", "Dummy data");

                    pinkBedLinen.setSaleProductUid(saleProductDao.insert(pinkBedLinen));
                    redTableLinen.setSaleProductUid(saleProductDao.insert(redTableLinen));
                    zariToy.setSaleProductUid(saleProductDao.insert(zariToy));
                    yellowHat.setSaleProductUid(saleProductDao.insert(yellowHat));


                    //Create categories
                    SaleProductGroup bedLinenGroup = new SaleProductGroup("Bed linen");
                    SaleProductGroup tableLinenGroup = new SaleProductGroup("Table linen");
                    SaleProductGroup toyGroup = new SaleProductGroup("Toys");
                    SaleProductGroup hatGroup = new SaleProductGroup("Hats");
                    SaleProductGroup catGroup = new SaleProductGroup("Cats");

                    bedLinenGroup.setSaleProductGroupUid(productGroupDao.insert(bedLinenGroup));
                    tableLinenGroup.setSaleProductGroupUid(productGroupDao.insert(tableLinenGroup));
                    toyGroup.setSaleProductGroupUid(productGroupDao.insert(toyGroup));
                    hatGroup.setSaleProductGroupUid(productGroupDao.insert(hatGroup));
                    catGroup.setSaleProductGroupUid(productGroupDao.insert(catGroup));

                    //Assign products to groups
                    SaleProductGroupJoin bedLinen1 =
                            new SaleProductGroupJoin(pinkBedLinen.getSaleProductUid(),
                                    bedLinenGroup.getSaleProductGroupUid());
                    SaleProductGroupJoin tableLinen1 =
                            new SaleProductGroupJoin(redTableLinen.getSaleProductUid(),
                                    tableLinenGroup.getSaleProductGroupUid());
                    SaleProductGroupJoin toy1 =
                            new SaleProductGroupJoin(zariToy.getSaleProductUid(),
                                    toyGroup.getSaleProductGroupUid());
                    SaleProductGroupJoin hat1 =
                            new SaleProductGroupJoin(yellowHat.getSaleProductUid(),
                                    hatGroup.getSaleProductGroupUid());
                    SaleProductGroupJoin cat1 =
                            new SaleProductGroupJoin(yellowCat.getSaleProductUid(),
                                    catGroup.getSaleProductGroupUid());

                    productGroupJoinDao.insert(bedLinen1);
                    productGroupJoinDao.insert(tableLinen1);
                    productGroupJoinDao.insert(toy1);
                    productGroupJoinDao.insert(hat1);
                    productGroupJoinDao.insert(cat1);

                }

                String pinkHatSaleTitle = "20x pink hat";
                SaleProduct finalPinkHatProduct = pinkHatProduct;
                saleDao.findAllSaleWithTitleAsync(pinkHatSaleTitle, new UmCallback<List<Sale>>() {
                    @Override
                    public void onSuccess(List<Sale> result) {
                        if (result.isEmpty()) {
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
                            pinkHatSale.setSaleUid(saleDao.insert(pinkHatSale));

                            //Insert items.
                            SaleItem thisSaleItem = new SaleItem(finalPinkHatProduct.getSaleProductUid(),
                                    20, 500, pinkHatSale.getSaleUid(),
                                    UMCalendarUtil.getDateInMilliPlusDays(-1));
                            saleItemDao.insert(thisSaleItem);



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
                        if (result.isEmpty()) {
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

                        if (result.isEmpty()) {
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
                        if (result.isEmpty()) {
                            Sale shawlSale = new Sale();
                            shawlSale.setSaleTitle(shawlSaleTitle);
                            shawlSale.setSaleActive(true);
                            shawlSale.setSaleCancelled(false);
                            shawlSale.setSaleDone(true);
                            shawlSale.setSalePreOrder(true);
                            shawlSale.setSalePaymentDone(false);
                            shawlSale.setSaleLocationUid(newLocation.getLocationUid());
                            shawlSale.setSaleCreationDate(UMCalendarUtil.getDateInMilliPlusDays(-3));
                            shawlSale.setSaleDueDate(UMCalendarUtil.getDateInMilliPlusDays(-2));
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
                        if (result.isEmpty()) {
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
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }
}
