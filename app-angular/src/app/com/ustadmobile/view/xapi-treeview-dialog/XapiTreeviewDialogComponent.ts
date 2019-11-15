import { OnInit, Component } from '@angular/core';
import core from 'UstadMobile-core';
import { UmBaseService } from '../../service/um-base.service';
import { MzBaseModal } from 'ngx-materialize';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import util from 'UstadMobile-lib-util';
import { UmTreeNode } from './um-tree-node/um-tree-node.component';

export class UmDataTreeNode implements UmTreeNode {
  checked: boolean;
  nodeLevel: number;
  nodeParent: any;
  nodeName: string;nodeId: any;
  showChildren: boolean;
  children: any[];
}

@Component({
  selector: 'xapi-treeview-dialog',
  templateUrl: './xapi-treeview-dialog.component.html',
  styleUrls: ['./xapi-treeview-dialog.component.css']
})

export class XapiTreeviewDialogComponent extends MzBaseModal implements OnInit,
core.com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView {
  private presenter: core.com.ustadmobile.core.controller.SelectMultipleLocationTreeDialogPresenter;
  systemImpl: any;
  nodes: UmTreeNode[] = []
  selectedNodeList = []

  constructor(private umService: UmBaseService) {
    super();
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
  }

  onCreate() {
    this.presenter = new core.com.ustadmobile.core.controller.SelectMultipleEntriesTreeDialogPresenter(
      this.umService.getContext(), UmAngularUtil.getArgumentsFromQueryParams({params:document.location.search + "&entriesSelected=0"}), this,
      this.umService.getDbInstance().contentEntryParentChildJoinDao)
    this.presenter.onCreate(null)
  }

  ngOnInit() {
    this.onCreate()
  }

  runOnUiThread(runnable) {
    runnable.run();
  }


  populateTopEntries(entries: any) {
    const entryNodeList = []
    util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(entries).forEach(entry => {
      const entryNode = new UmDataTreeNode()
      entryNode.nodeId = entry.contentEntryUid
      entryNode.nodeName = entry.title
      entryNode.nodeLevel = 0
      entryNode.checked = false;
      entryNode.children = this.createTreeNodes(entry.contentEntryUid, [], 1)
      entryNode.showChildren = false
      entryNodeList.push(entryNode)
    });
    this.nodes = entryNodeList
  }

  private createTreeNodes(parentEntryUid: number, nodes: any[], level: number): any[] {
    const entryNodeList = []
    const entryList = this.umService.getDbInstance().contentEntryDao.getChildrenByParentAsync(parentEntryUid, this.umService.continuation)
    util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(entryList).forEach(entry => {
      const entryNode = new UmDataTreeNode()
      entryNode.nodeId = entry.contentEntryUid
      entryNode.nodeName = entry.title
      entryNode.nodeParent = parentEntryUid
      entryNode.nodeLevel = level
      entryNode.checked = false;
      entryNode.children = this.createTreeNodes(entry.contentEntryUid, nodes, level + 1)
      entryNode.showChildren = false
      entryNodeList.push(entryNode)
    });
    return entryNodeList
  }

  handleOnClickCancel() {
    UmAngularUtil.fireOnDataChanged({
      nodes: "clear"
    })
  }

  showBaseProgressBar() {}

  setTitle() {}

  finish() {}
}
