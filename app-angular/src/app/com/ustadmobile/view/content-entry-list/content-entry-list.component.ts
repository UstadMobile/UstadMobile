import {Component,OnInit} from '@angular/core';
import {dataSample} from '../../util/UmDataSample';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { HttpParams } from "@angular/common/http";
import { sample } from 'hello-ustad';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements OnInit {
  
  entries = [];
  env = environment;
  currentEntryUid = "";
  ustadNameFromJs = "";
  private args: Params = null;
  private context: UmContextWrapper;

  constructor(private router: Router, private route: ActivatedRoute) {
    this.args = this.route.snapshot.queryParams;
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(route);
    this.route.queryParams.subscribe(args => {
      this.currentEntryUid = args["rootEntryUid"];
      this.entries = dataSample[this.currentEntryUid];
    });
  }

  ngOnInit() {

    const ustad = new sample.HelloUstadSample();
    ustad.printUstadName();
    this.ustadNameFromJs = ustad.getUstadName();

    //this.presenter = new controller.ContentEntryListFragmentPresenter(this.context, this.args, this, this);
  }

  navigate(entry) {
    const rootEntry = entry.entry_root === true;
    const basePath = rootEntry ? '/home/contentEntryList/' :'/home/contentEntryDetail/';

    const args: any = {
      relativeTo: this.route,
      queryParams: rootEntry ? {rootEntryUid: entry.entry_uid}: {entryUid: entry.entry_uid},
      queryParamsHandling: 'merge',
    };
    this.router.navigate([basePath], args);
  }

  ngOnDestroy(): void {}

}
