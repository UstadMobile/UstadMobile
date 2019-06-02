import { Observable, Subscription } from 'rxjs';
import { UmAngularUtil } from './../../util/UmAngularUtil';
import { UmDbMockService, ContentEntry } from './../../core/db/um-db-mock.service';
import {Component, ElementRef} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { com as core } from 'core';
import {com as db } from 'lib-database';
import {com as util } from 'lib-util';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent extends UmBaseComponent implements 
core.ustadmobile.core.view.ContentEntryListFragmentView {
  
  entries : db.ustadmobile.lib.db.entities.ContentEntry[] = [];
  env = environment;
  private pageNumber: number = 1;
  label_language: string = "";
  private entryListObservable: Observable<ContentEntry[]> = null
  private presenter: core.ustadmobile.core.controller.ContentEntryListFragmentPresenter;
  languages : db.ustadmobile.lib.db.entities.Language[]
  categories: db.ustadmobile.lib.db.entities.DistinctCategorySchema[];
  umForm : FormGroup;
  private subscription: Subscription;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
     umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);

    this.umForm = formBuilder.group({
      'language': ['-1', Validators.required]
    });
  
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
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.label_language = this.getString(this.MessageID.language); 
      }
    });

    //setup language spinner/select listener
    this.umForm.valueChanges.subscribe((form: any) => {
         console.log("changed language", form.language);
    });
    //setup category spinner/select listener
  }

  setContentEntryProvider(provider : Observable<ContentEntry[]>){
    this.entryListObservable = provider;
    this.entryListObservable.subscribe(entries =>{ 
      this.entries.push(...entries);
    })
  }

  openEntry(entry : db.ustadmobile.lib.db.entities.ContentEntry) {
    this.presenter.handleContentEntryClicked(entry);
  }

  setLanguageOptions(languages){
    const languageList = util.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(languages);
    this.languages = languageList.splice(1,languageList.length);
  }

  setCategorySchemaSpinner(categories){
    const categoryList = util.ustadmobile.lib.util.UMUtil.kotlinCategoryMapToJsArray(categories);
    this.categories = categoryList;
    console.log("cate", categoryList) 
  }

  setToolbarTitle(title: string){
    this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
      UmAngularUtil.DISPATCH_TITLE, title))
  }

  ngOnDestroy(){
    super.ngOnDestroy()
    this.presenter.onDestroy();
    this.subscription.unsubscribe();
  }
}
