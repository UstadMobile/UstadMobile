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
  entries = [];
  entryParentJoins = [];

  constructor() {
    super()
    if (!this.initialized) {
      this.initialized = true;
    }
  }

  public contentEntryDao: ContentEntryDao;
  public locationDao = new LocationDao();
  public contentEntryStatusDao = new ContentEntryStatusDao();
  public contentEntryRelatedEntryJoinDao = new ContentEntryRelatedEntryJoinDao();
  public containerDao = new ContainerDao();
  public networkNodeDao = new NetworkNodeDao();
  public xObjectDao = new XObjectDao();
  public xLangMapEntryDao = new XLangMapEntryDao();
  public statementDao = new StatementDao();
  public personDao = new PersonDao();
  public contentEntryParentChildJoinDao = new ContentEntryParentChildJoinDao(this.contentEntryDao);

  getData(entryUid) {
    return this.entries[entryUid];
  }
}

/**DAO */

export class StatementDao{
  getResults(any){
    
  }

  getListResults(any){
    const data = [
      {name:"John Doe",verb:"Attempted question 3 from Entry 4", result:"1", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 4 from Entry 1", result:"2", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 3 from Entry 1", result:"2", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 3 from Entry 1", result:"1", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 5 from Entry 5", result:"2", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 3 from Entry 1", result:"1", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 5 from Entry 1", result:"2", whenDate:1560211200000},
      {name:"John Doe",verb:"Attempted question 3 from Entry 5", result:"2", whenDate:1560211200000},
    ];
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(data)
  }
}

export class XObjectDao{

  findListOfObjectUidFromContentEntryUid(contentEntryUid){
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList([100, 200, 300, 400])
  }
}

export class LocationDao{
  findTopLocationsAsync(){

  }
}

export class ContentEntryParentChildJoinDao{
  constructor(private entryDao){}
  selectTopEntries(){
    const data = [
      {
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
        "contentEntryUid": 72932,
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
        "contentEntryUid": 92589,
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
      },
      {
        "contentEntryUid": 24995,
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
      }
    ]
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(data)
  }
}
export class XLangMapEntryDao{
  getAllVerbsInList(uidList){
    return [
      {verbLangMapUid:1,valueLangMap:"Do"},
      {verbLangMapUid:2,valueLangMap:"DoWhat"},
      {verbLangMapUid:3,valueLangMap:"DoWhatElse"},
      {verbLangMapUid:4,valueLangMap:"DoThis"}
    ]
  }

  getAllVerbs(any){
    return [
      {verbLangMapUid:1,valueLangMap:"Do"},
      {verbLangMapUid:2,valueLangMap:"DoWhat"},
      {verbLangMapUid:3,valueLangMap:"DoWhatElse"},
      {verbLangMapUid:4,valueLangMap:"DoThis"}
    ] 
  }

  getValuesWithListOfId(any){
    const data = [
      {verbLangMapUid:1,objectLangMapUid:2,languageLangMapUid:4,languageVariantLangMapUid:0,valueLangMap:"hello",statementLangMapUid:1},
      {verbLangMapUid:1,objectLangMapUid:2,languageLangMapUid:4,languageVariantLangMapUid:0,valueLangMap:"hello",statementLangMapUid:1},
      {verbLangMapUid:1,objectLangMapUid:2,languageLangMapUid:4,languageVariantLangMapUid:0,valueLangMap:"hello",statementLangMapUid:1},
      {verbLangMapUid:1,objectLangMapUid:2,languageLangMapUid:4,languageVariantLangMapUid:0,valueLangMap:"hello",statementLangMapUid:1},
    ]
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(data)
  }
}

export class PersonDao{

  personList = [
    {personUid:1,name:"Jane Doe"},
    {personUid:2,name:"Frank Doe"},
    {personUid:3,name:"James Doe"},
    {personUid:4,name:"Keneth Doe"}

  ];
  getAllPersonsInList(uidList){
    return this.personList;
  }

  getAllPersons(names,ids){
    return this.personList
  }
}

 export class ContentEntryDao{
  constructor(private entries, private joins) { }

  findLiveContentEntry(entryUid){
    const entry = UmAngularUtil.findEntry(this.entries, entryUid);
    return UmAngularUtil.createObserver(entry);
  }

  findByUidWithContentEntryStatusAsync(entryUid){
    const entry: any = UmAngularUtil.findEntry(this.entries, entryUid);
    entry['contentEntryStatus'] = {downloadStatus:24};
    return entry;
  }

  getChildrenByParentUidWithCategoryFilter(entryUid, language, category) {
    var entries = UmAngularUtil.findChildrenByParentUid(this.joins, this.entries, entryUid);
    if(language != 0){
      entries = entries.splice(0,entries.length - 2);
    }
    if(category != 0){
      entries = entries.splice(0,entries.length - 3);
    }
    return UmAngularUtil.createObserver(entries);
  }

  getContentByUuidAsync(entryUid) {
    
    const entry = UmAngularUtil.findEntry(this.entries, entryUid);
    return entry;
  }

  findUniqueLanguagesInListAsync(entryUid) {
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(languages[entryUid]);
  }

  findByUidAsync(entryUid) {
    return UmAngularUtil.findEntry(this.entries, entryUid);
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
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList([schemas])
  }

  getChildrenByParentAsync(entryUid){
    var entries = UmAngularUtil.findChildrenByParentUid(this.joins, this.entries, entryUid);
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(entries);
  }
}

class ContainerDao{
  findFilesByContentEntryUid(entryUid){
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList([]);
  }

  getMostRecentDownloadedContainerForContentEntryAsync(entryUid){
    const container = {
      mimeType: "application/zip",
      containerUid: 8989,
      fileSize: 8989898
    }

    return container as db.com.ustadmobile.lib.db.entities.Container;
  }

}

class NetworkNodeDao{}

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
  return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(relatedEntries);
}
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

const dashbord = [
  {
    "chartType":100,
    "yAxis": 200,
    "xAxis": 302 ,
    "subGroup": 306,
    "whoFilterList": [100],
    "didFilterList": [200],
    "objectsList": [300],
    "entriesList": [400],
    "fromDate": 3,
    "toDate": 4,
    "locationsList": [2,0,3],
    "reportTitle": "My personal report"
  }
]

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



