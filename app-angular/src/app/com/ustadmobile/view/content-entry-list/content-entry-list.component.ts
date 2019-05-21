import {Component,OnInit} from '@angular/core';
import {dataSample} from '../../util/UmDataSample';
import {environment} from 'src/environments/environment.prod';
import {Router,ActivatedRoute} from '@angular/router';
import { HttpParams } from "@angular/common/http";

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements OnInit {
  entries = [];
  env = environment;
  currentEntryUid = "";

  constructor(private router: Router, private activeRoute: ActivatedRoute) {
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function () {
      return false;
    };

    this.activeRoute.params.subscribe(val => {
      this.currentEntryUid = val.entryUid;
    });
  }

  ngOnInit() {
    this.entries = dataSample[this.currentEntryUid];
  }

  navigate(entry) {
    const basePath = (entry.entry_root === true ? '/home/entryList/' :'/home/entry/') + entry.entry_uid;
    /* const navigateWithParam = entry.entry_root === true ? {}:
     { queryParams: { entryUid: entry.entry_uid , section: 'details'} }; */
    this.router.navigate([basePath]);
  }

  ngOnDestroy(): void {}

}
