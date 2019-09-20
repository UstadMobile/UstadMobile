import { Injectable } from '@angular/core';
import db from 'UstadMobile-lib-database';
import mpp from 'UstadMobile-lib-database-mpp';
import ents from 'UstadMobile-lib-database-entities'
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Injectable({
  providedIn: 'root'
})
export class UmAppDatabaseService{

  constructor() {}

  initDb(context: UmContextWrapper){
    const entry = {
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
    } as ents.com.ustadmobile.lib.db.entities.ContentEntry

    mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl.Companion.register() 
    const database =  db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(context)
    database.contentEntryDao.insertAsync(entry)
  }
}
