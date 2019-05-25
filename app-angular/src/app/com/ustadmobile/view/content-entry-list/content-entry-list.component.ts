import { UmDbMockService, ContentEntry } from './../../core/db/um-db-mock.service';
import {Component,OnInit} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { com as core } from 'core';
import { com as db } from 'lib-database';
import { mock, anything, when, spy } from 'ts-mockito';
import { UmContextWrapper } from '../../util/UmContextWrapper';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent implements core.ustadmobile.core.view.ContentEntryListFragmentView ,OnInit {
  
  entries : ContentEntry[] = [];
  env = environment;
  currentEntryUid = "";
  private args: Params = null; 
  private readonly context: UmContextWrapper;
  private presenter: core.ustadmobile.core.controller.ContentEntryListFragmentPresenter;

  constructor(private router: Router, private route: ActivatedRoute, private umDb: UmDbMockService) {
    this.context = new UmContextWrapper(this.router);
    this.context.setActiveRoute(route);
    this.route.queryParams.subscribe((urlParams) => {
      this.args = urlParams;
     this.entries = this.umDb.getDataRepo(this.args["parentUid"]);
    });
  }

  ngOnInit() {
    this.presenter = new core.ustadmobile.core.controller.ContentEntryListFragmentPresenter(this.context, this.args, this);
    //this.presenter.onCreate(new Map<String, String>())
  }

  /* setUpPresenterDbMocks(){
    const umDbMock = spy(db.ustadmobile.core.db.UmAppDatabase);
    let managerMock : any = mock(core.ustadmobile.core.impl.UmAccountManager.Companion);
    when(managerMock.getRepositoryForActiveAccount(anything())).thenReturn(umDbMock);
    console.log("mock",managerMock);
  } */

  ngOnDestroy(){
    this.presenter.onDestroy();
  }

  navigate(entry : ContentEntry) {
    const contentEntry = entry as core.ustadmobile.lib.db.entities.ContentEntry;
    const basePath = entry.leaf === true  ? 'contentEntryList/' :'contentEntryDetail';

    const args: any = {
      relativeTo: this.route,
      queryParams: entry.leaf ? {parentUid: entry.contentEntryUid}: {entryUid: entry.contentEntryUid},
      queryParamsHandling: 'merge',
    };
    //core.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance.go(basePath,args, this.context,0);

    this.presenter.handleContentEntryClicked(contentEntry);
  }
}
