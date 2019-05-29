import { Observable, PartialObserver, Subscription } from 'rxjs';
import { UmAngularUtil } from './../../util/UmAngularUtil';
import { UmDbMockService, ContentEntry } from './../../core/db/um-db-mock.service';
import {Component} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, Params, NavigationEnd } from '@angular/router';
import { com as core } from 'core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent extends UmBaseComponent implements 
core.ustadmobile.core.view.ContentEntryListFragmentView {
  
  entries : ContentEntry[] = [];
  env = environment;
  private readonly args;
  private pageNumber: number = 1;
  private entryListObservable: Observable<ContentEntry[]> = null
  private presenter: core.ustadmobile.core.controller.ContentEntryListFragmentPresenter;

  constructor(localeService: UmBaseService, router: Router, route: ActivatedRoute, private umDb: UmDbMockService) {
    super(localeService, router, route, umDb);
    this.args = route.snapshot.queryParams;
  
    this.router.events.subscribe((e: any) => {
      if (e instanceof NavigationEnd) {
        this.entries = []
          this.presenter = new core.ustadmobile.core.controller
        .ContentEntryListFragmentPresenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
      }
    });
  }

  ngOnInit() {
    console.log("On init called")
    
  }

  setContentEntryProvider(provider : Observable<ContentEntry[]>){
    this.entryListObservable = provider;
    this.entryListObservable.subscribe(entries =>{ 
      this.entries.push(...entries);
    })
  }

  openEntry(entry : ContentEntry) {
    const contentEntry = entry as core.ustadmobile.lib.db.entities.ContentEntry;
    this.presenter.handleContentEntryClicked(contentEntry);
  }

  setLanguageOptions(languages){
    console.log("language", languages)
  }

  setCategorySchemaSpinner(categories){
    console.log("categories", categories);
  }

  setToolbarTitle(title: string){
    this.subject.next(title)
  }

  onFetchNextPage(){
    this.pageNumber = this.pageNumber + 1;
    console.log("Feching page ", this.pageNumber)
  }

  ngOnDestroy(){
    super.ngOnDestroy()
    this.presenter.onDestroy();
  }
}
