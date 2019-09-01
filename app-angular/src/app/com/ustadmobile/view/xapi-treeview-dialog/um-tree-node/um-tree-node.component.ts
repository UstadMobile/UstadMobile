import { Component, OnInit, Input } from '@angular/core';

export interface UmTreeNode{
  nodeName: string;
  nodeId: any;
  showChildren: boolean;
  children: any[];
}

@Component({
  selector: 'tree-view',
  templateUrl: './um-tree-node.component.html',
  styleUrls: ['./um-tree-node.component.css']
})

export class UmTreeNodeComponent implements OnInit {

  @Input() treeData: UmTreeNode[];
  constructor() { }

  toggleChild(node) {
    node.showChildren = !node.showChildren;
  }

  ngOnInit() {
  }

}
