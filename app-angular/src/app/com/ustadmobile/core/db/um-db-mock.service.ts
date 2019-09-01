import { Injectable } from '@angular/core';
import util from 'UstadMobile-lib-util';
import db from 'UstadMobile-lib-database';
import door from 'UstadMobile-lib-door-runtime';
import {UmAngularUtil} from "../../util/UmAngularUtil";
import core from 'UstadMobile-core';

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

  public contentEntryDao;
  public contentEntryStatusDao = new ContentEntryStatusDao();
  public contentEntryRelatedEntryJoinDao = new ContentEntryRelatedEntryJoinDao();
  public containerDao = new ContainerDao();
  public networkNodeDao = new NetworkNodeDao();
  public xObjectDao = new XObjectDao();
  public xLangMapEntryDao = new XLangMapEntryDao();
  public personDao = new PersonDao();

  getData(entryUid) {
    return this.entries[entryUid];
  }
}

/**DAO */

export class XObjectDao{

  findListOfObjectUidFromContentEntryUid(contentEntryUid){
    return [100, 200, 300, 400]
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



