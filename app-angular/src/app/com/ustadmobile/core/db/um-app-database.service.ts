import { Injectable } from '@angular/core';
import util from 'UstadMobile-lib-util';
import db from 'UstadMobile-lib-database';
import door from 'UstadMobile-lib-door-runtime';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Injectable({
  providedIn: 'root'
})
export class UmAppDatabaseService{
  
  public database: db.com.ustadmobile.core.db.UmAppDatabase

  constructor() {}

  initDb(context: UmContextWrapper){
    db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(context)
  }
}
