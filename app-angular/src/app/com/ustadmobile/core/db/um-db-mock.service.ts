import { Injectable } from '@angular/core';
import util from 'UstadMobile-lib-util';
import db from 'UstadMobile-lib-database';
import door from 'UstadMobile-lib-door-runtime'; 
import {UmAngularUtil} from "../../util/UmAngularUtil";

@Injectable({
  providedIn: 'root'
})
export class UmDbMockService extends db.com.ustadmobile.core.db.UmAppDatabase {
  ROOT_UID = 1311236;
  private initialized: boolean = false;
  constructor() {
    super()
    if (!this.initialized) {
      this.initialized = true;
      db.com.ustadmobile.core.db.UmAppDatabase.Companion.setInstance(this);
    }
  }

  contentEntryDao = new ContentEntryDao();
  contentEntryStatusDao = new ContentEntryStatusDao();
  contentEntryRelatedEntryJoinDao = new ContentEntryRelatedEntryJoinDao();
  containerDao = new ContainerDao();
  networkNodeDao = new NetworkNodeDao();

  getData(entryUid) {
    const data: ContentEntry[] = entryList[entryUid];
    return data;
  }
}

/**DAO */
class ContentEntryDao {
  constructor() {}
  getChildrenByParentUidWithCategoryFilter(entryUid, language, category): any {
    var entries = entryList[entryUid] as ContentEntry[];
    if(language != 0){
      entries = entries.splice(0,entries.length - 2);
    }
    if(category != 0){
      entries = entries.splice(0,entries.length - 3);
    }
    return UmAngularUtil.createObserver(entries);
  }

  getContentByUuidAsync(entryUid) {
    return UmAngularUtil.findObjectByLabel(entryList, 'contentEntryUid', entryUid) as ContentEntry;
  }

  findUniqueLanguagesInListAsync(entryUid) {
    return util.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(languages[entryUid]);
  }

  findByUidAsync(entryUid) {
    return UmAngularUtil.findObjectByLabel(entryList, 'contentEntryUid', entryUid) as ContentEntry;
  }


  findListOfCategoriesAsync(entryUid) {
    const schemas: DistinctCategorySchema[] = [
      {
        contentCategoryUid: 1,
        categoryName: "Category Name 1",
        contentCategorySchemaUid: 12,
        schemaName: "Schema"
      },
      {
        contentCategoryUid: 2,
        categoryName: "Category Name 2",
        contentCategorySchemaUid: 12,
        schemaName: "Schema"
      },
      {
        contentCategoryUid: 3,
        categoryName: "Category Name 3",
        contentCategorySchemaUid: 12,
        schemaName: "Schema"
      }
    ];
    return util.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList([schemas])
  }
}

class ContainerDao{
  findFilesByContentEntryUid(entryUid){
    return util.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList([]);
  }
}

class NetworkNodeDao{

}
class ContentEntryStatusDao{
  constructor() {}
  findContentEntryStatusByUid(entryUid) : door.com.ustadmobile.door.DoorLiveData {
    return UmAngularUtil.createObserver(0) 
  }
}

class ContentEntryRelatedEntryJoinDao{
  findAllTranslationsForContentEntryAsync(entryUid){
    
    var relatedEntries = [
      {
        cerejContentEntryUid: entryUid,
        cerejRelatedEntryUid: 41250,
        languageName: "Sample1"
      }
    ];
  return util.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(relatedEntries);
}
}


/**Entities */
export interface ContentEntry {
  contentEntryUid: number;
  title: string;
  description: string;
  entryId: number;
  author: string;
  publisher: string;
  licenseType: number;
  licenseName: string;
  licenseUrl: string;
  sourceUrl: string;
  thumbnailUrl: string;
  lastModified: string;
  leaf: boolean;
}

export interface Language {
  langUid: number;
  name: string;
  iso_639_1_standard: string;
  iso_639_2_standard: string;
  iso_639_3_standard: string;
  langLocalChangeSeqNum: number;
  langMasterChangeSeqNum: number;
  langLastChangedBy: number;
}
export interface DistinctCategorySchema {
  contentCategoryUid: number
  categoryName: String
  contentCategorySchemaUid: number
  schemaName: String
}

