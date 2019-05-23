import {Component,OnInit} from '@angular/core';
import {dataSample} from '../../util/UmDataSample';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { HttpParams } from "@angular/common/http";
import { sample } from 'hello-ustad';
import { com } from 'core';
import { com as com2 } from 'lib-util';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements com.ustadmobile.core.view.ContentEntryListFragmentView , OnInit {
  
  entries = [];
  env = environment;
  currentEntryUid = "";
  ustadNameFromJs = "";
  private args: Params = null;
  private context: UmContextWrapper;
  private presenter: com.ustadmobile.core.controller.ContentEntryListFragmentPresenter

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
    //const util = new sample.util.HelloUtil();
    ustad.printUstadName();
    this.ustadNameFromJs = ustad.getUstadName();
    //util.printUtil();

    //const util = com.ustadmobile.core.lib.util.UMUtil();
    //const num = util.pad0(6);
    console.log("System time = " , com2.ustadmobile.lib.util.getSystemTimeInMillis());
    //const utilPojo = new com.ustadmobile.core.db.UtilPojo(5, "bob");

    const courseProgress = new com.ustadmobile.core.model.CourseProgress();
    console.log("Um util = " , courseProgress); 
  
    this.presenter = new com.ustadmobile.core.controller.ContentEntryListFragmentPresenter(this.context, this.args, this, this);
    console.log("presenter", this.presenter);
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
