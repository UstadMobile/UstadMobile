package com.ustadmobile.port.rest;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.SaleProductDao;
import com.ustadmobile.core.db.dao.SaleProductParentJoinDao;
import com.ustadmobile.core.db.dao.SaleProductPictureDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.SaleProduct;
import com.ustadmobile.lib.db.entities.SaleProductParentJoin;
import com.ustadmobile.lib.db.entities.SaleProductPicture;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener {
    private String dummyBaseUrl = "http://localhost/dummy/address/";
    private String dummyAuth = "dummy";
    private UmAppDatabase appDb;

    private PersonCustomFieldDao personCustomFieldDao;
    private PersonDao personDao;
    private LocationDao locationDao;
    private PersonAuthDao personAuthDao;
    private PersonGroupMemberDao personGroupMemberDao;
    private SaleProductDao saleProductDao;
    private SaleProductParentJoinDao productParentJoinDao;
    private UmAppDatabase repo;

    private ServletContext ctx;


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent evt) {
        System.out.println("\nServletContextListener started");

        ctx = evt.getServletContext();

        appDb = UmAppDatabase.getInstance(evt.getServletContext());
        appDb.setAttachmentsDir(evt.getServletContext().getRealPath("/WEB-INF/attachments/"));

        repo = appDb.getRepository(dummyBaseUrl, dummyAuth);

        personDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonDao();
        locationDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getLocationDao();
        personAuthDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonAuthDao();
        personGroupMemberDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonGroupMemberDao();
        saleProductDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getSaleProductDao();
        productParentJoinDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getSaleProductParentJoinDao();

        //Load initial data
        loadInitialData();

    }

    private void loadInitialData(){
        //Any data goes here.

        //Create Admin
        Person adminPerson = personDao.findByUsername("admin");
        if(adminPerson == null) {
            adminPerson = new Person();
            adminPerson.setAdmin(true);
            adminPerson.setUsername("admin");
            adminPerson.setFirstNames("Admin");
            adminPerson.setLastName("Admin");
            adminPerson.setActive(true);

            adminPerson.setPersonUid(personDao.insert(adminPerson));

            PersonAuth adminPersonAuth = new PersonAuth(adminPerson.getPersonUid(),
                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                            PersonAuthDao.encryptPassword("golDoz1"));
            personAuthDao.insertAsync(adminPersonAuth, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    //Admin created.
                    System.out.println("ServletContextClass: Admin created. Continuing..");
                    addDummyData();
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }else {
            System.out.println("ServletContextClass: Admin Already created. Continuing..");
        }
    }

    private void addDummyData(){

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

                    try {
                        //AFGHANISTAN
                        Location afg = new Location(
                                new String("افغانستان".getBytes(), "UTF-8"),
                                "Afghanistan whole region", true);
                        long afgLocationUid = locationDao.insert(afg);

                        //CENTRAL AFGHANISTAN
                        Location centralAfg = new Location(
                                new String("(Central Afghanistan)".getBytes(), "UTF-8"),
                                "Center region", true, afgLocationUid);
                        long centralAfgLocationUid = locationDao.insert(centralAfg);

                        locationDao.insert(new Location(
                                new String("کابل (Kabul)".getBytes(), "UTF-8"),
                                "Kabul area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("بامیان (Bamyan)".getBytes(), "UTF-8"),
                                "Bamyan area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Kapisa) كابيسا ".getBytes(), "UTF-8"),
                                "Kapisa area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Logar) لوغار ".getBytes(), "UTF-8"),
                                "Logar area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Parwan) پروان ".getBytes(), "UTF-8"),
                                "Parwan area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Urozgan) اروزكان ".getBytes(), "UTF-8"),
                                "Urozgan area", true, centralAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Wardak) وردک".getBytes(), "UTF-8"),
                                "Wardak area", true, centralAfgLocationUid));

                        //EASTERN AFGHANISTAN
                        Location easternAfg = new Location(
                                new String("(Eastern Afghanistan)".getBytes(), "UTF-8"),
                                "Eastern region", true, afgLocationUid);
                        long easternAfgUid = locationDao.insert(easternAfg);
                        locationDao.insert(new Location(
                                new String(" (Laghman) لغمان ".getBytes(), "UTF-8"),
                                "Laghman area", true, easternAfgUid));
                        locationDao.insert(new Location(
                                new String("(Nangarhar) ننګرهار ".getBytes(), "UTF-8"),
                                "Nangarhar area", true, easternAfgUid));

                        //NORTH EAST AFGHANISTAN
                        Location northEastAfg= new Location(
                                new String(" (North East Afghanistan)".getBytes(), "UTF-8"),
                                "Northern region", true, afgLocationUid);
                        long northEastAfgLocationUid = locationDao.insert(northEastAfg);
                        locationDao.insert(new Location(
                                new String(" (Badakhshan  Province) بدخشان".getBytes(), "UTF-8"),
                                "Badakhshan  area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String(" (Baghlan Province) بغلان".getBytes(), "UTF-8"),
                                "Baghlan area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String(" (Kunar Province) کونړ".getBytes(), "UTF-8"),
                                "Kunar area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Kunduz Province) کندوز".getBytes(), "UTF-8"),
                                "Kunduz area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String(" (Nuristan Province) نورستان".getBytes(), "UTF-8"),
                                "Nuristan area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Panjshir Province) پنجشیر".getBytes(), "UTF-8"),
                                "Panjshir area", true, northEastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Takhar Province) تخار".getBytes(), "UTF-8"),
                                "Takhar area", true, northEastAfgLocationUid));

                        //NORTH WEST AFGHANISTAN
                        Location northWestAfg= new Location(
                                new String("(North East Afghanistan)".getBytes(), "UTF-8"),
                                "Northern region", true, afgLocationUid);
                        long northWestAfgLocationUid = locationDao.insert(northWestAfg);
                        locationDao.insert(new Location(
                                new String("(Balkh  Province) بلخ".getBytes(), "UTF-8"),
                                "Balkh  area", true, northWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Faryab  Province) فارياب".getBytes(), "UTF-8"),
                                "Faryab  area", true, northWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Jowzjan  Province) جوزجان".getBytes(), "UTF-8"),
                                "Jowzjan area", true, northWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Samangan  Province) سمنگان".getBytes(), "UTF-8"),
                                "Samangan  area", true, northWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Sar-e Pol  Province) سرپل".getBytes(), "UTF-8"),
                                "Sar-e Pol  area", true, northWestAfgLocationUid));


                        //SOUTHEAST AFGHANISTAN
                        Location southeastAfg = new Location(
                                new String("(Southeast Afghanistan)".getBytes(), "UTF-8"),
                                "Southeast region", true, afgLocationUid);
                        long southeastAfgLocationUid = locationDao.insert(southeastAfg);
                        locationDao.insert(new Location(
                                new String("(Khost Province) خوست".getBytes(), "UTF-8"),
                                "Khost area", true, southeastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Paktika Province) پکتیکا".getBytes(), "UTF-8"),
                                "Paktika area", true, southeastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Ghazni Province) غزنى".getBytes(), "UTF-8"),
                                "Ghazni area", true, southeastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Kandahar Province) کندهار ".getBytes(), "UTF-8"),
                                "Kandahar area", true, southeastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Pakika Province) پکتیا".getBytes(), "UTF-8"),
                                "Pakika area", true, southeastAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Zabul Province) زابل".getBytes(), "UTF-8"),
                                "Zabul area", true, southeastAfgLocationUid));

                        //SOUTHWEST AFGHANISTAN
                        Location southWestAfg = new Location(
                                new String("(Southwest Afghanistan)".getBytes(), "UTF-8"),
                                "Southwest region", true, afgLocationUid);
                        long southWestAfgLocationUid = locationDao.insert(southWestAfg);
                        locationDao.insert(new Location(
                                new String("(Daykundi Province) دایکندی".getBytes(), "UTF-8"),
                                "Daykundi area", true, southWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Helmand Province) هلمند".getBytes(), "UTF-8"),
                                "Helmand area", true, southWestAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Nimruz Province) نيمروز".getBytes(), "UTF-8"),
                                "Nimruz area", true, southWestAfgLocationUid));


                        //WESTERN AFGHANISTAN
                        Location westernAfg = new Location(
                                "(Western Afghanistan)", "Western region", true, afgLocationUid);
                        long westernAfgLocationUid = locationDao.insert(westernAfg);
                        locationDao.insert(new Location(
                                new String("(Herat Province) هرات".getBytes(), "UTF-8"),
                                "Herat area", true, westernAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Farah Province) فارا".getBytes(), "UTF-8"),
                                "Farah area", true, westernAfgLocationUid));
                        locationDao.insert(new Location(
                                new String("(Ghor Province) غور".getBytes(), "UTF-8"),
                                "Ghor area", true, westernAfgLocationUid));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
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

                    addSaleProducts();
                }

            }

            @Override
            public void onFailure(Throwable exception) {exception.printStackTrace();}
        });

    }

    private void addSaleProducts(){

        //Insert products
        String pinkHatName = "Pink Hat";
        saleProductDao.findByNameAsync(pinkHatName, new UmCallback<SaleProduct>() {
            @Override
            public void onSuccess(SaleProduct pinkHatProduct) {

                if(pinkHatProduct == null){
                    pinkHatProduct = new SaleProduct(pinkHatName, "Testing dummy data");
                    pinkHatProduct.setSaleProductUid(saleProductDao.insert(pinkHatProduct));
                    addPicToProduct(pinkHatProduct,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_pink_hats.png", repo);

                    //Create other products
                    SaleProduct greenSheets = new SaleProduct("Green Sheet", "Testing dummy data");
                    greenSheets.setSaleProductUid(saleProductDao.insert(greenSheets));
                    addPicToProduct(greenSheets,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_green_sheets.png", repo);

                    SaleProduct floralScarves  = new SaleProduct("Floral Scarf", "Dummy data");
                    floralScarves.setSaleProductUid(saleProductDao.insert(floralScarves));
                    addPicToProduct(floralScarves,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_floral_scarves.png", repo);

                    SaleProduct pinkBedLinen = new SaleProduct("Pink Bed Linen", "Dummy data");
                    pinkBedLinen.setSaleProductUid(saleProductDao.insert(pinkBedLinen));
                    addPicToProduct(pinkBedLinen,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_bed_linen.png", repo);

                    SaleProduct redTableLinen = new SaleProduct("Red Table Linen", "Dummy data");
                    redTableLinen.setSaleProductUid(saleProductDao.insert(redTableLinen));
                    addPicToProduct(redTableLinen,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_table_linen.png", repo);

                    SaleProduct zariToy = new SaleProduct("Zari Bagchesimsim", "Dummy data");
                    zariToy.setSaleProductUid(saleProductDao.insert(zariToy));
                    addPicToProduct(zariToy,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_toys.png", repo);

                    SaleProduct womenSuit  = new SaleProduct("Women Suit", "Dummy data");
                    womenSuit.setSaleProductUid(saleProductDao.insert(womenSuit));
                    addPicToProduct(womenSuit,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_women.png", repo);

                    SaleProduct mensBelt  = new SaleProduct("Men Belt", "Dummy data");
                    mensBelt.setSaleProductUid(saleProductDao.insert(mensBelt));
                    addPicToProduct(mensBelt,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_men.png", repo);

                    SaleProduct beigeClutch  = new SaleProduct("Beige Clutch", "Dummy data");
                    beigeClutch.setSaleProductUid(saleProductDao.insert(beigeClutch));
                    addPicToProduct(beigeClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_accessories.png", repo);


                    SaleProduct  brownBag = new SaleProduct("Brown Bag", "Dummy data");
                    brownBag.setSaleProductUid(saleProductDao.insert(brownBag));
                    addPicToProduct(brownBag,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_bag.jpg", repo);


                    SaleProduct blueClutch = new SaleProduct("Blue Clutch", "Dummy data");
                    blueClutch.setSaleProductUid(saleProductDao.insert(blueClutch));
                    addPicToProduct(blueClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_blue_clutch.png", repo);

                    SaleProduct bowKeychain = new SaleProduct("Bow Keychain", "Dummy data");
                    bowKeychain.setSaleProductUid(saleProductDao.insert(bowKeychain));
                    addPicToProduct(bowKeychain,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_bow_keychain.jpeg", repo);

                    SaleProduct keychain = new SaleProduct("Keychain", "Dummy data");
                    keychain.setSaleProductUid(saleProductDao.insert(keychain));
                    addPicToProduct(keychain,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_keychain.jpg", repo);

                    SaleProduct keychains = new SaleProduct("Keychains", "Dummy data");
                    keychains.setSaleProductUid(saleProductDao.insert(keychains));
                    addPicToProduct(keychains,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_keychains.jpg", repo);

                    SaleProduct lightBlueClutch = new SaleProduct("Light Blue Clutch", "Dummy data");
                    lightBlueClutch.setSaleProductUid(saleProductDao.insert(lightBlueClutch));
                    addPicToProduct(lightBlueClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_light_blue_clutch.png", repo);

                    SaleProduct metallicClutch = new SaleProduct("Metallic Clutch", "Dummy data");
                    metallicClutch.setSaleProductUid(saleProductDao.insert(metallicClutch));
                    addPicToProduct(metallicClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_metallic_clutch.png", repo);

                    SaleProduct rubyClutch = new SaleProduct("Ruby Clutch", "Dummy data");
                    rubyClutch.setSaleProductUid(saleProductDao.insert(rubyClutch));
                    addPicToProduct(rubyClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_clutch.png", repo);

                    SaleProduct rubyPurse = new SaleProduct("Ruby purse", "Dummy data");
                    rubyPurse.setSaleProductUid(saleProductDao.insert(rubyPurse));
                    addPicToProduct(rubyPurse,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_purse.png", repo);

                    SaleProduct rubyScarf = new SaleProduct("Ruby Scarf", "Dummy data");
                    rubyScarf.setSaleProductUid(saleProductDao.insert(rubyScarf));
                    addPicToProduct(rubyScarf,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_scarf.jpg", repo);

                    SaleProduct laceholderRubyScarf = new SaleProduct("Laceholder Ruby Scarf", "Dummy data");
                    laceholderRubyScarf.setSaleProductUid(saleProductDao.insert(laceholderRubyScarf));
                    addPicToProduct(laceholderRubyScarf,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_scarves.jpg", repo);

                    SaleProduct turquoiseClutch = new SaleProduct("Turquoise Clutch", "Dummy data");
                    turquoiseClutch.setSaleProductUid(saleProductDao.insert(turquoiseClutch));
                    addPicToProduct(turquoiseClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_turquoise_clutch.png", repo);

                    SaleProduct turquoisePurse = new SaleProduct("Turquoise Purse", "Dummy data");
                    turquoisePurse.setSaleProductUid(saleProductDao.insert(turquoisePurse));
                    addPicToProduct(turquoisePurse,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_turquoise_purse .png", repo);

                    SaleProduct yellowClutch = new SaleProduct("Yellow Clutch", "Dummy data");
                    yellowClutch.setSaleProductUid(saleProductDao.insert(yellowClutch));
                    addPicToProduct(yellowClutch,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_yellow_clutch.png", repo);

                    SaleProduct yellowPurse = new SaleProduct("Yellow Purse", "Dummy data");
                    yellowPurse.setSaleProductUid(saleProductDao.insert(yellowPurse));
                    addPicToProduct(yellowPurse,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_yellow_purse.png", repo);


                    //Create categories (Joins) (NEW)

                    SaleProduct bedLinenCategry = new SaleProduct("Bed Linen", "Dummy data", true);
                    bedLinenCategry.setSaleProductUid(saleProductDao.insert(bedLinenCategry));
                    addPicToProduct(bedLinenCategry,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_bed_linen.png", repo);

                    SaleProduct toysCategory = new SaleProduct("Toys", "Dummy data", true);
                    toysCategory.setSaleProductUid(saleProductDao.insert(toysCategory));
                    addPicToProduct(toysCategory,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_toys.png", repo);

                    SaleProduct accessoriesCategory = new SaleProduct("Accessories", "Dummy data", true);
                    accessoriesCategory.setSaleProductUid(saleProductDao.insert(accessoriesCategory));
                    addPicToProduct(accessoriesCategory,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_accessories.png", repo);


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
                    addPicToProduct(categoryclutches, "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_clutch.png", repo);

                    SaleProduct pursesCategory = new SaleProduct("Purses", "All purses", true);
                    pursesCategory.setSaleProductUid(saleProductDao.insert(pursesCategory));
                    addPicToProduct(pursesCategory, "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_purse.png", repo);

                    SaleProduct scarvesCategory = new SaleProduct("Scarves", "All scarves", true);
                    scarvesCategory.setSaleProductUid(saleProductDao.insert(scarvesCategory));
                    addPicToProduct(scarvesCategory, "/WEB-INF/goldozi/goldozi_product_placeholder_ruby_scarf.jpg", repo);

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
                    addPicToProduct(womenCategory, "/WEB-INF/goldozi/goldozi_product_placeholder_women.png", repo);

                    SaleProduct menCategory = new SaleProduct("Men", "All men products", true);
                    menCategory.setSaleProductUid(saleProductDao.insert(menCategory));
                    addPicToProduct(menCategory, "/WEB-INF/goldozi/goldozi_product_placeholder_men.png", repo);

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
                    addPicToProduct(collectionCategory,
                            "/WEB-INF/goldozi/goldozi_product_placeholder_accessories.png", repo);

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

    private void addPicToProduct(SaleProduct product, String assetPath, UmAppDatabase repo){
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
                    assetIS = ctx.getResourceAsStream(assetPath);

                    pictureDao.setAttachment(productPictureUid, assetIS);
                    if(assetIS == null){
                        System.out.println("Null Image stream. Check url");
                    }else {
                        System.out.println("Added SaleProductPic OK.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ee){
                    System.out.println("Unable to process: " + product.getSaleProductName());
                    ee.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


}
