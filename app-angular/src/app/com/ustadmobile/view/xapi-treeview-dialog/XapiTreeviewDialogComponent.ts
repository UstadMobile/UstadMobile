import { OnInit, Component, Input } from '@angular/core';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import core from 'UstadMobile-core';
import { UmBaseService } from '../../service/um-base.service';
import { MzBaseModal } from 'ngx-materialize';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { Subscription } from 'rxjs';
import { TreeNode } from '@angular/router/src/utils/tree';
import { UmTreeNode } from './um-tree-node/um-tree-node.component';

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
  nodes: UmTreeNode[] = []
  sampleData

  constructor(private umService: UmBaseService, private umDb: UmDbMockService) {
    super();
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
  }

  onCreate(){
    this.presenter = new core.com.ustadmobile.core.controller.SelectMultipleEntriesTreeDialogPresenter(
      this.umService.getContextWrapper(), UmAngularUtil.queryParamsToMap(document.location.search+"&entriesSelected=0", false), this,
      this.umDb.contentEntryParentChildJoinDao)
    this.presenter.onCreate(null)
    console.log("This create called")
  }

  ngOnInit() {
    UmAngularUtil.registerResourceReadyListener(this)
  }

  runOnUiThread(runnable){
    runnable.run();
  }


  populateTopEntries(entries: any){
    console.log(entries)
  }

  setTitle(title: string){

  }

  finish(){

  }
}
