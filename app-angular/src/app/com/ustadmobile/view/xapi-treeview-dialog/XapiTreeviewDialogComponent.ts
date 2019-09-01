import { OnInit, Component } from '@angular/core';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import core from 'UstadMobile-core';
import { UmBaseService } from '../../service/um-base.service';
import { MzBaseModal } from 'ngx-materialize';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { Subscription } from 'rxjs';

@Component({
    selector: 'xapi-treeview-dialog', 
    templateUrl: './xapi-treeview-dialog.component.html',
    styleUrls: ['./xapi-treeview-dialog.component.css']
  })
  
export class XapiTreeviewDialogComponent extends MzBaseModal implements OnInit,
core.com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView {

  presenter: core.com.ustadmobile.core.controller.SelectMultipleLocationTreeDialogPresenter;
  subscription : Subscription; 
  systemImpl: any;
  constructor(private umService: UmBaseService, private umDb: UmDbMockService) {
    super();
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
  }

  onCreate(){
    this.presenter = new core.com.ustadmobile.core.controller.SelectMultipleEntriesTreeDialogPresenter(
      this.umService.getContextWrapper(), UmAngularUtil.queryParamsToMap(document.location.search+"&entriesSelected=0", false), this,
      this.umDb.contentEntryParentChildJoinDao)
    this.presenter.onCreate(null)
  }

  ngOnInit() {
    console.log("entries", this.umDb.contentEntryParentChildJoinDao.selectTopEntries())
    this.subscription = UmAngularUtil.registerUmObserver(this)
  }


  populateTopEntries(entries: any){

  }

  setTitle(title: string){

  }

  finish(){

  }
}
