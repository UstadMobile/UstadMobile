import { Observable } from 'rxjs';
import { UmAngularUtil } from './../../util/UmAngularUtil';
import { UmDbMockService, ContentEntry } from './../../core/db/um-db-mock.service';
import {Component} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { com as core } from 'core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent extends UmBaseComponent implements core.ustadmobile.core.view.ContentEntryListFragmentView {
  
  entries : ContentEntry[] = [];
  env = environment;
  private readonly args;
  private presenter: core.ustadmobile.core.controller.ContentEntryListFragmentPresenter;

  constructor(localeService: UmBaseService, router: Router, route: ActivatedRoute, private umDb: UmDbMockService) {
    super(localeService, router, route, umDb);
    this.args = route.snapshot.queryParams;
  }

  ngOnInit() {
    this.presenter = new core.ustadmobile.core.controller
      .ContentEntryListFragmentPresenter(this.context, UmAngularUtil.queryParamsToMap(), this);
    this.presenter.onCreate(null);
  }

  setContentEntryProvider(provider : Observable<ContentEntry[]>){
    provider.subscribe(entries =>{ 
      this.entries = entries;
    })
  }

  ngOnDestroy(){
    this.presenter.onDestroy();
  }

  navigate(entry : ContentEntry) {
    const contentEntry = entry as core.ustadmobile.lib.db.entities.ContentEntry;

    //core.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance.go(basePath,args, this.context,0);

    this.presenter.handleContentEntryClicked(contentEntry);
  }
}
