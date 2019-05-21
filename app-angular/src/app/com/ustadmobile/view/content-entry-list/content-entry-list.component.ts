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
    
    this.activeRoute.queryParams.subscribe(args => {
      this.currentEntryUid = args["rootEntryUid"];
      this.entries = dataSample[this.currentEntryUid];
    });
  }

  ngOnInit() {}

  navigate(entry) {
    const rootEntry = entry.entry_root === true;
    const basePath = rootEntry ? '/home/contentEntryList/' :'/home/contentEntryDetail/';

    const args: any = {
      relativeTo: this.activeRoute,
      queryParams: rootEntry ? {rootEntryUid: entry.entry_uid}: {entryUid: entry.entry_uid},
      queryParamsHandling: 'merge',
    };
    this.router.navigate([basePath], args);
  }

  ngOnDestroy(): void {}

}
