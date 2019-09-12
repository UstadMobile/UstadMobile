import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { UmAngularUtil } from '../../../util/UmAngularUtil';

export interface UmTreeNode{
  nodeName: string;
  nodeParent: any;
  nodeLevel: number;
  nodeId: any;
  showChildren: boolean;
  checked: boolean;
  children: any[];
}

@Component({
  selector: 'tree-view',
  templateUrl: './um-tree-node.component.html',
  styleUrls: ['./um-tree-node.component.css']
})


export class UmTreeNodeComponent implements OnInit {

  @Input() treeData: UmTreeNode[];

  private  selectedNodeList = new Set();

  constructor() { }

  toggleChild(node) {
    node.showChildren = !node.showChildren;
  }

  setChecked(node){
    if(node.children.length > 0){
      node.children.forEach(nodeChild => {
        nodeChild.checked = !nodeChild.checked
      });
      node.checked = !node.checked
      this.updateSeletectionList(node)
    }else{
      node.checked = !node.checked
      this.updateSeletectionList(node) 
    }
  }

  private updateSeletectionList(node: UmTreeNode){
    const mNode:any = btoa(JSON.stringify({"id":node.nodeId,"name":node.nodeName})) 
    if(node.checked === true){
      this.selectedNodeList.add(mNode) 
    }
    if(node.checked === false){
      this.selectedNodeList.delete(mNode) 
    }
    UmAngularUtil.fireOnDataChanged({nodes: JSON.stringify(Array.from(this.selectedNodeList.values()))})
  }

  ngOnInit() {}

}