const languages = {
  "1311236": [{
    "langUid": 1,
    "name": "pellentesque at",
    "iso_639_1_standard": "dui",
    "iso_639_2_standard": "quis",
    "iso_639_3_standard": "magna",
    "langLocalChangeSeqNum": 73801,
    "langMasterChangeSeqNum": 75240,
    "langLastChangedBy": 1624
  }, {
    "langUid": 2,
    "name": "lobortis sapien",
    "iso_639_1_standard": "ut",
    "iso_639_2_standard": "natoque",
    "iso_639_3_standard": "cum",
    "langLocalChangeSeqNum": 75482,
    "langMasterChangeSeqNum": 11068,
    "langLastChangedBy": 62140
  }, {
    "langUid": 3,
    "name": "iaculis congue",
    "iso_639_1_standard": "velit",
    "iso_639_2_standard": "pede",
    "iso_639_3_standard": "metus",
    "langLocalChangeSeqNum": 75370,
    "langMasterChangeSeqNum": 50569,
    "langLastChangedBy": 93444
  }, {
    "langUid": 4,
    "name": "sit amet",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "pellentesque",
    "iso_639_3_standard": "vulputate",
    "langLocalChangeSeqNum": 39410,
    "langMasterChangeSeqNum": 46112,
    "langLastChangedBy": 82190
  }, {
    "langUid": 5,
    "name": "ut nunc",
    "iso_639_1_standard": "praesent",
    "iso_639_2_standard": "nisi",
    "iso_639_3_standard": "nam",
    "langLocalChangeSeqNum": 24164,
    "langMasterChangeSeqNum": 34629,
    "langLastChangedBy": 12748
  }, {
    "langUid": 6,
    "name": "phasellus in",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "curae",
    "iso_639_3_standard": "placerat",
    "langLocalChangeSeqNum": 7131,
    "langMasterChangeSeqNum": 6606,
    "langLastChangedBy": 97838
  }],
  "41250": [{
    "langUid": 1,
    "name": "pellentesque at",
    "iso_639_1_standard": "dui",
    "iso_639_2_standard": "quis",
    "iso_639_3_standard": "magna",
    "langLocalChangeSeqNum": 73801,
    "langMasterChangeSeqNum": 75240,
    "langLastChangedBy": 1624
  }, {
    "langUid": 2,
    "name": "lobortis sapien",
    "iso_639_1_standard": "ut",
    "iso_639_2_standard": "natoque",
    "iso_639_3_standard": "cum",
    "langLocalChangeSeqNum": 75482,
    "langMasterChangeSeqNum": 11068,
    "langLastChangedBy": 62140
  }, {
    "langUid": 3,
    "name": "iaculis congue",
    "iso_639_1_standard": "velit",
    "iso_639_2_standard": "pede",
    "iso_639_3_standard": "metus",
    "langLocalChangeSeqNum": 75370,
    "langMasterChangeSeqNum": 50569,
    "langLastChangedBy": 93444
  }, {
    "langUid": 4,
    "name": "sit amet",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "pellentesque",
    "iso_639_3_standard": "vulputate",
    "langLocalChangeSeqNum": 39410,
    "langMasterChangeSeqNum": 46112,
    "langLastChangedBy": 82190
  }, {
    "langUid": 5,
    "name": "ut nunc",
    "iso_639_1_standard": "praesent",
    "iso_639_2_standard": "nisi",
    "iso_639_3_standard": "nam",
    "langLocalChangeSeqNum": 24164,
    "langMasterChangeSeqNum": 34629,
    "langLastChangedBy": 12748
  }, {
    "langUid": 6,
    "name": "phasellus in",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "curae",
    "iso_639_3_standard": "placerat",
    "langLocalChangeSeqNum": 7131,
    "langMasterChangeSeqNum": 6606,
    "langLastChangedBy": 97838
  }],
  "83098": [{
    "langUid": 1,
    "name": "pellentesque at",
    "iso_639_1_standard": "dui",
    "iso_639_2_standard": "quis",
    "iso_639_3_standard": "magna",
    "langLocalChangeSeqNum": 73801,
    "langMasterChangeSeqNum": 75240,
    "langLastChangedBy": 1624
  }, {
    "langUid": 2,
    "name": "lobortis sapien",
    "iso_639_1_standard": "ut",
    "iso_639_2_standard": "natoque",
    "iso_639_3_standard": "cum",
    "langLocalChangeSeqNum": 75482,
    "langMasterChangeSeqNum": 11068,
    "langLastChangedBy": 62140
  }, {
    "langUid": 3,
    "name": "iaculis congue",
    "iso_639_1_standard": "velit",
    "iso_639_2_standard": "pede",
    "iso_639_3_standard": "metus",
    "langLocalChangeSeqNum": 75370,
    "langMasterChangeSeqNum": 50569,
    "langLastChangedBy": 93444
  }, {
    "langUid": 4,
    "name": "sit amet",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "pellentesque",
    "iso_639_3_standard": "vulputate",
    "langLocalChangeSeqNum": 39410,
    "langMasterChangeSeqNum": 46112,
    "langLastChangedBy": 82190
  }, {
    "langUid": 5,
    "name": "ut nunc",
    "iso_639_1_standard": "praesent",
    "iso_639_2_standard": "nisi",
    "iso_639_3_standard": "nam",
    "langLocalChangeSeqNum": 24164,
    "langMasterChangeSeqNum": 34629,
    "langLastChangedBy": 12748
  }, {
    "langUid": 6,
    "name": "phasellus in",
    "iso_639_1_standard": "amet",
    "iso_639_2_standard": "curae",
    "iso_639_3_standard": "placerat",
    "langLocalChangeSeqNum": 7131,
    "langMasterChangeSeqNum": 6606,
    "langLastChangedBy": 97838
  }]
}
const entryList = {
  "1311236": [{
      "contentEntryUid": 41250,
      "title": "magnis dis parturient",
      "description": "Suspendisse potenti. In eleifend quam a odio.",
      "entryId": 7064822,
      "author": "Moritz Lindgren",
      "publisher": "Marlow Crumbleholme",
      "licenseType": 81,
      "licenseName": "vitae",
      "licenseUrl": "https://ucoz.com/mattis/nibh/ligula/nec/sem.json",
      "sourceUrl": "https://deliciousdays.com/volutpat/erat.xml?quam=convallis&sollicitudin=eget&vitae=eleifend&consectetuer=luctus&eget=ultricies&rutrum=eu&at=nibh&lorem=quisque&integer=id&tincidunt=justo&ante=sit&vel=amet&ipsum=sapien&praesent=dignissim&blandit=vestibulum&lacinia=vestibulum&erat=ante&vestibulum=ipsum&sed=primis&magna=in&at=faucibus&nunc=orci&commodo=luctus&placerat=et&praesent=ultrices&blandit=posuere&nam=cubilia&nulla=curae&integer=nulla&pede=dapibus&justo=dolor&lacinia=vel&eget=est&tincidunt=donec&eget=odio&tempus=justo&vel=sollicitudin&pede=ut&morbi=suscipit&porttitor=a&lorem=feugiat&id=et&ligula=eros&suspendisse=vestibulum&ornare=ac&consequat=est&lectus=lacinia&in=nisi&est=venenatis&risus=tristique&auctor=fusce&sed=congue&tristique=diam&in=id&tempus=ornare&sit=imperdiet&amet=sapien&sem=urna&fusce=pretium&consequat=nisl&nulla=ut&nisl=volutpat&nunc=sapien&nisl=arcu&duis=sed",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
      "lastModified": "62-963-5233",
      "leaf": false
    },
    {
      "contentEntryUid": 1311236,
      "title": "magnis dis parturient",
      "description": "Suspendisse potenti. In eleifend quam a odio.",
      "entryId": 7064822,
      "author": "Moritz Lindgren",
      "publisher": "Marlow Crumbleholme",
      "licenseType": 81,
      "licenseName": "vitae",
      "licenseUrl": "https://ucoz.com/mattis/nibh/ligula/nec/sem.json",
      "sourceUrl": "https://deliciousdays.com/volutpat/erat.xml?quam=convallis&sollicitudin=eget&vitae=eleifend&consectetuer=luctus&eget=ultricies&rutrum=eu&at=nibh&lorem=quisque&integer=id&tincidunt=justo&ante=sit&vel=amet&ipsum=sapien&praesent=dignissim&blandit=vestibulum&lacinia=vestibulum&erat=ante&vestibulum=ipsum&sed=primis&magna=in&at=faucibus&nunc=orci&commodo=luctus&placerat=et&praesent=ultrices&blandit=posuere&nam=cubilia&nulla=curae&integer=nulla&pede=dapibus&justo=dolor&lacinia=vel&eget=est&tincidunt=donec&eget=odio&tempus=justo&vel=sollicitudin&pede=ut&morbi=suscipit&porttitor=a&lorem=feugiat&id=et&ligula=eros&suspendisse=vestibulum&ornare=ac&consequat=est&lectus=lacinia&in=nisi&est=venenatis&risus=tristique&auctor=fusce&sed=congue&tristique=diam&in=id&tempus=ornare&sit=imperdiet&amet=sapien&sem=urna&fusce=pretium&consequat=nisl&nulla=ut&nisl=volutpat&nunc=sapien&nisl=arcu&duis=sed",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
      "lastModified": "62-963-5233",
      "leaf": false
    },
    {
      "contentEntryUid": 83098,
      "title": "est phasellus sit amet erat",
      "description": "In blandit ultrices enim. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin interdum mauris non ligula pellentesque ultrices. Phasellus id sapien in sapien iaculis congue.",
      "entryId": 4850079,
      "author": "Steffie Limb",
      "publisher": "Eal Geffe",
      "licenseType": 42,
      "licenseName": "ipsum aliquam",
      "licenseUrl": "https://dmoz.org/augue/quam/sollicitudin/vitae/consectetuer/eget.html",
      "sourceUrl": "https://tamu.edu/elementum/nullam/varius.png?interdum=sapien&mauris=in&non=sapien&ligula=iaculis&pellentesque=congue&ultrices=vivamus&phasellus=metus&id=arcu&sapien=adipiscing&in=molestie&sapien=hendrerit&iaculis=at&congue=vulputate&vivamus=vitae&metus=nisl&arcu=aenean&adipiscing=lectus&molestie=pellentesque&hendrerit=eget&at=nunc&vulputate=donec&vitae=quis&nisl=orci&aenean=eget&lectus=orci&pellentesque=vehicula&eget=condimentum&nunc=curabitur&donec=in&quis=libero&orci=ut&eget=massa&orci=volutpat&vehicula=convallis&condimentum=morbi&curabitur=odio&in=odio&libero=elementum&ut=eu&massa=interdum",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
      "lastModified": "01-794-7396",
      "leaf": true
    }, {
      "contentEntryUid": 31228,
      "title": "tincidunt nulla mollis",
      "description": "Fusce consequat.",
      "entryId": 7479491,
      "author": "Barny Jerromes",
      "publisher": "Camala Deary",
      "licenseType": 62,
      "licenseName": "in tempus",
      "licenseUrl": "http://is.gd/imperdiet.js",
      "sourceUrl": "http://cbsnews.com/odio/elementum/eu.jsp?magna=justo&bibendum=etiam&imperdiet=pretium&nullam=iaculis&orci=justo&pede=in&venenatis=hac&non=habitasse&sodales=platea&sed=dictumst&tincidunt=etiam&eu=faucibus&felis=cursus&fusce=urna&posuere=ut&felis=tellus&sed=nulla&lacus=ut&morbi=erat&sem=id&mauris=mauris&laoreet=vulputate&ut=elementum&rhoncus=nullam&aliquet=varius&pulvinar=nulla&sed=facilisi&nisl=cras&nunc=non&rhoncus=velit&dui=nec&vel=nisi&sem=vulputate&sed=nonummy&sagittis=maecenas",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
      "lastModified": "23-043-9012",
      "leaf": true
    }, {
      "contentEntryUid": 62506,
      "title": "cubilia curae donec",
      "description": "Integer ac neque. Duis bibendum.",
      "entryId": 8323608,
      "author": "Dion Pevie",
      "publisher": "Brander O'Garmen",
      "licenseType": 4,
      "licenseName": "luctus tincidunt",
      "licenseUrl": "https://ucla.edu/in/imperdiet/et.json",
      "sourceUrl": "https://mlb.com/pede/venenatis/non/sodales.html?turpis=a&sed=ipsum&ante=integer&vivamus=a&tortor=nibh&duis=in&mattis=quis&egestas=justo&metus=maecenas&aenean=rhoncus&fermentum=aliquam&donec=lacus&ut=morbi&mauris=quis&eget=tortor&massa=id&tempor=nulla&convallis=ultrices&nulla=aliquet&neque=maecenas&libero=leo&convallis=odio&eget=condimentum&eleifend=id&luctus=luctus&ultricies=nec&eu=molestie&nibh=sed&quisque=justo&id=pellentesque&justo=viverra&sit=pede&amet=ac&sapien=diam&dignissim=cras&vestibulum=pellentesque&vestibulum=volutpat&ante=dui&ipsum=maecenas&primis=tristique&in=est&faucibus=et&orci=tempus&luctus=semper&et=est&ultrices=quam&posuere=pharetra&cubilia=magna&curae=ac&nulla=consequat&dapibus=metus&dolor=sapien&vel=ut&est=nunc&donec=vestibulum&odio=ante&justo=ipsum&sollicitudin=primis&ut=in&suscipit=faucibus&a=orci&feugiat=luctus&et=et&eros=ultrices&vestibulum=posuere&ac=cubilia&est=curae&lacinia=mauris&nisi=viverra&venenatis=diam&tristique=vitae&fusce=quam&congue=suspendisse&diam=potenti&id=nullam&ornare=porttitor&imperdiet=lacus&sapien=at&urna=turpis&pretium=donec&nisl=posuere&ut=metus",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
      "lastModified": "24-156-6015",
      "leaf": true
    }, {
      "contentEntryUid": 38522,
      "title": "condimentum curabitur in libero ut",
      "description": "Nulla neque libero, convallis eget, eleifend luctus, ultricies eu, nibh. Quisque id justo sit amet sapien dignissim vestibulum.",
      "entryId": 7004919,
      "author": "Hewie Guilleton",
      "publisher": "Aubrie Cream",
      "licenseType": 39,
      "licenseName": "turpis donec",
      "licenseUrl": "http://mlb.com/molestie/nibh/in/lectus/pellentesque.aspx",
      "sourceUrl": "https://constantcontact.com/nunc/viverra/dapibus/nulla/suscipit/ligula.jsp?felis=eget&fusce=congue&posuere=eget&felis=semper&sed=rutrum&lacus=nulla&morbi=nunc&sem=purus&mauris=phasellus&laoreet=in&ut=felis&rhoncus=donec&aliquet=semper&pulvinar=sapien&sed=a&nisl=libero&nunc=nam&rhoncus=dui&dui=proin&vel=leo&sem=odio&sed=porttitor&sagittis=id&nam=consequat&congue=in&risus=consequat&semper=ut&porta=nulla&volutpat=sed&quam=accumsan&pede=felis&lobortis=ut&ligula=at&sit=dolor&amet=quis&eleifend=odio&pede=consequat&libero=varius",
      "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
      "lastModified": "70-351-1587",
      "leaf": true
    }
  ],
  "41250": [{
    "contentEntryUid": 72932,
    "title": "nulla sed vel enim sit",
    "description": "Donec ut mauris eget massa tempor convallis. Nulla neque libero, convallis eget, eleifend luctus, ultricies eu, nibh. Quisque id justo sit amet sapien dignissim vestibulum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla dapibus dolor vel est.",
    "entryId": 1685704,
    "author": "Damita Aland",
    "publisher": "Nixie Gerrey",
    "licenseType": 71,
    "licenseName": "est et",
    "licenseUrl": "https://cloudflare.com/orci/pede.jsp",
    "sourceUrl": "http://addtoany.com/blandit/nam/nulla/integer.js?diam=mauris&neque=enim&vestibulum=leo&eget=rhoncus&vulputate=sed&ut=vestibulum&ultrices=sit&vel=amet&augue=cursus&vestibulum=id&ante=turpis&ipsum=integer&primis=aliquet&in=massa&faucibus=id&orci=lobortis&luctus=convallis&et=tortor&ultrices=risus&posuere=dapibus&cubilia=augue&curae=vel&donec=accumsan&pharetra=tellus&magna=nisi&vestibulum=eu&aliquet=orci&ultrices=mauris&erat=lacinia&tortor=sapien&sollicitudin=quis&mi=libero&sit=nullam&amet=sit&lobortis=amet&sapien=turpis&sapien=elementum&non=ligula&mi=vehicula&integer=consequat&ac=morbi&neque=a&duis=ipsum&bibendum=integer&morbi=a&non=nibh&quam=in&nec=quis&dui=justo&luctus=maecenas&rutrum=rhoncus&nulla=aliquam&tellus=lacus&in=morbi&sagittis=quis&dui=tortor&vel=id&nisl=nulla&duis=ultrices&ac=aliquet&nibh=maecenas&fusce=leo&lacus=odio&purus=condimentum&aliquet=id&at=luctus&feugiat=nec&non=molestie&pretium=sed&quis=justo&lectus=pellentesque&suspendisse=viverra&potenti=pede&in=ac&eleifend=diam&quam=cras&a=pellentesque&odio=volutpat&in=dui&hac=maecenas",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "00-605-8113",
    "leaf": false
  }, {
    "contentEntryUid": 38321,
    "title": "in libero ut massa volutpat",
    "description": "Nullam molestie nibh in lectus.",
    "entryId": 7539665,
    "author": "Germaine Chastenet",
    "publisher": "Tye Gabbatiss",
    "licenseType": 87,
    "licenseName": "sapien",
    "licenseUrl": "https://upenn.edu/vitae/ipsum/aliquam/non/mauris.png",
    "sourceUrl": "https://hc360.com/id/consequat/in.json?iaculis=hac&diam=habitasse&erat=platea&fermentum=dictumst&justo=etiam&nec=faucibus&condimentum=cursus&neque=urna&sapien=ut&placerat=tellus&ante=nulla&nulla=ut&justo=erat&aliquam=id&quis=mauris&turpis=vulputate&eget=elementum&elit=nullam&sodales=varius&scelerisque=nulla&mauris=facilisi&sit=cras&amet=non&eros=velit&suspendisse=nec&accumsan=nisi&tortor=vulputate&quis=nonummy&turpis=maecenas&sed=tincidunt&ante=lacus&vivamus=at&tortor=velit&duis=vivamus&mattis=vel&egestas=nulla&metus=eget&aenean=eros&fermentum=elementum&donec=pellentesque&ut=quisque&mauris=porta&eget=volutpat&massa=erat&tempor=quisque&convallis=erat&nulla=eros&neque=viverra&libero=eget&convallis=congue&eget=eget&eleifend=semper&luctus=rutrum&ultricies=nulla&eu=nunc&nibh=purus&quisque=phasellus&id=in&justo=felis&sit=donec&amet=semper&sapien=sapien&dignissim=a&vestibulum=libero&vestibulum=nam&ante=dui&ipsum=proin&primis=leo&in=odio&faucibus=porttitor&orci=id&luctus=consequat&et=in&ultrices=consequat&posuere=ut&cubilia=nulla&curae=sed&nulla=accumsan&dapibus=felis&dolor=ut",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "49-138-1993",
    "leaf": true
  }, {
    "contentEntryUid": 84342,
    "title": "eget congue eget",
    "description": "Nam tristique tortor eu pede.",
    "entryId": 8757020,
    "author": "Ivie Ansteys",
    "publisher": "Fawn Raithby",
    "licenseType": 43,
    "licenseName": "nullam orci",
    "licenseUrl": "https://cmu.edu/at/nulla/suspendisse/potenti.jpg",
    "sourceUrl": "https://intel.com/volutpat/erat/quisque/erat/eros.png?in=augue&hac=aliquam&habitasse=erat&platea=volutpat&dictumst=in&morbi=congue&vestibulum=etiam&velit=justo&id=etiam&pretium=pretium&iaculis=iaculis&diam=justo&erat=in&fermentum=hac&justo=habitasse&nec=platea&condimentum=dictumst&neque=etiam&sapien=faucibus&placerat=cursus&ante=urna&nulla=ut&justo=tellus&aliquam=nulla&quis=ut&turpis=erat&eget=id&elit=mauris&sodales=vulputate&scelerisque=elementum&mauris=nullam&sit=varius&amet=nulla&eros=facilisi&suspendisse=cras&accumsan=non&tortor=velit&quis=nec&turpis=nisi&sed=vulputate&ante=nonummy&vivamus=maecenas&tortor=tincidunt&duis=lacus&mattis=at&egestas=velit&metus=vivamus&aenean=vel&fermentum=nulla&donec=eget&ut=eros&mauris=elementum&eget=pellentesque&massa=quisque&tempor=porta&convallis=volutpat&nulla=erat&neque=quisque&libero=erat&convallis=eros&eget=viverra&eleifend=eget&luctus=congue&ultricies=eget&eu=semper",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/ff4444/ffffff",
    "lastModified": "83-583-5809",
    "leaf": true
  }, {
    "contentEntryUid": 14686,
    "title": "sapien arcu sed",
    "description": "In hac habitasse platea dictumst. Etiam faucibus cursus urna.",
    "entryId": 7875633,
    "author": "Homerus Lightbown",
    "publisher": "Flora Banck",
    "licenseType": 78,
    "licenseName": "in",
    "licenseUrl": "https://prlog.org/sollicitudin/vitae.json",
    "sourceUrl": "https://chicagotribune.com/tincidunt/nulla/mollis/molestie/lorem/quisque/ut.html?eu=sem&felis=praesent&fusce=id&posuere=massa&felis=id&sed=nisl&lacus=venenatis&morbi=lacinia&sem=aenean&mauris=sit&laoreet=amet&ut=justo&rhoncus=morbi&aliquet=ut&pulvinar=odio&sed=cras&nisl=mi&nunc=pede&rhoncus=malesuada&dui=in&vel=imperdiet&sem=et&sed=commodo&sagittis=vulputate&nam=justo&congue=in&risus=blandit&semper=ultrices&porta=enim&volutpat=lorem&quam=ipsum&pede=dolor&lobortis=sit&ligula=amet&sit=consectetuer&amet=adipiscing&eleifend=elit&pede=proin&libero=interdum&quis=mauris&orci=non&nullam=ligula&molestie=pellentesque&nibh=ultrices&in=phasellus&lectus=id",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "03-900-3267",
    "leaf": true
  }, {
    "contentEntryUid": 55343,
    "title": "venenatis turpis enim blandit mi",
    "description": "Etiam justo. Etiam pretium iaculis justo.",
    "entryId": 808648,
    "author": "Bette-ann Petofi",
    "publisher": "Caldwell Heisler",
    "licenseType": 13,
    "licenseName": "cubilia curae",
    "licenseUrl": "https://google.cn/ut/erat/id/mauris.xml",
    "sourceUrl": "http://friendfeed.com/nascetur/ridiculus/mus/vivamus/vestibulum/sagittis/sapien.jpg?nulla=luctus&suspendisse=rutrum&potenti=nulla&cras=tellus&in=in&purus=sagittis&eu=dui&magna=vel&vulputate=nisl&luctus=duis&cum=ac&sociis=nibh&natoque=fusce&penatibus=lacus&et=purus&magnis=aliquet&dis=at&parturient=feugiat&montes=non&nascetur=pretium&ridiculus=quis&mus=lectus&vivamus=suspendisse&vestibulum=potenti&sagittis=in&sapien=eleifend&cum=quam&sociis=a&natoque=odio&penatibus=in&et=hac&magnis=habitasse&dis=platea&parturient=dictumst&montes=maecenas&nascetur=ut&ridiculus=massa&mus=quis&etiam=augue&vel=luctus&augue=tincidunt&vestibulum=nulla&rutrum=mollis&rutrum=molestie&neque=lorem&aenean=quisque&auctor=ut&gravida=erat&sem=curabitur&praesent=gravida&id=nisi&massa=at&id=nibh&nisl=in&venenatis=hac&lacinia=habitasse&aenean=platea&sit=dictumst&amet=aliquam&justo=augue&morbi=quam&ut=sollicitudin&odio=vitae&cras=consectetuer&mi=eget&pede=rutrum&malesuada=at&in=lorem&imperdiet=integer&et=tincidunt&commodo=ante&vulputate=vel&justo=ipsum&in=praesent&blandit=blandit&ultrices=lacinia&enim=erat&lorem=vestibulum&ipsum=sed&dolor=magna&sit=at&amet=nunc&consectetuer=commodo&adipiscing=placerat&elit=praesent&proin=blandit&interdum=nam&mauris=nulla&non=integer&ligula=pede&pellentesque=justo&ultrices=lacinia",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "06-461-2378",
    "leaf": true
  }, {
    "contentEntryUid": 92589,
    "title": "blandit nam nulla integer pede",
    "description": "Sed sagittis. Nam congue, risus semper porta volutpat, quam pede lobortis ligula, sit amet eleifend pede libero quis orci.",
    "entryId": 425697,
    "author": "Jessy Shotbolt",
    "publisher": "Dyanne Gate",
    "licenseType": 16,
    "licenseName": "vel",
    "licenseUrl": "http://xrea.com/quam/turpis/adipiscing.js",
    "sourceUrl": "https://merriam-webster.com/vitae/consectetuer/eget/rutrum.xml?diam=luctus&cras=et&pellentesque=ultrices&volutpat=posuere&dui=cubilia&maecenas=curae&tristique=nulla&est=dapibus&et=dolor&tempus=vel&semper=est&est=donec&quam=odio&pharetra=justo&magna=sollicitudin&ac=ut&consequat=suscipit&metus=a&sapien=feugiat&ut=et&nunc=eros&vestibulum=vestibulum&ante=ac&ipsum=est&primis=lacinia&in=nisi&faucibus=venenatis&orci=tristique&luctus=fusce&et=congue&ultrices=diam&posuere=id&cubilia=ornare&curae=imperdiet&mauris=sapien&viverra=urna&diam=pretium&vitae=nisl&quam=ut&suspendisse=volutpat",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "21-355-7776",
    "leaf": true
  }],
  "72932": [{
    "contentEntryUid": 52549,
    "title": "quam pede lobortis",
    "description": "In congue. Etiam justo. Etiam pretium iaculis justo.",
    "entryId": 8466392,
    "author": "Bessie Bullman",
    "publisher": "Winfield Pittet",
    "licenseType": 4,
    "licenseName": "dictumst",
    "licenseUrl": "https://canalblog.com/id/nisl/venenatis/lacinia.jpg",
    "sourceUrl": "http://1und1.de/consequat/ut.jsp?justo=id&maecenas=ligula&rhoncus=suspendisse&aliquam=ornare&lacus=consequat&morbi=lectus&quis=in&tortor=est&id=risus&nulla=auctor&ultrices=sed&aliquet=tristique&maecenas=in&leo=tempus&odio=sit&condimentum=amet&id=sem&luctus=fusce&nec=consequat&molestie=nulla&sed=nisl&justo=nunc&pellentesque=nisl&viverra=duis&pede=bibendum&ac=felis&diam=sed&cras=interdum&pellentesque=venenatis&volutpat=turpis&dui=enim&maecenas=blandit&tristique=mi&est=in&et=porttitor&tempus=pede",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "81-435-0009",
    "leaf": false
  }, {
    "contentEntryUid": 71343,
    "title": "metus vitae ipsum aliquam non",
    "description": "Integer aliquet, massa id lobortis convallis, tortor risus dapibus augue, vel accumsan tellus nisi eu orci. Mauris lacinia sapien quis libero. Nullam sit amet turpis elementum ligula vehicula consequat. Morbi a ipsum.",
    "entryId": 6882360,
    "author": "Lotty Kimbury",
    "publisher": "Gabbie Higginbottam",
    "licenseType": 47,
    "licenseName": "lacus curabitur",
    "licenseUrl": "https://merriam-webster.com/feugiat/non/pretium/quis/lectus/suspendisse.html",
    "sourceUrl": "https://123-reg.co.uk/duis/faucibus/accumsan.png?eget=etiam&tincidunt=pretium&eget=iaculis&tempus=justo&vel=in&pede=hac&morbi=habitasse&porttitor=platea&lorem=dictumst&id=etiam&ligula=faucibus&suspendisse=cursus&ornare=urna&consequat=ut&lectus=tellus&in=nulla&est=ut&risus=erat&auctor=id&sed=mauris&tristique=vulputate&in=elementum&tempus=nullam&sit=varius&amet=nulla&sem=facilisi&fusce=cras&consequat=non&nulla=velit&nisl=nec&nunc=nisi&nisl=vulputate&duis=nonummy&bibendum=maecenas&felis=tincidunt",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "65-365-5627",
    "leaf": true
  }, {
    "contentEntryUid": 39313,
    "title": "luctus cum sociis natoque",
    "description": "Morbi porttitor lorem id ligula. Suspendisse ornare consequat lectus. In est risus, auctor sed, tristique in, tempus sit amet, sem.",
    "entryId": 6873146,
    "author": "Agnese Lackie",
    "publisher": "Chickie Dizlie",
    "licenseType": 100,
    "licenseName": "congue",
    "licenseUrl": "https://mit.edu/velit/vivamus/vel/nulla.json",
    "sourceUrl": "http://google.cn/blandit/mi/in/porttitor/pede/justo.xml?aliquam=scelerisque&sit=mauris&amet=sit&diam=amet&in=eros&magna=suspendisse&bibendum=accumsan&imperdiet=tortor&nullam=quis&orci=turpis&pede=sed&venenatis=ante&non=vivamus&sodales=tortor&sed=duis&tincidunt=mattis&eu=egestas&felis=metus&fusce=aenean&posuere=fermentum&felis=donec&sed=ut&lacus=mauris&morbi=eget&sem=massa&mauris=tempor&laoreet=convallis&ut=nulla&rhoncus=neque&aliquet=libero&pulvinar=convallis&sed=eget&nisl=eleifend&nunc=luctus&rhoncus=ultricies&dui=eu&vel=nibh&sem=quisque&sed=id&sagittis=justo&nam=sit&congue=amet&risus=sapien&semper=dignissim&porta=vestibulum",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "29-328-9775",
    "leaf": true
  }, {
    "contentEntryUid": 56681,
    "title": "sit amet sem fusce",
    "description": "Donec dapibus. Duis at velit eu est congue elementum. In hac habitasse platea dictumst. Morbi vestibulum, velit id pretium iaculis, diam erat fermentum justo, nec condimentum neque sapien placerat ante.",
    "entryId": 7319255,
    "author": "Arther Bottomley",
    "publisher": "Saraann Titman",
    "licenseType": 71,
    "licenseName": "habitasse",
    "licenseUrl": "http://discuz.net/felis/sed/lacus.jsp",
    "sourceUrl": "http://nhs.uk/sit/amet/turpis/elementum.html?fringilla=dapibus&rhoncus=duis&mauris=at&enim=velit&leo=eu&rhoncus=est&sed=congue&vestibulum=elementum&sit=in&amet=hac&cursus=habitasse&id=platea&turpis=dictumst&integer=morbi&aliquet=vestibulum&massa=velit&id=id&lobortis=pretium&convallis=iaculis&tortor=diam&risus=erat&dapibus=fermentum&augue=justo&vel=nec&accumsan=condimentum&tellus=neque&nisi=sapien&eu=placerat&orci=ante&mauris=nulla&lacinia=justo&sapien=aliquam&quis=quis&libero=turpis&nullam=eget&sit=elit&amet=sodales&turpis=scelerisque&elementum=mauris&ligula=sit&vehicula=amet&consequat=eros&morbi=suspendisse&a=accumsan&ipsum=tortor&integer=quis&a=turpis&nibh=sed&in=ante&quis=vivamus&justo=tortor&maecenas=duis&rhoncus=mattis&aliquam=egestas&lacus=metus&morbi=aenean&quis=fermentum&tortor=donec&id=ut&nulla=mauris&ultrices=eget&aliquet=massa&maecenas=tempor&leo=convallis&odio=nulla&condimentum=neque&id=libero&luctus=convallis&nec=eget&molestie=eleifend&sed=luctus&justo=ultricies&pellentesque=eu&viverra=nibh&pede=quisque&ac=id",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "12-137-9799",
    "leaf": true
  }, {
    "contentEntryUid": 92338,
    "title": "nullam varius nulla facilisi",
    "description": "Aliquam non mauris. Morbi non lectus.",
    "entryId": 291165,
    "author": "Gardiner Czaja",
    "publisher": "Catherin Mangam",
    "licenseType": 57,
    "licenseName": "erat",
    "licenseUrl": "https://ehow.com/consequat/metus.jsp",
    "sourceUrl": "http://ehow.com/libero/ut/massa.aspx?et=et&tempus=commodo",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "99-542-6280",
    "leaf": true
  }, {
    "contentEntryUid": 24995,
    "title": "et magnis dis parturient",
    "description": "Morbi non quam nec dui luctus rutrum. Nulla tellus. In sagittis dui vel nisl. Duis ac nibh.",
    "entryId": 2108041,
    "author": "Susana Hodjetts",
    "publisher": "Lelia Ewer",
    "licenseType": 97,
    "licenseName": "integer",
    "licenseUrl": "http://yellowbook.com/cursus/vestibulum/proin.aspx",
    "sourceUrl": "http://engadget.com/ante/vivamus/tortor/duis/mattis/egestas/metus.png?orci=praesent&vehicula=lectus&condimentum=vestibulum&curabitur=quam&in=sapien&libero=varius&ut=ut&massa=blandit&volutpat=non&convallis=interdum&morbi=in&odio=ante&odio=vestibulum&elementum=ante&eu=ipsum&interdum=primis&eu=in&tincidunt=faucibus&in=orci&leo=luctus&maecenas=et&pulvinar=ultrices&lobortis=posuere&est=cubilia&phasellus=curae&sit=duis&amet=faucibus&erat=accumsan&nulla=odio&tempus=curabitur&vivamus=convallis&in=duis&felis=consequat&eu=dui&sapien=nec&cursus=nisi&vestibulum=volutpat&proin=eleifend&eu=donec&mi=ut&nulla=dolor&ac=morbi&enim=vel&in=lectus&tempor=in&turpis=quam&nec=fringilla",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "32-832-8900",
    "leaf": true
  }, {
    "contentEntryUid": 57410,
    "title": "elementum ligula vehicula consequat morbi",
    "description": "Proin interdum mauris non ligula pellentesque ultrices. Phasellus id sapien in sapien iaculis congue. Vivamus metus arcu, adipiscing molestie, hendrerit at, vulputate vitae, nisl.",
    "entryId": 5255886,
    "author": "Augie Skaif",
    "publisher": "Lisabeth Pyer",
    "licenseType": 80,
    "licenseName": "ut nulla",
    "licenseUrl": "https://spiegel.de/adipiscing/molestie/hendrerit.jpg",
    "sourceUrl": "https://friendfeed.com/id/consequat/in.png?ut=augue&massa=vestibulum&volutpat=rutrum&convallis=rutrum&morbi=neque&odio=aenean&odio=auctor&elementum=gravida&eu=sem&interdum=praesent&eu=id&tincidunt=massa&in=id&leo=nisl&maecenas=venenatis&pulvinar=lacinia&lobortis=aenean&est=sit&phasellus=amet&sit=justo&amet=morbi&erat=ut&nulla=odio&tempus=cras&vivamus=mi&in=pede&felis=malesuada&eu=in&sapien=imperdiet&cursus=et&vestibulum=commodo&proin=vulputate&eu=justo&mi=in&nulla=blandit&ac=ultrices&enim=enim&in=lorem&tempor=ipsum&turpis=dolor&nec=sit&euismod=amet&scelerisque=consectetuer&quam=adipiscing&turpis=elit&adipiscing=proin&lorem=interdum&vitae=mauris&mattis=non&nibh=ligula&ligula=pellentesque&nec=ultrices&sem=phasellus&duis=id&aliquam=sapien&convallis=in&nunc=sapien&proin=iaculis&at=congue&turpis=vivamus&a=metus&pede=arcu&posuere=adipiscing&nonummy=molestie&integer=hendrerit&non=at&velit=vulputate&donec=vitae&diam=nisl&neque=aenean&vestibulum=lectus&eget=pellentesque&vulputate=eget&ut=nunc&ultrices=donec&vel=quis&augue=orci&vestibulum=eget&ante=orci&ipsum=vehicula&primis=condimentum&in=curabitur&faucibus=in&orci=libero&luctus=ut&et=massa&ultrices=volutpat&posuere=convallis&cubilia=morbi&curae=odio&donec=odio&pharetra=elementum&magna=eu&vestibulum=interdum&aliquet=eu&ultrices=tincidunt",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "33-397-8961",
    "leaf": true
  }, {
    "contentEntryUid": 78035,
    "title": "imperdiet sapien urna",
    "description": "Quisque id justo sit amet sapien dignissim vestibulum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla dapibus dolor vel est. Donec odio justo, sollicitudin ut, suscipit a, feugiat et, eros.",
    "entryId": 8685902,
    "author": "Carol Sibun",
    "publisher": "Sheryl Wilcinskis",
    "licenseType": 12,
    "licenseName": "morbi",
    "licenseUrl": "http://istockphoto.com/non/quam/nec/dui/luctus/rutrum/nulla.html",
    "sourceUrl": "http://addthis.com/consequat/varius/integer/ac/leo/pellentesque.png?imperdiet=consequat&et=dui&commodo=nec&vulputate=nisi&justo=volutpat&in=eleifend&blandit=donec&ultrices=ut&enim=dolor&lorem=morbi&ipsum=vel&dolor=lectus&sit=in&amet=quam&consectetuer=fringilla&adipiscing=rhoncus&elit=mauris&proin=enim&interdum=leo&mauris=rhoncus&non=sed&ligula=vestibulum&pellentesque=sit&ultrices=amet&phasellus=cursus&id=id&sapien=turpis&in=integer&sapien=aliquet&iaculis=massa&congue=id&vivamus=lobortis&metus=convallis&arcu=tortor&adipiscing=risus&molestie=dapibus&hendrerit=augue&at=vel&vulputate=accumsan&vitae=tellus&nisl=nisi&aenean=eu&lectus=orci&pellentesque=mauris&eget=lacinia&nunc=sapien&donec=quis&quis=libero&orci=nullam&eget=sit&orci=amet&vehicula=turpis&condimentum=elementum&curabitur=ligula&in=vehicula&libero=consequat&ut=morbi&massa=a&volutpat=ipsum&convallis=integer&morbi=a&odio=nibh&odio=in&elementum=quis&eu=justo&interdum=maecenas&eu=rhoncus&tincidunt=aliquam&in=lacus&leo=morbi&maecenas=quis&pulvinar=tortor&lobortis=id&est=nulla&phasellus=ultrices&sit=aliquet&amet=maecenas&erat=leo&nulla=odio",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/ff4444/ffffff",
    "lastModified": "09-654-4063",
    "leaf": true
  }, {
    "contentEntryUid": 5910,
    "title": "vivamus vestibulum sagittis",
    "description": "Nulla neque libero, convallis eget, eleifend luctus, ultricies eu, nibh. Quisque id justo sit amet sapien dignissim vestibulum. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla dapibus dolor vel est. Donec odio justo, sollicitudin ut, suscipit a, feugiat et, eros.",
    "entryId": 5387970,
    "author": "Wilbert Richen",
    "publisher": "Klaus Godain",
    "licenseType": 9,
    "licenseName": "mollis",
    "licenseUrl": "http://mac.com/integer/a/nibh.xml",
    "sourceUrl": "https://elegantthemes.com/phasellus/in/felis/donec.aspx?tristique=sem&fusce=sed&congue=sagittis&diam=nam&id=congue&ornare=risus&imperdiet=semper&sapien=porta&urna=volutpat&pretium=quam&nisl=pede&ut=lobortis&volutpat=ligula&sapien=sit&arcu=amet&sed=eleifend&augue=pede&aliquam=libero&erat=quis&volutpat=orci&in=nullam&congue=molestie&etiam=nibh&justo=in&etiam=lectus&pretium=pellentesque&iaculis=at&justo=nulla&in=suspendisse&hac=potenti&habitasse=cras&platea=in&dictumst=purus&etiam=eu&faucibus=magna&cursus=vulputate&urna=luctus&ut=cum&tellus=sociis&nulla=natoque&ut=penatibus&erat=et&id=magnis&mauris=dis&vulputate=parturient&elementum=montes&nullam=nascetur&varius=ridiculus&nulla=mus&facilisi=vivamus&cras=vestibulum&non=sagittis&velit=sapien&nec=cum&nisi=sociis&vulputate=natoque&nonummy=penatibus&maecenas=et&tincidunt=magnis&lacus=dis&at=parturient&velit=montes&vivamus=nascetur&vel=ridiculus&nulla=mus&eget=etiam&eros=vel&elementum=augue&pellentesque=vestibulum&quisque=rutrum&porta=rutrum&volutpat=neque&erat=aenean&quisque=auctor&erat=gravida&eros=sem&viverra=praesent&eget=id&congue=massa&eget=id&semper=nisl&rutrum=venenatis&nulla=lacinia&nunc=aenean&purus=sit&phasellus=amet&in=justo&felis=morbi&donec=ut&semper=odio&sapien=cras&a=mi&libero=pede&nam=malesuada&dui=in&proin=imperdiet&leo=et&odio=commodo&porttitor=vulputate",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "12-333-1147",
    "leaf": true
  }, {
    "contentEntryUid": 50,
    "title": "est risus auctor",
    "description": "Sed accumsan felis. Ut at dolor quis odio consequat varius.",
    "entryId": 246062,
    "author": "Barr Mustoe",
    "publisher": "Vina Stempe",
    "licenseType": 31,
    "licenseName": "tincidunt",
    "licenseUrl": "http://youtube.com/id/lobortis/convallis/tortor/risus/dapibus/augue.js",
    "sourceUrl": "https://tumblr.com/a/ipsum/integer.jpg?tincidunt=dignissim&nulla=vestibulum&mollis=vestibulum&molestie=ante&lorem=ipsum&quisque=primis&ut=in&erat=faucibus&curabitur=orci&gravida=luctus&nisi=et&at=ultrices&nibh=posuere&in=cubilia&hac=curae&habitasse=nulla&platea=dapibus&dictumst=dolor&aliquam=vel&augue=est&quam=donec&sollicitudin=odio&vitae=justo&consectetuer=sollicitudin&eget=ut&rutrum=suscipit&at=a&lorem=feugiat&integer=et&tincidunt=eros&ante=vestibulum&vel=ac&ipsum=est&praesent=lacinia&blandit=nisi&lacinia=venenatis&erat=tristique&vestibulum=fusce&sed=congue&magna=diam&at=id&nunc=ornare&commodo=imperdiet&placerat=sapien&praesent=urna&blandit=pretium&nam=nisl&nulla=ut&integer=volutpat&pede=sapien&justo=arcu&lacinia=sed&eget=augue&tincidunt=aliquam&eget=erat&tempus=volutpat&vel=in&pede=congue&morbi=etiam&porttitor=justo&lorem=etiam&id=pretium&ligula=iaculis&suspendisse=justo",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "60-208-0014",
    "leaf": true
  }, {
    "contentEntryUid": 92757,
    "title": "duis mattis egestas metus",
    "description": "Nunc rhoncus dui vel sem. Sed sagittis. Nam congue, risus semper porta volutpat, quam pede lobortis ligula, sit amet eleifend pede libero quis orci. Nullam molestie nibh in lectus.",
    "entryId": 3928750,
    "author": "Coral Fairebrother",
    "publisher": "Cahra Khomich",
    "licenseType": 37,
    "licenseName": "sed tincidunt",
    "licenseUrl": "http://wordpress.org/nullam/sit/amet.png",
    "sourceUrl": "https://uol.com.br/felis/sed/interdum/venenatis/turpis/enim/blandit.js?nascetur=ipsum&ridiculus=dolor&mus=sit&vivamus=amet&vestibulum=consectetuer&sagittis=adipiscing&sapien=elit&cum=proin",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "43-067-5763",
    "leaf": true
  }, {
    "contentEntryUid": 58757,
    "title": "amet eleifend pede libero",
    "description": "Praesent lectus. Vestibulum quam sapien, varius ut, blandit non, interdum in, ante. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Duis faucibus accumsan odio. Curabitur convallis.",
    "entryId": 2653211,
    "author": "Jasmin Wison",
    "publisher": "Burr Brende",
    "licenseType": 15,
    "licenseName": "orci",
    "licenseUrl": "http://themeforest.net/mus/etiam/vel/augue/vestibulum.png",
    "sourceUrl": "https://bluehost.com/ornare.html?est=imperdiet&phasellus=sapien&sit=urna&amet=pretium&erat=nisl&nulla=ut&tempus=volutpat&vivamus=sapien&in=arcu&felis=sed&eu=augue&sapien=aliquam&cursus=erat&vestibulum=volutpat&proin=in&eu=congue&mi=etiam&nulla=justo&ac=etiam&enim=pretium&in=iaculis&tempor=justo&turpis=in&nec=hac&euismod=habitasse&scelerisque=platea&quam=dictumst&turpis=etiam&adipiscing=faucibus&lorem=cursus&vitae=urna&mattis=ut&nibh=tellus&ligula=nulla&nec=ut&sem=erat&duis=id&aliquam=mauris&convallis=vulputate&nunc=elementum&proin=nullam&at=varius&turpis=nulla&a=facilisi&pede=cras&posuere=non&nonummy=velit&integer=nec&non=nisi&velit=vulputate&donec=nonummy&diam=maecenas&neque=tincidunt&vestibulum=lacus&eget=at&vulputate=velit&ut=vivamus&ultrices=vel&vel=nulla&augue=eget&vestibulum=eros&ante=elementum&ipsum=pellentesque&primis=quisque&in=porta&faucibus=volutpat&orci=erat&luctus=quisque&et=erat&ultrices=eros&posuere=viverra&cubilia=eget&curae=congue&donec=eget&pharetra=semper&magna=rutrum&vestibulum=nulla&aliquet=nunc&ultrices=purus&erat=phasellus&tortor=in&sollicitudin=felis&mi=donec&sit=semper&amet=sapien&lobortis=a&sapien=libero&sapien=nam&non=dui&mi=proin&integer=leo&ac=odio&neque=porttitor&duis=id&bibendum=consequat&morbi=in&non=consequat&quam=ut",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "68-452-4863",
    "leaf": true
  }, {
    "contentEntryUid": 50994,
    "title": "eleifend pede libero quis orci",
    "description": "Sed sagittis. Nam congue, risus semper porta volutpat, quam pede lobortis ligula, sit amet eleifend pede libero quis orci.",
    "entryId": 3513126,
    "author": "Frederica Batkin",
    "publisher": "Briant Henner",
    "licenseType": 54,
    "licenseName": "donec",
    "licenseUrl": "http://csmonitor.com/duis/aliquam/convallis/nunc/proin.jsp",
    "sourceUrl": "http://google.pl/eleifend.aspx?quis=quam&justo=pharetra&maecenas=magna&rhoncus=ac&aliquam=consequat&lacus=metus&morbi=sapien&quis=ut&tortor=nunc&id=vestibulum&nulla=ante&ultrices=ipsum&aliquet=primis&maecenas=in&leo=faucibus&odio=orci&condimentum=luctus&id=et&luctus=ultrices&nec=posuere&molestie=cubilia&sed=curae&justo=mauris&pellentesque=viverra&viverra=diam&pede=vitae&ac=quam&diam=suspendisse&cras=potenti&pellentesque=nullam&volutpat=porttitor&dui=lacus&maecenas=at&tristique=turpis&est=donec",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "47-600-6656",
    "leaf": true
  }, {
    "contentEntryUid": 14879,
    "title": "erat fermentum justo",
    "description": "Nullam molestie nibh in lectus. Pellentesque at nulla. Suspendisse potenti. Cras in purus eu magna vulputate luctus.",
    "entryId": 5501898,
    "author": "Dion Fairhead",
    "publisher": "Roseanne Cullip",
    "licenseType": 29,
    "licenseName": "proin",
    "licenseUrl": "https://engadget.com/aliquam/convallis.jsp",
    "sourceUrl": "http://newsvine.com/sit/amet.html?luctus=id&nec=ornare&molestie=imperdiet&sed=sapien&justo=urna&pellentesque=pretium&viverra=nisl&pede=ut&ac=volutpat&diam=sapien&cras=arcu&pellentesque=sed&volutpat=augue&dui=aliquam&maecenas=erat&tristique=volutpat&est=in&et=congue&tempus=etiam&semper=justo&est=etiam&quam=pretium&pharetra=iaculis&magna=justo&ac=in&consequat=hac&metus=habitasse&sapien=platea&ut=dictumst&nunc=etiam&vestibulum=faucibus&ante=cursus&ipsum=urna&primis=ut&in=tellus&faucibus=nulla&orci=ut&luctus=erat&et=id&ultrices=mauris&posuere=vulputate&cubilia=elementum&curae=nullam&mauris=varius&viverra=nulla&diam=facilisi&vitae=cras&quam=non&suspendisse=velit&potenti=nec&nullam=nisi&porttitor=vulputate&lacus=nonummy&at=maecenas&turpis=tincidunt",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "08-451-2758",
    "leaf": true
  }, {
    "contentEntryUid": 66350,
    "title": "commodo placerat praesent blandit nam",
    "description": "Suspendisse ornare consequat lectus. In est risus, auctor sed, tristique in, tempus sit amet, sem.",
    "entryId": 1270935,
    "author": "Joseph Bartholin",
    "publisher": "Elwin Hymas",
    "licenseType": 76,
    "licenseName": "tortor sollicitudin",
    "licenseUrl": "http://youtu.be/a/nibh/in/quis/justo/maecenas/rhoncus.html",
    "sourceUrl": "https://51.la/eu.jsp?vivamus=adipiscing&tortor=elit&duis=proin&mattis=risus&egestas=praesent&metus=lectus&aenean=vestibulum&fermentum=quam&donec=sapien&ut=varius&mauris=ut&eget=blandit&massa=non&tempor=interdum&convallis=in&nulla=ante&neque=vestibulum",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/ff4444/ffffff",
    "lastModified": "26-174-4796",
    "leaf": true
  }, {
    "contentEntryUid": 65046,
    "title": "congue vivamus metus arcu",
    "description": "Morbi non lectus.",
    "entryId": 2761412,
    "author": "Penelopa Hartmann",
    "publisher": "Louella Overil",
    "licenseType": 16,
    "licenseName": "quis",
    "licenseUrl": "http://examiner.com/sagittis/nam/congue/risus.json",
    "sourceUrl": "https://uiuc.edu/tempus/vel/pede/morbi/porttitor/lorem/id.png?mauris=luctus&laoreet=et&ut=ultrices&rhoncus=posuere&aliquet=cubilia&pulvinar=curae&sed=mauris&nisl=viverra&nunc=diam&rhoncus=vitae&dui=quam&vel=suspendisse&sem=potenti&sed=nullam&sagittis=porttitor&nam=lacus&congue=at&risus=turpis&semper=donec&porta=posuere&volutpat=metus&quam=vitae&pede=ipsum&lobortis=aliquam&ligula=non&sit=mauris&amet=morbi&eleifend=non&pede=lectus&libero=aliquam&quis=sit&orci=amet&nullam=diam&molestie=in&nibh=magna&in=bibendum&lectus=imperdiet&pellentesque=nullam&at=orci&nulla=pede&suspendisse=venenatis&potenti=non&cras=sodales&in=sed&purus=tincidunt&eu=eu&magna=felis&vulputate=fusce&luctus=posuere&cum=felis&sociis=sed&natoque=lacus&penatibus=morbi&et=sem&magnis=mauris&dis=laoreet&parturient=ut&montes=rhoncus&nascetur=aliquet&ridiculus=pulvinar&mus=sed&vivamus=nisl&vestibulum=nunc&sagittis=rhoncus&sapien=dui&cum=vel&sociis=sem&natoque=sed&penatibus=sagittis&et=nam&magnis=congue&dis=risus&parturient=semper&montes=porta&nascetur=volutpat&ridiculus=quam&mus=pede",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "17-412-0047",
    "leaf": true
  }, {
    "contentEntryUid": 30154,
    "title": "sit amet eleifend",
    "description": "Integer pede justo, lacinia eget, tincidunt eget, tempus vel, pede. Morbi porttitor lorem id ligula.",
    "entryId": 9759789,
    "author": "Robin Antoszewski",
    "publisher": "Minta Alloisi",
    "licenseType": 26,
    "licenseName": "leo",
    "licenseUrl": "http://dell.com/nulla/tempus/vivamus.aspx",
    "sourceUrl": "http://skype.com/curabitur/gravida.aspx?elit=id&proin=ornare&risus=imperdiet&praesent=sapien&lectus=urna&vestibulum=pretium&quam=nisl&sapien=ut&varius=volutpat&ut=sapien&blandit=arcu&non=sed&interdum=augue&in=aliquam&ante=erat&vestibulum=volutpat&ante=in&ipsum=congue&primis=etiam&in=justo&faucibus=etiam&orci=pretium&luctus=iaculis&et=justo&ultrices=in&posuere=hac&cubilia=habitasse&curae=platea&duis=dictumst&faucibus=etiam&accumsan=faucibus&odio=cursus&curabitur=urna&convallis=ut&duis=tellus&consequat=nulla&dui=ut&nec=erat&nisi=id&volutpat=mauris&eleifend=vulputate&donec=elementum&ut=nullam&dolor=varius&morbi=nulla&vel=facilisi&lectus=cras&in=non&quam=velit&fringilla=nec&rhoncus=nisi&mauris=vulputate&enim=nonummy&leo=maecenas&rhoncus=tincidunt&sed=lacus&vestibulum=at&sit=velit&amet=vivamus&cursus=vel&id=nulla&turpis=eget&integer=eros&aliquet=elementum&massa=pellentesque&id=quisque&lobortis=porta&convallis=volutpat&tortor=erat&risus=quisque&dapibus=erat&augue=eros&vel=viverra&accumsan=eget&tellus=congue&nisi=eget&eu=semper&orci=rutrum&mauris=nulla&lacinia=nunc&sapien=purus&quis=phasellus&libero=in&nullam=felis&sit=donec&amet=semper&turpis=sapien&elementum=a&ligula=libero&vehicula=nam&consequat=dui&morbi=proin&a=leo&ipsum=odio&integer=porttitor&a=id",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/5fa2dd/ffffff",
    "lastModified": "11-122-5762",
    "leaf": true
  }, {
    "contentEntryUid": 15438,
    "title": "lectus suspendisse potenti",
    "description": "Aliquam quis turpis eget elit sodales scelerisque. Mauris sit amet eros. Suspendisse accumsan tortor quis turpis.",
    "entryId": 3207829,
    "author": "Ichabod Carmont",
    "publisher": "Giulia Rolles",
    "licenseType": 7,
    "licenseName": "erat volutpat",
    "licenseUrl": "http://gmpg.org/turpis.png",
    "sourceUrl": "https://nasa.gov/risus/dapibus/augue.js?congue=faucibus&etiam=orci&justo=luctus&etiam=et&pretium=ultrices&iaculis=posuere&justo=cubilia&in=curae&hac=donec&habitasse=pharetra&platea=magna&dictumst=vestibulum&etiam=aliquet&faucibus=ultrices&cursus=erat&urna=tortor&ut=sollicitudin&tellus=mi&nulla=sit&ut=amet&erat=lobortis&id=sapien&mauris=sapien&vulputate=non&elementum=mi&nullam=integer&varius=ac&nulla=neque&facilisi=duis&cras=bibendum&non=morbi&velit=non&nec=quam&nisi=nec&vulputate=dui&nonummy=luctus&maecenas=rutrum&tincidunt=nulla&lacus=tellus&at=in&velit=sagittis&vivamus=dui&vel=vel&nulla=nisl&eget=duis&eros=ac&elementum=nibh&pellentesque=fusce&quisque=lacus&porta=purus&volutpat=aliquet&erat=at&quisque=feugiat&erat=non&eros=pretium&viverra=quis&eget=lectus&congue=suspendisse&eget=potenti&semper=in&rutrum=eleifend&nulla=quam&nunc=a&purus=odio&phasellus=in&in=hac&felis=habitasse&donec=platea&semper=dictumst&sapien=maecenas&a=ut&libero=massa&nam=quis&dui=augue",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/cc0000/ffffff",
    "lastModified": "21-829-3762",
    "leaf": true
  }, {
    "contentEntryUid": 88074,
    "title": "at nibh in hac habitasse",
    "description": "Mauris lacinia sapien quis libero.",
    "entryId": 9988371,
    "author": "Lauren Audley",
    "publisher": "Nada Todarello",
    "licenseType": 75,
    "licenseName": "dictumst morbi",
    "licenseUrl": "http://reddit.com/nisl/nunc/rhoncus/dui/vel/sem.jpg",
    "sourceUrl": "http://utexas.edu/ultrices.xml?convallis=eget&morbi=eleifend&odio=luctus&odio=ultricies&elementum=eu&eu=nibh",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "20-548-2034",
    "leaf": true
  }, {
    "contentEntryUid": 7409,
    "title": "nunc commodo placerat praesent",
    "description": "Morbi vestibulum, velit id pretium iaculis, diam erat fermentum justo, nec condimentum neque sapien placerat ante.",
    "entryId": 3919446,
    "author": "Mace Pfeffel",
    "publisher": "Ilaire Richfield",
    "licenseType": 12,
    "licenseName": "aliquet maecenas",
    "licenseUrl": "https://simplemachines.org/nulla/integer/pede/justo.xml",
    "sourceUrl": "http://elpais.com/in/faucibus/orci/luctus/et/ultrices/posuere.aspx?in=lacinia&magna=aenean&bibendum=sit&imperdiet=amet&nullam=justo&orci=morbi&pede=ut&venenatis=odio&non=cras&sodales=mi&sed=pede&tincidunt=malesuada&eu=in&felis=imperdiet&fusce=et&posuere=commodo&felis=vulputate&sed=justo&lacus=in&morbi=blandit&sem=ultrices&mauris=enim&laoreet=lorem&ut=ipsum&rhoncus=dolor&aliquet=sit&pulvinar=amet&sed=consectetuer&nisl=adipiscing&nunc=elit&rhoncus=proin&dui=interdum&vel=mauris&sem=non&sed=ligula&sagittis=pellentesque&nam=ultrices&congue=phasellus&risus=id&semper=sapien&porta=in&volutpat=sapien&quam=iaculis&pede=congue&lobortis=vivamus&ligula=metus&sit=arcu&amet=adipiscing&eleifend=molestie&pede=hendrerit&libero=at&quis=vulputate&orci=vitae&nullam=nisl&molestie=aenean&nibh=lectus&in=pellentesque&lectus=eget&pellentesque=nunc&at=donec&nulla=quis&suspendisse=orci&potenti=eget&cras=orci&in=vehicula&purus=condimentum&eu=curabitur&magna=in&vulputate=libero&luctus=ut&cum=massa&sociis=volutpat&natoque=convallis&penatibus=morbi",
    "thumbnailUrl": "http://dummyimage.com/200x200.png/dddddd/000000",
    "lastModified": "05-281-4333",
    "leaf": true
  }]
}
