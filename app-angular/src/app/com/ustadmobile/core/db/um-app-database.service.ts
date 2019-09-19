import { Injectable } from '@angular/core';
import db from 'UstadMobile-lib-database';
import mpp from 'UstadMobile-lib-database-mpp';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Injectable({
  providedIn: 'root'
})
export class UmAppDatabaseService{
  
  public database: db.com.ustadmobile.core.db.UmAppDatabase

  constructor() {}

  getInstance(context: UmContextWrapper){
    //mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl.register()
    console.log(mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl)
    const database =  db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(context)
    return database
  }
}
